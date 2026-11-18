Set-StrictMode -Version Latest

function Get-Java21 {
    param([string]$Requested = 'java')
    $candidates = New-Object 'System.Collections.Generic.List[string]'
    $candidates.Add($Requested)
    if ($env:JAVA_HOME) { $candidates.Add((Join-Path $env:JAVA_HOME 'bin\java.exe')) }
    $cfRuntime = Join-Path $env:USERPROFILE 'curseforge\minecraft\Install\runtime'
    if (Test-Path -LiteralPath $cfRuntime) {
        Get-ChildItem -LiteralPath $cfRuntime -Filter java.exe -Recurse -File -ErrorAction SilentlyContinue |
            ForEach-Object { $candidates.Add($_.FullName) }
    }
    foreach ($candidate in $candidates) {
        $command = Get-Command $candidate -CommandType Application -ErrorAction SilentlyContinue
        if (-not $command) { continue }
        $process = New-Object System.Diagnostics.Process
        $process.StartInfo.FileName = $command.Source
        $process.StartInfo.Arguments = '-version'
        $process.StartInfo.UseShellExecute = $false
        $process.StartInfo.RedirectStandardError = $true
        $process.StartInfo.RedirectStandardOutput = $true
        $process.StartInfo.CreateNoWindow = $true
        try {
            [void]$process.Start()
            $version = $process.StandardError.ReadToEnd() + $process.StandardOutput.ReadToEnd()
            $process.WaitForExit()
            if ($process.ExitCode -eq 0 -and $version -match 'version\s+"21[\."]') {
                return $command.Source
            }
        } catch { continue } finally { $process.Dispose() }
    }
    throw 'Java 21 was not found. Install a Java 21 runtime or pass -JavaPath with the full path to java.exe.'
}

function Test-PinnedFile {
    param([string]$Path, [string]$Sha256)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { return $false }
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() -eq $Sha256.ToLowerInvariant()
}

function Test-NeoForgeServer {
    param([string]$ServerPath, [string]$Version)
    $library = Join-Path $ServerPath "libraries\net\neoforged\neoforge\$Version"
    $winArgs = Join-Path $library 'win_args.txt'
    $patchedJar = Join-Path $library "neoforge-$Version-server.jar"
    # The installer extracts win_args.txt before running its patch processors.
    if (-not (Test-Path -LiteralPath $winArgs -PathType Leaf) -or
        -not (Test-Path -LiteralPath $patchedJar -PathType Leaf)) { return $false }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    try {
        $archive = [IO.Compression.ZipFile]::OpenRead($patchedJar)
        try { return $archive.Entries.Count -gt 0 } finally { $archive.Dispose() }
    } catch [System.IO.InvalidDataException], [System.IO.IOException] {
        return $false
    }
}

function Get-PinnedFile {
    param([string]$Url, [string]$Path, [string]$Sha256)
    if (Test-PinnedFile $Path $Sha256) { return }
    $partial = $Path + '.part'
    for ($attempt = 1; $attempt -le 3; $attempt++) {
        try {
            Invoke-WebRequest -UseBasicParsing -Uri $Url -OutFile $partial
            if (-not (Test-PinnedFile $partial $Sha256)) { throw ('Checksum mismatch: ' + $Path) }
            Move-Item -LiteralPath $partial -Destination $Path -Force
            return
        } catch {
            if (Test-Path -LiteralPath $partial) { Remove-Item -LiteralPath $partial -Force }
            if ($attempt -eq 3) { throw }
            Start-Sleep -Seconds $attempt
        }
    }
}
