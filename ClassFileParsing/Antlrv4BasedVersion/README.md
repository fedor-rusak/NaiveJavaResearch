# ANTLR v4 version of class file parser

Instead of hand coding all parsing here I describe grammar of structure plus procedures important for correct parsing.

This grammar is used by ANTLR to generate Parser class that can be later used for parsing and getting parse tree.

This is a work in progress but supporting smaller piece of code sounds easier at the moment than working with hand-coded procedure for analyzing tricky binary structure.

## Running

You have to options for running this code.

First one will trigger removal of all the temporary data and generation of java classes. Use this:

```
clean_generate_compile_run.bat
```

If you decide to add some code into generated classes by hand and run such version then use this:

```
compile_run.bat
```

In both cases information about constant pool, methods, major, minor version and etc must be sent to output.