@echo off
rem Wrapper for deploy-rustore.ps1 - run by double click or from cmd.
rem All arguments are passed to PowerShell as is.
rem
rem Examples:
rem   deploy-rustore.bat -DryRun
rem   deploy-rustore.bat -Message "Fixed calc" -SendToModeration
rem   deploy-rustore.bat -Version 1.3.0 -BuildOnly
rem
rem NOTE: keep this file ASCII-only, cmd.exe uses cp866 and mangles UTF-8 text.

setlocal
set "SCRIPT=%~dp0deploy-rustore.ps1"
set "PS=powershell"

where pwsh >nul 2>&1
if %ERRORLEVEL%==0 set "PS=pwsh"

%PS% -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*

set "CODE=%ERRORLEVEL%"
if not "%CODE%"=="0" (
    echo.
    echo Script exited with code %CODE%
)

rem pause only when the window was opened by double click (cmd /c <script>), not in CI
if "%~1"=="" echo %cmdcmdline% | findstr /i /c:"/c" >nul && pause

exit /b %CODE%
