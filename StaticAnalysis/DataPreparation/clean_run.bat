IF EXIST resultPrepare rmdir /s /q resultPrepare

mkdir resultPrepare

IF EXIST resultCompress rmdir /s /q resultCompress

mkdir resultCompress

IF EXIST resultLinking rmdir /s /q resultLinking

mkdir resultLinking

IF EXIST resultToUse rmdir /s /q resultToUse

mkdir resultToUse


CALL node callScriptOnFilesFromFolder prepare ../ClassParsing/result resultPrepare

CALL node callScriptOnFilesFromFolder compress resultPrepare resultCompress

CALL node linking resultCompress resultLinking

CALL node lastTouch resultLinking/miniData.json resultToUse/readyToAnalyzeData.json