# Java class file parsing

Class file structure is a result of javac command which successfully compiled *.java file.

[JAVA SE 7 specification](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html) was used for analysing this step.

If you have installed JDK 7 then you should have tool called [javap](http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javap.html) which can give some reports from parsing class files.

## Hand-coded version

Working application that can parse classes of certain level of complexity. It is really hard to maintain and expand. More details [here](HandCodedVersion/README.md)

## ANTLR v4 version

It is still work in progress yet it looks much promising as basis for mechanism of parsing complex binary structures like class files. More details [here](Antlrv4BasedVersion/README.md).

It is based on cool library [ANTLR v4](https://github.com/antlr/antlr4) that is great open source project.