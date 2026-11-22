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

$cache = Join-Path $PSScriptRoot 'downloads'
[void](New-Item -ItemType Directory -Path $cache -Force)
foreach ($mod in $plan.downloads) {
    $relevant = @($targets | Where-Object { $mod.side -eq 'both' -or $_.side -eq $mod.side })
    if ($relevant.Count -eq 0) { continue }
    $file = Join-Path $cache $mod.file_name
    foreach ($target in $relevant) {
        $present = Join-Path $target.mods $mod.file_name
        if (Test-PinnedFile $present $mod.sha256) {
            Copy-Item -LiteralPath $present -Destination $file -Force
            break
        }
    }
    Get-PinnedFile $mod.download_url $file $mod.sha256
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
        $to = Join-Path $target.mods $mod.file_name
        if (Test-PinnedFile $to $mod.sha256) { continue }
        if (Test-Path -LiteralPath $to -PathType Leaf) {
            $backupMods = Join-Path $backup 'mods'
            [void](New-Item -ItemType Directory -Path $backupMods -Force)
            Copy-Item -LiteralPath $to -Destination $backupMods
        }
        Copy-Item -LiteralPath (Join-Path $cache $mod.file_name) -Destination $to -Force
    }
    foreach ($old in $target.spec.retire) {
        $oldFile = Join-Path $target.mods $old
        if (Test-Path -LiteralPath $oldFile -PathType Leaf) {
            $backupMods = Join-Path $backup 'mods'
            [void](New-Item -ItemType Directory -Path $backupMods -Force)
            Move-Item -LiteralPath $oldFile -Destination (Join-Path $backupMods $old)
        }
    }
    Write-Host ("Updated " + $target.root + " to " + $plan.version)
}
