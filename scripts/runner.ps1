. ".\scripts\InvokeProcess.ps1"
$date = Get-Date -Format "yy-MM-dd HH:mm:ss"

$dataPath = "./data"
[String[]]$modes = "par", "seq"

foreach ($x in $modes)
{
    [String[]]$nums = "65536", "131072", "262144", "524288", "1048576", "2097152"
    foreach ($num in $nums) {
        $train_input = "$dataPath/input_train_$num.csv"
        $test_input  = "$dataPath/input_test_$num.csv"
        $pinfo = New-Object System.Diagnostics.ProcessStartInfo
        $pinfo.FileName = "C:\Program Files\Java\jre1.8.0_341\bin\java.exe"
        $pinfo.RedirectStandardError = $true
        $pinfo.RedirectStandardOutput = $true
        $pinfo.UseShellExecute = $false
        $pinfo.Arguments = "-Xmx10G -Xss8G -jar ./target/scala-2.12/FinalProject-assembly-0.1.0-SNAPSHOT.jar $train_input $test_input $x"

        $current = Get-Date -Format "HH:mm:ss"
        "${date}_$current - Starting $x in $num..." | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        $results = Invoke-Process $pinfo

        "--------------StdOut-----------------" | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        $results.StdOut | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        "--------------StdErr-----------------" | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        $results.StdErr | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        "-------------ExitCode----------------" | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        $results.ExitCode | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
        
        $current = Get-Date -Format "HH:mm:ss"
        "${date}_$current - ... $x in $num - Done" | Tee-Object -Append "$dataPath/logs/${num}.log" | Write-Host
    }    
}
