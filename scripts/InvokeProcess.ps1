function Invoke-Process {
    # Runs the specified executable and captures its exit code, stdout
    # and stderr.
    # Returns: custom object.
    param(
		[System.Diagnostics.ProcessStartInfo]$pinfo
    )

    # Creating process object.
    $p = New-Object -TypeName System.Diagnostics.Process
    $p.StartInfo = $pinfo

    # Creating string builders to store stdout and stderr.
    $oStdErrBuilder = New-Object -TypeName System.Text.StringBuilder

    # Adding event handers for stdout and stderr.
    $sScripBlock = {
        if (! [String]::IsNullOrEmpty($EventArgs.Data)) {
            $Event.MessageData.AppendLine($EventArgs.Data)
        }
    }
    $oStdErrEvent = Register-ObjectEvent -InputObject $p `
        -Action $sScripBlock -EventName 'ErrorDataReceived' `
        -MessageData $oStdErrBuilder

    # Starting process.
    $p.Start()
    $p.BeginErrorReadLine()
	$stdOut = $p.StandardOutput.ReadToEnd()
    $p.WaitForExit()

    # Unregistering events to retrieve process output.
    Unregister-Event -SourceIdentifier $oStdErrEvent.Name

    $oResult = New-Object -TypeName PSObject -Property ([Ordered]@{
        "ExitCode" = $p.ExitCode;
        "StdOut"   = $stdOut;
        "StdErr"   = $oStdErrBuilder.ToString().Trim()
    })

    return $oResult
}