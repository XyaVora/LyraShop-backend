# =============================================
# Script chạy LyraShop Backend locally
# Chỉnh DB_PASSWORD cho đúng với MySQL của bạn
# =============================================

# Đảm bảo dùng Java 25
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
$env:PATH = "C:\Program Files\Java\jdk-25.0.4.1\bin;" + ($env:PATH -replace "C:\\Program Files\\Eclipse Adoptium\\jdk-17[^;]*\\bin;?", "")

$env:DB_URL      = "jdbc:mysql://localhost:3306/lyrashop_db?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true"
$env:DB_USERNAME = "lyrashop"
$env:DB_PASSWORD = "110252702"   # <-- đổi password ở đây nếu cần

# Tạo JWT secret ngẫu nhiên mỗi lần chạy
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtKey = New-Object byte[] 32
$rng.GetBytes($jwtKey)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($jwtKey)

Write-Host "✅ Env vars đã được set:" -ForegroundColor Green
Write-Host "   DB_URL      = $env:DB_URL"
Write-Host "   DB_USERNAME = $env:DB_USERNAME"
Write-Host "   DB_PASSWORD = ****"
Write-Host ""
Write-Host "🚀 Đang khởi động Spring Boot..." -ForegroundColor Cyan

.\mvnw.cmd spring-boot:run
