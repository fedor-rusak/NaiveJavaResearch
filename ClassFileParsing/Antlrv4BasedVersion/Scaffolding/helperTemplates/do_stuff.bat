@ECHO OFF

REM First we should remove temporary folder
IF EXIST tmp rmdir /s /q tmp

REM Then we should generate java source files from grammar file
cd ..
CALL antlr4.bat folderName\grammarName.g4 -o folderName\tmp
cd folderName

REM This is how you can compile java files
dir /s /B  *.java > sources.txt
javac -sourcepath . -cp ..\antlr-4.5.3-complete.jar; -d tmp\folderName @sources.txt

del sources.txt

REM Copy test data
copy TestData.txt tmp\folderName\TestData.txt

REM Now let's run all that stuff
cd tmp/folderName
CALL java -cp ../../../antlr-4.5.3-complete.jar;. Test TestData.txt
cd ../..