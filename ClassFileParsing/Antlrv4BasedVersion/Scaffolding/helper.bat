@ECHO OFF

REM We count number of arguments
set argumentCount=0
for %%x in (%*) do Set /A argumentCount+=1


if %argumentCount% NEQ 2 (GOTO:badEnding) ELSE (GOTO:goodEnding)



SETLOCAL
SET "sourcedir=c:\sourcedir"
FOR /f "delims=" %%a IN (
 'dir /b /a-d "%sourcedir%\*" '
 ) DO (
 SET "name=%%a"
 CALL :transform
)

GOTO:EOF

:transform
ECHO REN "%sourcedir%\%name%" "%name:~16,9%%name:~0,16%%name:~25%"
GOTO:EOF

:badEnding
echo Number of arguments is %argumentCount%.
echo 2 parameters were expected (folderName grammarName).
GOTO:EOF

:goodEnding
SET folderName=%1
SET grammarName=%2

echo Folder for new project is %folderName%
echo File for new grammar is %grammarName%


if EXIST %1 GOTO:folderExists


mkdir %1


copy helperTemplates\TestData.txt %1\TestData.txt


REM cd helperTemplates

type NUL > ..\%1\do_stuff.bat

setlocal enabledelayedexpansion

for /f "delims=" %%x in ('type helperTemplates\grammarName.g4') do (
	set somes=%%x
	set somes=!somes:grammarName=%grammarName%!
	echo !somes!
	echo !somes! >> %1\%2.g4
)

for /f "delims=" %%x in ('type helperTemplates\do_stuff.bat') do (
	set somes=%%x
	set somes=!somes:folderName=%folderName%!
	set somes=!somes:grammarName=%grammarName%!
	echo !somes!
	echo !somes! >> %1\do_stuff.bat
)

for /f "delims=" %%x in ('type helperTemplates\Test.java') do (
	set somes=%%x
	set somes=!somes:grammarName=%grammarName%!
	echo !somes!
	echo !somes! >> %1\Test.java
)

cd ..

GOTO:EOF


:folderExists
echo Folder %1 already exists!
GOTO:EOF