REM First we should remove temporary folder 
IF EXIST tmp rmdir /s /q tmp 

REM Then we should generate java source files from grammar file 
cd Scaffolding
CALL antlr4.bat ..\ClassFileParser.g4 -o ../tmp/omittedWTF
cd ..

REM This is how you can compile java files 
dir /s /B tmp\*.java > sources.txt 
dir /s /B src\*.java >> sources.txt 
javac -sourcepath . -cp Scaffolding\antlr-4.5.3-complete.jar;tmp  -d tmp @sources.txt 
del sources.txt 

REM Now let's run all that stuff 
cd tmp
CALL java -cp ../Scaffolding/antlr-4.5.3-complete.jar;. Test TestData.class
cd ..