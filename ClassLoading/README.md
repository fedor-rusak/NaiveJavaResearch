# Class loading

It is a boring subject because there are numerous articles describing "ClassLoader Hierarchy" and they never give you details that may help you to implement this thing on your own.

From my current perspective all this hierarchy stuff is just some band-aid for the fact that once class was loaded then the only way to remove its version is by removing whole Classloader.

And whatever you do you classloader must call in the end a native method *load* that would verify and save it in internal JVM structures so that it later can be used for execution.

This step relies heavily on details from class file parsing. So I have to wait before I have a good class file structure and parser. With this components I could try to create a mechanism for reading class files and finding their dependencies.