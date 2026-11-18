[CmdletBinding()]
param(
    [string]$JavaPath = 'java',
    [string]$ClientModsPath = '',
    [switch]$AcceptEula
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Common.ps1')

if (-not $AcceptEula) {
    throw 'Read https://aka.ms/MinecraftEULA and rerun with -AcceptEula if you agree.'
}
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$java = Get-Java21 $JavaPath
$lockPath = Join-Path $PSScriptRoot 'mods.lock.json'
$lock = Get-Content -LiteralPath $lockPath -Raw -Encoding UTF8 | ConvertFrom-Json
$server = Join-Path $PSScriptRoot 'server'
$marker = Join-Path $server '.m1-prototype.json'
if ((Test-Path -LiteralPath $server) -and -not (Test-Path -LiteralPath $marker)) {
    $existing = @(Get-ChildItem -LiteralPath $server -Force)
    if ($existing.Count -gt 0) { throw 'The server directory is not empty and is not an M1 prototype installation. Use a fresh folder.' }
}
[void](New-Item -ItemType Directory -Path $server -Force)
$utf8 = New-Object System.Text.UTF8Encoding($false)
$markerData = @{ pack = $lock.pack.name; version = $lock.pack.version } | ConvertTo-Json -Compress
[IO.File]::WriteAllText($marker, $markerData, $utf8)
$mods = Join-Path $server 'mods'
[void](New-Item -ItemType Directory -Path $mods -Force)
$wanted = @($lock.files | Where-Object { $_.side -ne 'client' })
$allowed = @($wanted | ForEach-Object { $_.file_name }) + @($lock.extra_files | Where-Object { $_.side -ne 'client' } | ForEach-Object { $_.file_name })
$unexpected = @(Get-ChildItem -LiteralPath $mods -Filter '*.jar' -File | Where-Object { $_.Name -notin $allowed })
if ($unexpected.Count -gt 0) { throw ('Unexpected mods found: ' + (($unexpected | ForEach-Object { $_.Name }) -join ', ')) }

$index = 0
foreach ($file in $wanted) {
    $index++
    Write-Host ('[{0}/{1}] {2}' -f $index, $wanted.Count, $file.file_name)
    $target = Join-Path $mods $file.file_name
    if (Test-PinnedFile $target $file.sha256) { continue }
    if ($ClientModsPath) {
        $local = Join-Path $ClientModsPath $file.file_name
        if (Test-PinnedFile $local $file.sha256) {
            Copy-Item -LiteralPath $local -Destination $target -Force
            continue
        }
    }
    Get-PinnedFile $file.download_url $target $file.sha256
}

$overrides = Join-Path $PSScriptRoot 'overrides'
foreach ($file in ($lock.extra_files | Where-Object { $_.side -ne 'client' })) {
    $source = Join-Path $overrides $file.path
    if (-not (Test-PinnedFile $source $file.sha256)) { throw ('Invalid bundled file: ' + $file.path) }
}
Get-ChildItem -LiteralPath $overrides -Recurse -File | ForEach-Object {
    $relative = $_.FullName.Substring($overrides.Length).TrimStart([char[]]"\/")
    $target = Join-Path $server $relative
    [void](New-Item -ItemType Directory -Path (Split-Path -Parent $target) -Force)
    if ($relative -like 'mods\*' -or -not (Test-Path -LiteralPath $target)) {
        Copy-Item -LiteralPath $_.FullName -Destination $target -Force
    }
}

$installer = Join-Path $PSScriptRoot ('neoforge-' + $lock.neoforge + '-installer.jar')
if (-not (Test-NeoForgeServer -ServerPath $server -Version $lock.neoforge)) {
    Get-PinnedFile $lock.neoforge_installer.url $installer $lock.neoforge_installer.sha256
    Push-Location $PSScriptRoot
    try {
        & $java '-jar' $installer '--installServer' $server
        $installExit = $LASTEXITCODE
        if ($installExit -ne 0) {
            Write-Warning 'NeoForge installation failed. Retrying with the libraries already downloaded.'
            Start-Sleep -Seconds 2
            & $java '-jar' $installer '--installServer' $server
            $installExit = $LASTEXITCODE
        }
    } finally { Pop-Location }
    if ($installExit -ne 0 -or -not (Test-NeoForgeServer -ServerPath $server -Version $lock.neoforge)) {
        throw "NeoForge installation incomplete (exit code $installExit). See ${installer}.log."
    }
}
$properties = Join-Path $server 'server.properties'
if (-not (Test-Path -LiteralPath $properties)) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'server.properties') -Destination $properties
}
[IO.File]::WriteAllText((Join-Path $server 'eula.txt'), "eula=true`n", $utf8)
Write-Host ''
Write-Host 'Installation complete. Start the server using Start-Server.cmd.'
Write-Host 'Open FTB Quests to start the first industrial chapter.'
