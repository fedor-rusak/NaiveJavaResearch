# So you want to make a small JVM?

The good news is that specifications and implementation are free and open to analyse and recreate. Even if they are a bit complicated sometimes.

These things make writing simple intepreter for java bytecode a bit time-consuming:

 * standard JVM statup requires loading and instantiation of about 100 classes
 * arrays are lacking java class abd must be implemented as sort of hack (sad!)
 * lack of type information in runtime means that most bytecode commands like return, add, divide, compare and etc. have versions for each primitive type (float, integer, long, double)
 * classes from standard java library rely heavily on OS specific functionality implementation and call it through JNI which must be handled properly
 * still unkwnown feature to me is usage of locking (synchronize) for java bytecode
 * special mechanism must be implemented for handling exceptions
 * JVM bytecode is stack-based in nature so it requires mechanism to manipulate data about frames, their content and relation