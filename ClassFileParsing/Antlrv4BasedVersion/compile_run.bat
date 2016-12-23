@ECHO OFF

REM This is how you can compile java files 
dir /s /B tmp\*.java > sources.txt 
dir /s /B src\*.java >> sources.txt 
javac -sourcepath . -cp Scaffolding\antlr-4.5.3-complete.jar;tmp  -d tmp @sources.txt 
REM del sources.txt 

REM Now let's run all that stuff 
cd tmp
CALL java -cp ../Scaffolding/antlr-4.5.3-complete.jar;. Test TestData.class
cd ..