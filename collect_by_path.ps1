# File configuration
$inputFile = "D:\WorkSpace\MPJ\BookStoreOnline\input_paths.txt"
$outputFile = "D:\WorkSpace\MPJ\BookStoreOnline\collected_content.txt"

# Create template input file if not exists
if (-not (Test-Path $inputFile)) {
    "@# Paste file paths here (one per line)`r`n# Example: D:\WorkSpace\MPJ\src\Main.java" | Out-File $inputFile -Encoding UTF8
    Write-Host "---"
    Write-Host "Created 'input_paths.txt'. Please paste your file paths there and run again."
    exit
}

# Read paths
$paths = Get-Content $inputFile | Where-Object { $_.Trim() -and -not $_.StartsWith("#") -and -not $_.StartsWith("@") }
$total = ($paths | Measure-Object).Count

if ($total -eq 0) {
    Write-Host "No paths found in 'input_paths.txt'!" -ForegroundColor Yellow
    exit
}

Write-Host "Processing $total files..." -ForegroundColor Green

# Header for output
if (Test-Path $outputFile) { Remove-Item $outputFile -Force }

"================================================================================" | Out-File $outputFile -Encoding UTF8
"                    COMBINED SOURCE CODE CONTENT                                " | Out-File $outputFile -Append -Encoding UTF8
"================================================================================" | Out-File $outputFile -Append -Encoding UTF8
"" | Out-File $outputFile -Append -Encoding UTF8
"Created: $((Get-Date).ToString('dd/MM/yyyy HH:mm:ss'))" | Out-File $outputFile -Append -Encoding UTF8
"Total files: $total" | Out-File $outputFile -Append -Encoding UTF8
"" | Out-File $outputFile -Append -Encoding UTF8

$count = 0
foreach ($rawPath in $paths) {
    $count++
    $path = $rawPath.Trim().Trim('"').Replace('/', '\')
    
    $finalPath = $null
    if (Test-Path $path) {
        $finalPath = $path
    } else {
        # Fuzzy search: Tìm kiếm file trong toàn bộ thư mục hiện tại nếu đường dẫn trực tiếp không đúng
        Write-Host "[$count/$total] Path not found directly, searching for: $path..." -ForegroundColor Yellow
        $fileName = Split-Path $path -Leaf
        $found = Get-ChildItem -Path . -Filter $fileName -Recurse -ErrorAction SilentlyContinue | 
                 Where-Object { $_.FullName -like "*$path" -or $_.Name -eq $path } | 
                 Select-Object -First 1
        
        if ($found) {
            $finalPath = $found.FullName
            Write-Host " -> Found at: $finalPath" -ForegroundColor Cyan
        }
    }

    if ($finalPath) {
        $file = Get-Item $finalPath
        Write-Host "[$count/$total] Reading: $($file.Name)"
        
        "" | Out-File $outputFile -Append -Encoding UTF8
        "################################################################################" | Out-File $outputFile -Append -Encoding UTF8
        "# FILE [$count / $total]: $($file.Name)" | Out-File $outputFile -Append -Encoding UTF8
        "# Full Path: $finalPath" | Out-File $outputFile -Append -Encoding UTF8
        "################################################################################" | Out-File $outputFile -Append -Encoding UTF8
        "" | Out-File $outputFile -Append -Encoding UTF8
        
        try {
            $content = Get-Content -Path $finalPath -Raw -Encoding UTF8
            $content | Out-File $outputFile -Append -Encoding UTF8
        } catch {
            "ERROR: Could not read file - $($_.Exception.Message)" | Out-File $outputFile -Append -Encoding UTF8
        }
    } else {
        Write-Host "[$count/$total] NOT FOUND: $path" -ForegroundColor Red
        "!!! WARNING: Could not find or access: $path" | Out-File $outputFile -Append -Encoding UTF8
    }
}

Write-Host "`nDONE! Output saved to: $outputFile" -ForegroundColor Green
