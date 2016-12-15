@ECHO OFF

REM This is how you can compile java files 
dir  tmp /s /B  *.java | findstr /i /v "Scaffolding" > sources.txt 
javac -sourcepath . -cp Scaffolding\antlr-4.5.3-complete.jar;tmp  -d tmp @sources.txt 
del sources.txt 

REM Now let's run all that stuff 
cd tmp
CALL java -cp ../Scaffolding/antlr-4.5.3-complete.jar;. Test TestData.class
cd ..