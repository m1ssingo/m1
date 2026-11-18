[CmdletBinding()]
param([string]$JavaPath = 'java', [ValidateRange(3,16)][int]$MemoryGB = 5)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Common.ps1')
$java = Get-Java21 $JavaPath
$server = Join-Path $PSScriptRoot 'server'
$lock = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'mods.lock.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$argsFile = 'libraries/net/neoforged/neoforge/' + $lock.neoforge + '/win_args.txt'
if (-not (Test-NeoForgeServer -ServerPath $server -Version $lock.neoforge)) {
    throw 'NeoForge installation is incomplete. Run Install-Server.ps1 first.'
}
Push-Location $server
try {
    & $java '-Xms1G' ('-Xmx' + $MemoryGB + 'G') '-XX:ActiveProcessorCount=4' ('@' + $argsFile) 'nogui'
    $result = $LASTEXITCODE
} finally { Pop-Location }
if ($result -ne 0) { throw ('Server exited with code ' + $result + '. See server/logs/latest.log.') }
