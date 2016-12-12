@ECHO OFF

REM This is how you can compile java files 
dir /s /B  *.java > sources.txt 
javac -sourcepath . -cp ..\antlr-4.5.3-complete.jar;javaClassParsing\tmp  -d tmp\javaClassParsing @sources.txt 
del sources.txt 

REM Now let's run all that stuff 
cd tmp/javaClassParsing 
CALL java -cp ../../../antlr-4.5.3-complete.jar;. Test TestData.class
cd ../.. 