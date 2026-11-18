<#
.SYNOPSIS
    Сборка и выкладка VedHelper в RuStore: поднять версию → тесты → AAB → API RuStore.

.DESCRIPTION
    Один запуск делает весь релизный цикл:
      1. поднимает versionCode (+1) и versionName (patch/minor/major) в app/build.gradle.kts;
      2. прогоняет unit-тесты (можно отключить -SkipTests);
      3. собирает подписанный bundleRelease (AAB);
      4. публикует в RuStore через Public API: токен → черновик версии → загрузка AAB
         → (по желанию) отправка на модерацию.

    Ключ подписи берётся из переменных окружения MYAPP_KEYSTORE / MYAPP_KEYSTORE_PASSWORD /
    MYAPP_KEY_ALIAS (как в CI) либо из параметров -Keystore / -KeystorePassword / -KeyAlias.

    Доступ к RuStore API: RS_KEY_ID и RS_PRIVATE_KEY (base64 PKCS#8, как в CI secrets).
    Без них скрипт остановится на шаге сборки — для локальной сборки используйте -BuildOnly.

.PARAMETER Bump
    Как поднять версию: patch (по умолчанию), minor, major, none.

.PARAMETER Version
    Задать versionName явно, например 1.3.0 (versionCode всё равно +1).

.PARAMETER Message
    Текст «Что нового» для черновика версии.

.PARAMETER SendToModeration
    Отправить черновик на модерацию после загрузки AAB.
    По умолчанию черновик остаётся в консоли RuStore — версию отправляют вручную.

.PARAMETER Instant
    publishType=INSTANTLY (публикация сразу после модерации). По умолчанию MANUAL.

.PARAMETER BuildOnly
    Только поднять версию и собрать AAB, без публикации (не нужны ключи RuStore).

.PARAMETER DryRun
    Показать план и новые версии, ничего не менять и не собирать.

.PARAMETER SkipTests
    Не прогонять unit-тесты перед сборкой.

.PARAMETER DevApiHost
    Адрес API для debug-сборки (по умолчанию http://10.0.2.2:8180). На релиз не влияет.

.EXAMPLE
    # обычный релиз: 1.2 → 1.2.1, тесты, AAB, черновик в RuStore
    .\deploy-rustore.ps1 -Message "Исправлен расчёт кВт и валюты" -SendToModeration

.EXAMPLE
    # только собрать AAB с версией 1.3.0
    .\deploy-rustore.ps1 -Version 1.3.0 -BuildOnly

.EXAMPLE
    # посмотреть, что произойдёт
    .\deploy-rustore.ps1 -Bump minor -DryRun
#>

[CmdletBinding()]
param(
    [ValidateSet('patch', 'minor', 'major', 'none')]
    [string]$Bump = 'patch',

    [string]$Version,

    [string]$Message = 'Обновление приложения',

    [switch]$SendToModeration,

    [switch]$Instant,

    [switch]$BuildOnly,

    [switch]$DryRun,

    [switch]$SkipTests,

    [string]$DevApiHost = 'http://10.0.2.2:8180',

    # ключ подписи (по умолчанию из окружения)
    [string]$Keystore = $env:MYAPP_KEYSTORE,
    [string]$KeystorePassword = $env:MYAPP_KEYSTORE_PASSWORD,
    [string]$KeyAlias = $env:MYAPP_KEY_ALIAS,

    # доступ к RuStore API
    [string]$KeyId = $env:RS_KEY_ID,
    [string]$PrivateKey = $env:RS_PRIVATE_KEY,

    [string]$PackageName = 'com.alternadv.vedhelper',
    [string]$AppName = 'Помощник ВЭД',
    [string]$ShortDescription = 'Помощник для участников внешнеэкономической деятельности',
    [string]$AgeLegal = '0+',
    [string[]]$Categories = @('business', 'tools'),

    [string]$ApiBase = 'https://public-api-m.rustore.ru'
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

# консоль Windows по умолчанию cp866/cp1251 — иначе кириллица в выводе и в логах ломается
try {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
    $OutputEncoding = [System.Text.Encoding]::UTF8
} catch { }

# --------------------------------------------------------------------------- вывод

function Write-Step([string]$Text) { Write-Host "`n==> $Text" -ForegroundColor Cyan }
function Write-Ok([string]$Text)   { Write-Host "    OK  $Text" -ForegroundColor Green }
function Write-Warn2([string]$Text){ Write-Host "    !   $Text" -ForegroundColor Yellow }
function Write-Info([string]$Text) { Write-Host "        $Text" -ForegroundColor Gray }
# Исходное содержимое app/build.gradle.kts до подъёма версии. Пока оно не $null, любой
# провал (Fail или необработанное исключение) возвращает файл как был — иначе после упавшей
# сборки в рабочей копии остаётся поднятая версия, и следующий запуск поднимает её ещё раз.
$script:GradleBackup = $null

function Restore-GradleVersion {
    if ($null -eq $script:GradleBackup) { return }
    try {
        [System.IO.File]::WriteAllBytes($GradleFile, $script:GradleBackup)
        Write-Host "    !   версия в app/build.gradle.kts возвращена" -ForegroundColor Yellow
    }
    catch {
        Write-Host "    !   не удалось вернуть app/build.gradle.kts: $($_.Exception.Message)" -ForegroundColor Red
    }
    $script:GradleBackup = $null
}

function Fail([string]$Text) {
    Write-Host "`nОШИБКА: $Text" -ForegroundColor Red
    Restore-GradleVersion
    exit 1
}

# необработанное исключение (ErrorActionPreference=Stop) — тоже откатываем версию
trap {
    Write-Host "`nОШИБКА: $($_.Exception.Message)" -ForegroundColor Red
    Restore-GradleVersion
    exit 1
}

# ------------------------------------------------------------------- пути и утилиты

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

$GradleFile = Join-Path $ScriptDir 'app/build.gradle.kts'
$Gradlew = if ($IsWindows -or $PSVersionTable.PSVersion.Major -lt 6) { '.\gradlew.bat' } else { './gradlew' }
$AabPath = Join-Path $ScriptDir 'app/build/outputs/bundle/release/app-release.aab'

function Get-GradleVersion {
    if (-not (Test-Path $GradleFile)) { Fail "не найден $GradleFile" }

    $text = Get-Content -Raw -Encoding UTF8 $GradleFile
    $code = [regex]::Match($text, 'versionCode\s*=\s*(\d+)')
    $name = [regex]::Match($text, 'versionName\s*=\s*"([^"]+)"')

    if (-not $code.Success -or -not $name.Success) {
        Fail 'в app/build.gradle.kts не найдены versionCode / versionName'
    }

    [pscustomobject]@{ Code = [int]$code.Groups[1].Value; Name = $name.Groups[1].Value }
}

function Get-NextVersion([string]$Current, [string]$Kind) {
    if ($Kind -eq 'none') { return $Current }

    $parts = $Current.Split('.')
    while ($parts.Count -lt 3) { $parts += '0' }

    $major = [int]$parts[0]; $minor = [int]$parts[1]; $patch = [int]$parts[2]

    switch ($Kind) {
        'major' { $major++; $minor = 0; $patch = 0 }
        'minor' { $minor++; $patch = 0 }
        'patch' { $patch++ }
    }

    return "$major.$minor.$patch"
}

function Set-GradleVersion([int]$Code, [string]$Name) {
    $text = Get-Content -Raw -Encoding UTF8 $GradleFile

    $text = [regex]::Replace($text, 'versionCode\s*=\s*\d+', "versionCode = $Code")
    $text = [regex]::Replace($text, 'versionName\s*=\s*"[^"]+"', "versionName = `"$Name`"")

    # файл в UTF-8 без BOM — иначе Gradle не любит BOM в build-скрипте
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($GradleFile, $text, $utf8)
}

function Invoke-Gradle([string[]]$Tasks) {
    $args = $Tasks + @("--console=plain", "-PdevApiHost=$DevApiHost")
    Write-Info ("$Gradlew " + ($args -join ' '))

    & $Gradlew @args
    if ($LASTEXITCODE -ne 0) { Fail "gradle завершился с кодом $LASTEXITCODE" }
}

# ------------------------------------------------------------- подпись для RuStore

# Запуск openssl без сюрпризов PowerShell 5.1: stderr нативной программы не должен
# становиться исключением (при ErrorActionPreference=Stop это ломает вызов).
function Invoke-OpenSsl([string]$Exe, [string[]]$Arguments) {
    $prev = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $output = & $Exe @Arguments 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "openssl $($Arguments -join ' ') завершился с кодом $LASTEXITCODE : $output"
        }
        return $output
    }
    finally {
        $ErrorActionPreference = $prev
    }
}

# HTTP-запрос к RuStore API с разбором тела ошибки: сервис кладёт {"code","message"}
# даже при 4xx/5xx, поэтому сообщение «(400) Bad Request» без тела бесполезно.
function Invoke-RuStoreJson {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        [string]$Body
    )

    $request = @{
        Method  = $Method
        Uri     = $Uri
        Headers = $Headers
    }
    if ($Body) {
        $request.ContentType = 'application/json'
        $request.Body = $Body
    }

    try {
        return Invoke-RestMethod @request
    }
    catch {
        $status = $null
        $text = $null

        $response = $_.Exception.Response
        if ($response) {
            try { $status = [int]$response.StatusCode } catch { }
            try {
                $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
                $text = $reader.ReadToEnd()
                $reader.Dispose()
            }
            catch { }
        }

        if (-not $text -and $_.ErrorDetails -and $_.ErrorDetails.Message) {
            $text = $_.ErrorDetails.Message
        }

        $detail = $_.Exception.Message
        if ($text) {
            try {
                $json = $text | ConvertFrom-Json
                if ($json.message) { $detail = "$($json.code): $($json.message)" }
                else { $detail = $text }
            }
            catch { $detail = $text }
        }

        $label = if ($status) { "HTTP $status" } else { 'сеть' }
        Fail "$label — $detail`n        запрос: $Method $Uri"
    }
}

function Get-RsaSignatureBase64([string]$Data, [string]$PrivateKeyBase64) {
    $keyBytes = [Convert]::FromBase64String($PrivateKeyBase64)
    $dataBytes = [System.Text.Encoding]::UTF8.GetBytes($Data)

    # PowerShell 7+ (.NET Core): PKCS#8 разбирается штатно, без внешних утилит
    try {
        $rsa = [System.Security.Cryptography.RSA]::Create()
        $method = $rsa.GetType().GetMethod('ImportPkcs8PrivateKey')
        if ($method) {
            $rsa.ImportPkcs8PrivateKey($keyBytes, [ref]0)
            $sig = $rsa.SignData($dataBytes, [System.Security.Cryptography.HashAlgorithmName]::SHA512,
                                 [System.Security.Cryptography.RSASignaturePadding]::Pkcs1)
            $rsa.Dispose()
            return [Convert]::ToBase64String($sig)
        }
        $rsa.Dispose()
    }
    catch {
        # упадём в openssl — там ошибка будет понятнее
    }

    # PowerShell 5.1 (.NET Framework): ImportPkcs8PrivateKey отсутствует — подписываем openssl.
    # Работаем только через файлы: stdout нативной программы в cp866, бинарь через него не гоняем.
    $openssl = Get-Command openssl -ErrorAction SilentlyContinue
    if (-not $openssl) {
        Fail @'
не удалось подписать запрос: в PowerShell 5.1 нет ImportPkcs8PrivateKey, а openssl не найден.
   Варианты: запустить в PowerShell 7+ (pwsh) либо установить openssl
   (идёт вместе с Git for Windows: C:\Program Files\Git\usr\bin\openssl.exe).
'@
    }

    $dir = Join-Path ([System.IO.Path]::GetTempPath()) ("ved-sign-" + [guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Path $dir | Out-Null
    $keyFile = Join-Path $dir 'key.der'
    $pemFile = Join-Path $dir 'key.pem'
    $dataFile = Join-Path $dir 'data.bin'
    $sigFile = Join-Path $dir 'sig.bin'

    try {
        [System.IO.File]::WriteAllBytes($keyFile, $keyBytes)
        [System.IO.File]::WriteAllBytes($dataFile, $dataBytes)

        # OpenSSL 3 сам определяет DER/PEM
        Invoke-OpenSsl $openssl.Source @('rsa', '-in', $keyFile, '-out', $pemFile) | Out-Null
        Invoke-OpenSsl $openssl.Source @('dgst', '-sha512', '-sign', $pemFile, '-out', $sigFile, $dataFile) | Out-Null

        if (-not (Test-Path $sigFile)) { Fail 'openssl не создал файл подписи' }

        return [Convert]::ToBase64String([System.IO.File]::ReadAllBytes($sigFile))
    }
    finally {
        Remove-Item $dir -Recurse -Force -ErrorAction SilentlyContinue
    }
}

function Get-RuStoreToken {
    $timestamp = [DateTimeOffset]::Now.ToString('yyyy-MM-ddTHH:mm:ss.fffffffzzz')

    $signature = Get-RsaSignatureBase64 -Data ($KeyId + $timestamp) -PrivateKeyBase64 $PrivateKey

    $body = @{
        keyId     = $KeyId
        timestamp = $timestamp
        signature = $signature
    } | ConvertTo-Json -Compress

    Write-Info "POST $ApiBase/public/auth/"
    $response = Invoke-RuStoreJson -Method Post -Uri "$ApiBase/public/auth/" -Body $body

    if ($response.code -ne 'OK' -or -not $response.body.jwe) {
        Fail "RuStore вернул ошибку авторизации: $($response.code) $($response.message)"
    }

    Write-Ok "токен получен (ttl $($response.body.ttl) c)"
    return $response.body.jwe
}

function New-VersionDraft([string]$Token, [string]$WhatsNew) {
    $uri = "$ApiBase/public/v1/application/$PackageName/version"

    $payload = @{
        appName          = $AppName
        appType          = 'MAIN'
        categories       = $Categories
        ageLegal         = $AgeLegal
        shortDescription = $ShortDescription
        whatsNew         = $WhatsNew
        publishType      = if ($Instant) { 'INSTANTLY' } else { 'MANUAL' }
    } | ConvertTo-Json -Compress

    Write-Info "POST $uri"
    $response = Invoke-RuStoreJson -Method Post -Uri $uri `
        -Headers @{ 'Public-Token' = $Token } -Body $payload

    if ($response.code -ne 'OK') {
        Fail "RuStore вернул ошибку при создании черновика: $($response.code) $($response.message)"
    }

    Write-Ok "черновик создан, versionId = $($response.body)"
    return $response.body
}

function Send-AabFile([string]$Token, [string]$VersionId, [string]$File) {
    if (-not (Test-Path $File)) { Fail "не найден AAB: $File" }

    $sizeMb = [math]::Round((Get-Item $File).Length / 1MB, 1)
    $uri = "$ApiBase/public/v1/application/$PackageName/version/$VersionId/aab"
    Write-Info "POST $uri (файл $sizeMb МБ)"

    Add-Type -AssemblyName System.Net.Http

    $http = New-Object System.Net.Http.HttpClient
    $http.Timeout = [TimeSpan]::FromMinutes(15)
    try {
        $content = New-Object System.Net.Http.MultipartFormDataContent
        $bytes = [System.IO.File]::ReadAllBytes($File)
        $fileContent = New-Object System.Net.Http.ByteArrayContent($bytes)
        $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse('application/octet-stream')
        $content.Add($fileContent, 'file', [System.IO.Path]::GetFileName($File))
        $http.DefaultRequestHeaders.Add('Public-Token', $Token)

        $response = $http.PostAsync($uri, $content).GetAwaiter().GetResult()
        $text = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()

        if (-not $response.IsSuccessStatusCode) {
            Fail "загрузка AAB не удалась (HTTP $([int]$response.StatusCode)): $text"
        }

        try { $json = $text | ConvertFrom-Json } catch { Fail "неожиданный ответ на загрузку AAB: $text" }
        if ($json.code -ne 'OK') {
            Fail "RuStore вернул ошибку при загрузке AAB: $($json.code) $($json.message)"
        }

        Write-Ok 'AAB загружен'
    }
    finally {
        $http.Dispose()
    }
}

function Send-ToModeration([string]$Token, [string]$VersionId) {
    $priority = if ($Instant) { 1 } else { 0 }
    $uri = "$ApiBase/public/v1/application/$PackageName/version/$VersionId/commit?priorityUpdate=$priority"

    Write-Info "POST $uri"
    $response = Invoke-RuStoreJson -Method Post -Uri $uri -Headers @{ 'Public-Token' = $Token }

    if ($response.code -ne 'OK') {
        Fail "RuStore вернул ошибку при отправке на модерацию: $($response.code) $($response.message)"
    }

    Write-Ok 'черновик отправлен на модерацию'
}

# ---------------------------------------------------------------- подготовка релиза

# Android SDK: Gradle ищет его в local.properties или ANDROID_HOME. Без этого сборка падает
# невнятным «SDK location not found» — проверяем заранее.
function Assert-AndroidSdk {
    if ($env:ANDROID_HOME -or $env:ANDROID_SDK_ROOT) { return }

    $localProps = Join-Path $ScriptDir 'local.properties'
    if (Test-Path $localProps) {
        $sdkDir = (Get-Content $localProps | Where-Object { $_ -match '^sdk\.dir=' }) -replace '^sdk\.dir=', ''
        if ($sdkDir -and (Test-Path $sdkDir)) { return }
    }

    $default = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
    if (Test-Path $default) {
        $utf8 = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($localProps, "sdk.dir=" + $default.Replace([char]92, [char]47) + "`n", $utf8)
        Write-Info "local.properties создан: $default"
        return
    }

    Fail @'
не найден Android SDK. Варианты:
   • задать переменную окружения ANDROID_HOME=F:\path\to\Android\Sdk
   • создать local.properties со строкой sdk.dir=F:/path/to/Android/Sdk
'@
}

Assert-AndroidSdk

Write-Host "`n=== VedHelper → RuStore ===" -ForegroundColor White
Write-Info "пакет: $PackageName"

$current = Get-GradleVersion
$newName = if ($Version) { $Version } else { Get-NextVersion -Current $current.Name -Kind $Bump }
$newCode = $current.Code + 1

if ($Bump -eq 'none' -and -not $Version) {
    $newName = $current.Name
    $newCode = $current.Code
}

Write-Step '1. Версия'
Write-Info "versionName: $($current.Name) → $newName"
Write-Info "versionCode: $($current.Code) → $newCode"

if ($DryRun) {
    Write-Step 'DryRun: дальше ничего не выполняем'
    Write-Info 'было бы: bump → tests → bundleRelease → publish'
    exit 0
}

if ($newName -eq $current.Name -and $newCode -eq $current.Code) {
    Write-Warn2 'версия не изменилась'
}

$script:GradleBackup = [System.IO.File]::ReadAllBytes($GradleFile)
Set-GradleVersion -Code $newCode -Name $newName
Write-Ok "app/build.gradle.kts обновлён (при провале тестов/сборки/публикации вернётся)"

# ------------------------------------------------------------------------ сборка

Write-Step '2. Тесты'
if ($SkipTests) {
    Write-Warn2 'пропущены (-SkipTests)'
}
else {
    Invoke-Gradle @('testDebugUnitTest')
    $results = Get-ChildItem -Path 'app/build/test-results/testDebugUnitTest' -Filter '*.xml' -ErrorAction SilentlyContinue
    $total = 0; $failed = 0
    foreach ($file in $results) {
        [xml]$xml = Get-Content -Raw $file.FullName
        $total += [int]$xml.testsuite.tests
        $failed += [int]$xml.testsuite.failures + [int]$xml.testsuite.errors
    }
    if ($failed -gt 0) { Fail "тесты провалены: $failed из $total" }
    Write-Ok "тестов пройдено: $total"
}

Write-Step '3. Сборка AAB'
if (-not $Keystore) { Fail 'не задан ключ подписи: MYAPP_KEYSTORE / -Keystore' }
if (-not (Test-Path $Keystore)) { Fail "файл ключа не найден: $Keystore" }

$env:MYAPP_KEYSTORE = (Resolve-Path $Keystore).Path
$env:MYAPP_KEYSTORE_PASSWORD = $KeystorePassword
$env:MYAPP_KEY_ALIAS = $KeyAlias

Invoke-Gradle @('bundleRelease', '-x', 'lintVitalAnalyzeRelease', '-x', 'lintVitalRelease')

if (-not (Test-Path $AabPath)) { Fail "AAB не найден после сборки: $AabPath" }
$sizeMb = [math]::Round((Get-Item $AabPath).Length / 1MB, 1)
Write-Ok "AAB собран: $AabPath ($sizeMb МБ)"

if ($BuildOnly) {
    $script:GradleBackup = $null   # AAB собран с этой версией — оставляем её
    Write-Step 'Готово (BuildOnly): AAB собран, публикация пропущена'
    Write-Info "версия $newName ($newCode)"
    exit 0
}

# ---------------------------------------------------------------- публикация

Write-Step '4. Авторизация в RuStore API'
if (-not $KeyId -or -not $PrivateKey) {
    Fail @'
не заданы RS_KEY_ID / RS_PRIVATE_KEY (или -KeyId / -PrivateKey).
   Для локальной сборки без публикации используйте -BuildOnly.
   Ключи выдаются в RuStore Консоль → Настройки → API.
'@
}

$token = Get-RuStoreToken

Write-Step '5. Черновик версии'
Write-Info "что нового: $Message"
$versionId = New-VersionDraft -Token $token -WhatsNew $Message

Write-Step '6. Загрузка AAB'
Send-AabFile -Token $token -VersionId $versionId -File $AabPath
# AAB с этим versionCode уже в RuStore: откат дал бы повтор кода при следующем запуске
$script:GradleBackup = $null

if ($SendToModeration) {
    Write-Step '7. Отправка на модерацию'
    Send-ToModeration -Token $token -VersionId $versionId
    Write-Host "`nГотово: версия $newName ($newCode) отправлена на модерацию." -ForegroundColor Green
}
else {
    Write-Host "`nГотово: черновик версии $newName ($newCode) создан и AAB загружен." -ForegroundColor Green
    Write-Info "Отправить на модерацию: повторите с -SendToModeration либо через RuStore Консоль."
}
