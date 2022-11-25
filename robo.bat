@echo OFF
REM This is used to run the various tools i've created for making robots

goto MAIN

:compile
python tools/compile.py
goto end

:package
python tools/package.py
goto end

:filestats
python tools/filestats.py
goto end

:clean
python tools/clean.py
goto end

:usage
echo Usage: robo.bat ^<tool^>
echo Tools:
echo   ^> compile   ^| Compile the robot
echo   ^> package   ^| Package the robot into a jar file
echo   ^> clean     ^| Remove class files
echo   ^> filestats ^| Print out the file statistics
goto end

:MAIN
set tool=%1

if "%tool%" == "compile" goto compile
if "%tool%" == "package" goto package
if "%tool%" == "clean" goto clean
if "%tool%" == "filestats" goto filestats

:end