del *.class

IF EXIST result rmdir /s /q result


javac -cp .;class_json_api.jar FileLister.java


mkdir result

CALL node parseTheseClasses ../SourceAggregation/lib result