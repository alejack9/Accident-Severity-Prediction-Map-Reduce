. ".\scripts\InvokeProcess.ps1"
$date = Get-Date -Format "yy-MM-dd HH:mm:ss"

$dataPath = "./data"
[String[]]$modes = "seq"
# [String[]]$modes = "seq"

Write-Host $PWD

foreach ($x in $modes)
{
    [String[]]$nums = "32768"
    # [String[]]$nums =  "16384", "8192", "4096", "2048", "1024"
    foreach ($num in $nums) {
        $train_input = "$dataPath/input_train_${num}.csv"
        $test_input  = "$dataPath/input_test_${num}.csv"
        $pinfo = New-Object System.Diagnostics.ProcessStartInfo
        $pinfo.FileName = "C:\Program Files\Java\jre1.8.0_341\bin\java.exe"
        $pinfo.RedirectStandardError = $true
        $pinfo.RedirectStandardOutput = $true
        $pinfo.UseShellExecute = $false
        $pinfo.Arguments = "-Xmx10G -Xss8G -jar .\target\scala-2.12\AccidentSeverityPrediction-assembly-1.0.0.jar $train_input $test_input $x $dataPath\results\${num}_${x}_cont.log"

        $current = Get-Date -Format "HH:mm:ss"
        "${date}_$current - Starting $x in $num..." | Tee-Object -Append "$dataPath\logs\${num}_${x}_cont.log" | Write-Host
        $results = Invoke-Process $pinfo
        $results.StdOut | Tee-Object -Append "$dataPath\logs\${num}_${x}_cont.log" | Write-Host
        $results.StdErr | Tee-Object -Append "$dataPath\logs\${num}_${x}_cont.log" | Write-Host
        "-------------ExitCode----------------" | Tee-Object -Append "$dataPath\logs\${num}_${x}_cont.log" | Write-Host
        $results.ExitCode | Tee-Object -Append "$dataPath\logs\${num}_${x}_cont.log" | Write-Host
        
        $current = Get-Date -Format "HH:mm:ss"
        "${date}_$current - ... $x in $num - Done" | Tee-Object -Append "$dataPath\logs\${num}_${x}_cont.log" | Write-Host
    }    
}
