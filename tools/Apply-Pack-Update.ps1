[CmdletBinding()]
param(
    [string]$ClientPath,
    [string]$ServerPath
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Common.ps1')

if (-not $ClientPath -and -not $ServerPath) {
    throw 'Pass -ClientPath, -ServerPath, or both. Use the game directory containing mods.'
}
$plan = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'patch.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$targets = @()
foreach ($side in @('client', 'server')) {
    $requested = if ($side -eq 'client') { $ClientPath } else { $ServerPath }
    if (-not $requested) { continue }
    $root = (Resolve-Path -LiteralPath $requested).Path
    $mods = Join-Path $root 'mods'
    if (-not (Test-Path -LiteralPath $mods -PathType Container)) {
        throw ('The game directory must contain mods: ' + $root)
    }
    $spec = $plan.$side
    if (-not ((Test-Path -LiteralPath (Join-Path $mods $spec.baseline)) -or
              (Test-Path -LiteralPath (Join-Path $mods $spec.replacement)))) {
        throw ('The installed M1 version is outside the update range: ' + $root)
    }
    $targets += [pscustomobject]@{ root = $root; mods = $mods; spec = $spec; side = $side }
}
if ($targets.Count -eq 2 -and $targets[0].root -eq $targets[1].root) {
    throw 'ClientPath and ServerPath must point to different game directories.'
}

function Get-DownloadPath($File) {
    if ($File.PSObject.Properties['path'] -and $File.path) { return $File.path }
    return 'mods/' + $File.file_name
}

function Get-UpdateDownload($File, $Path) {
    if (Test-PinnedFile $Path $File.sha256) { return }
    $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
    if ($curl) {
        $partial = $Path + '.' + [guid]::NewGuid().ToString('N') + '.part'
        try {
            & $curl.Source --fail --silent --show-error --location --retry 1 --max-time 35 `
                --output $partial $File.download_url
            if ($LASTEXITCODE -eq 0 -and (Test-PinnedFile $partial $File.sha256)) {
                Move-Item -LiteralPath $partial -Destination $Path -Force
                return
            }
        } finally {
            if (Test-Path -LiteralPath $partial) { Remove-Item -LiteralPath $partial -Force }
        }
    }
    Get-PinnedFile $File.download_url $Path $File.sha256
}

function Merge-ClientPreset($RelativePath, $Source, $Destination) {
    if ($RelativePath -notin @('config/stellarview-client.toml', 'config/iris.properties')) { return $false }
    if (-not (Test-Path -LiteralPath $Destination -PathType Leaf)) { return $false }
    $text = [IO.File]::ReadAllText($Destination)
    if ($RelativePath -eq 'config/stellarview-client.toml') {
        $setting = '(?m)^([ \t]*static_sky[ \t]*=[ \t]*)(?:true|false)'
        $section = '(?m)^[ \t]*\["Stellar View Client Config"\."General Config"\.client\][^\r\n]*'
        if ($text -match $setting) {
            $text = [regex]::Replace($text, $setting, '${1}true')
        } elseif ($text -match $section) {
            $text = [regex]::Replace($text, $section, '$0' + "`nstatic_sky = true")
        } else {
            $text = $text.TrimEnd() + "`n`n" + [IO.File]::ReadAllText($Source)
        }
    } else {
        foreach ($line in [IO.File]::ReadAllLines($Source)) {
            if ($line -notmatch '^([a-zA-Z]+)=') { continue }
            $setting = '(?m)^[ \t]*' + [regex]::Escape($Matches[1]) + '[ \t]*[=:][^\r\n]*'
            if ($text -match $setting) {
                $text = [regex]::Replace($text, $setting, $line)
            } else {
                $text = $text.TrimEnd() + "`n" + $line + "`n"
            }
        }
    }
    [IO.File]::WriteAllText($Destination, $text, [Text.UTF8Encoding]::new($false))
    return $true
}

$cache = Join-Path $PSScriptRoot 'downloads'
[void](New-Item -ItemType Directory -Path $cache -Force)
foreach ($mod in $plan.downloads) {
    $relevant = @($targets | Where-Object { $mod.side -eq 'both' -or $_.side -eq $mod.side })
    if ($relevant.Count -eq 0) { continue }
    $file = Join-Path $cache $mod.file_name
    foreach ($target in $relevant) {
        $present = Join-Path $target.root (Get-DownloadPath $mod)
        if (Test-PinnedFile $present $mod.sha256) {
            Copy-Item -LiteralPath $present -Destination $file -Force
            break
        }
    }
    Get-UpdateDownload $mod $file
}

foreach ($target in $targets) {
    foreach ($file in @($plan.shared) + @($target.spec.files)) {
        $from = Join-Path $PSScriptRoot $file.source
        if (-not (Test-PinnedFile $from $file.sha256)) { throw ('Invalid update file: ' + $file.source) }
    }
}
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
foreach ($target in $targets) {
    $backup = Join-Path $target.root "m1-update-backups/$stamp"
    foreach ($file in @($plan.shared) + @($target.spec.files)) {
        $from = Join-Path $PSScriptRoot $file.source
        $to = Join-Path $target.root $file.target
        if (Test-Path -LiteralPath $to -PathType Leaf) {
            $backupFile = Join-Path $backup $file.target
            [void](New-Item -ItemType Directory -Path (Split-Path -Parent $backupFile) -Force)
            Copy-Item -LiteralPath $to -Destination $backupFile
        }
        [void](New-Item -ItemType Directory -Path (Split-Path -Parent $to) -Force)
        if ($target.side -eq 'client' -and (Merge-ClientPreset $file.target $from $to)) { continue }
        if ($file.target -eq 'config/fallingtree.json' -and (Test-Path -LiteralPath $to -PathType Leaf)) {
            $config = Get-Content -LiteralPath $to -Raw -Encoding UTF8 | ConvertFrom-Json
            $default = Get-Content -LiteralPath $from -Raw -Encoding UTF8 | ConvertFrom-Json
            if ($config.PSObject.Properties['trees'] -and $config.trees) {
                foreach ($key in @('maxSize', 'maxScanSize')) {
                    $config.trees | Add-Member -NotePropertyName $key -NotePropertyValue $default.trees.$key -Force
                }
                $json = $config | ConvertTo-Json -Depth 32
                [IO.File]::WriteAllText($to, $json + "`n", [Text.UTF8Encoding]::new($false))
                continue
            }
        }
        Copy-Item -LiteralPath $from -Destination $to -Force
    }
    foreach ($mod in $plan.downloads) {
        if ($mod.side -ne 'both' -and $mod.side -ne $target.side) { continue }
        $relativePath = Get-DownloadPath $mod
        $to = Join-Path $target.root $relativePath
        if (Test-PinnedFile $to $mod.sha256) { continue }
        if (Test-Path -LiteralPath $to -PathType Leaf) {
            $backupFile = Join-Path $backup $relativePath
            [void](New-Item -ItemType Directory -Path (Split-Path -Parent $backupFile) -Force)
            Copy-Item -LiteralPath $to -Destination $backupFile
        }
        [void](New-Item -ItemType Directory -Path (Split-Path -Parent $to) -Force)
        Copy-Item -LiteralPath (Join-Path $cache $mod.file_name) -Destination $to -Force
    }
    foreach ($old in $target.spec.retire) {
        $relativePath = if ($old.Contains('/')) { $old } else { 'mods/' + $old }
        $oldFile = Join-Path $target.root $relativePath
        if (Test-Path -LiteralPath $oldFile -PathType Leaf) {
            $backupFile = Join-Path $backup $relativePath
            [void](New-Item -ItemType Directory -Path (Split-Path -Parent $backupFile) -Force)
            Move-Item -LiteralPath $oldFile -Destination $backupFile
        }
    }
    Write-Host ("Updated " + $target.root + " to " + $plan.version)
}
