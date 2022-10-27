. ".\scripts\InvokeProcess.ps1"
$date = Get-Date -Format "yy-MM-dd HH:mm:ss"

$dataPath = "./data"
[String[]]$nums = "1024", "2048", "4096", "8192"

foreach ($num in $nums) {
    $train_input = "$dataPath/input_train_$num.csv"
    $test_input  = "$dataPath/input_test_$num.csv"
    [String[]]$modes = "seq", "par"

    foreach ($x in $modes)
    {
        $pinfo = New-Object System.Diagnostics.ProcessStartInfo
        $pinfo.FileName = "java"
        $pinfo.RedirectStandardError = $true
        $pinfo.RedirectStandardOutput = $true
        $pinfo.UseShellExecute = $false
        $pinfo.Arguments = "-Xmx10G -Xss8G -jar ./target/scala-2.12/FinalProject-assembly-0.1.0-SNAPSHOT.jar $train_input $test_input $x"

        "$date - Starting $x..." | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        $results = Invoke-Process $pinfo

        "--------------StdOut-----------------" | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        $results.StdOut | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        "--------------StdErr-----------------" | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        $results.StdErr | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        "-------------ExitCode----------------" | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        $results.ExitCode | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
    }
}
