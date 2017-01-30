del *.class

IF EXIST result rmdir /s /q result


javac -cp .;class_json_api.jar FileLister.java

java -cp .;class_json_api.jar;../ClassFileParsing/Antlrv4BasedVersion/Scaffolding/antlr-4.5.3-complete.jar FileLister ../ClassFileParsing/Antlrv4BasedVersion/Scaffolding/antlr-4.5.3-complete.jar