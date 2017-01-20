# Naive Java Research

I thinkg programming is a craft of using programming tools for solving problems.

So the better you know your tools the better solutions you implement.

When you know Java syntax and had some experience with more-than-hello-world projects where to go next?

And this is my current moment and I decided to go technical and learn more about under-the-hood details.

## Benefits so far

I understand the structure and decisions behind class file structure. With all the constants, methods, interfaces and attributes. Technically speaking this is the only that can be expressed in bytecode without going native.

Better class file understanding leads to better understanding of bytecode specification and approach for its execution. Which is some odd mixture of stack and register usage.

I tried successfully to use invokedynamic bytecode from Java 7 and it was hard. Yet it is useful to know because it is used for lambda expressions in Java 8.

I would say I feel much bigger appreciation for this technology overall knowing the scope of problem that they tried to solve with this scripting, class-based, cross-platform oriented mess of a language.

All this knowledge can't be used for simple bugfix tasks or writing snippets of code for some frameworks. Yet it helps to understand the trade-offs of this tool, its differences and similarities to other scripting languages.

## Technical requirements

My workstation is Windows 7 and build scripts for java applications will be batch scripts that should be called via cmd application.

## Specifications and source code

This is original [JAVA SE 7 specification](https://docs.oracle.com/javase/specs/jvms/se7/html/index.html) from Oracle site. We are not planning to implement the most bleeding edge features so 7 version should be enough.

Repositories of [OpenJDK 7](https://github.com/openjdk-mirror/jdk7u-jdk) and [OpenJDK HotSpot](https://github.com/openjdk-mirror/jdk7u-hotspot) will be extremely valuable for our research.

Oracle site is a bit hard to navigate so if you need [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html#jdk-7u80-oth-JPR) then go ahead and download it. If link is dead then you should try to search for archive page where you can get old distributions.

## Goal

To have fun exploring great volumes of information about interpreters called *virtual machines*.

Make a java program that would similarly to "real" JVM execute HelloWorld example.

```java
public class HelloWorld {
	public static void main(String[] args) {
		System.out.println("Hello world!");
	}
}
```

## Approach

First of all I sat down and search for all the available information about feature or mechanism in JVM that I want to implement.

Then I try to get some practical results like scripts for getting useful reports or java classes with build scripts.

All the technical information is saved as files with documentation, code, scripts and my personal opinion about my findings is written to [thought dump](ThoughtDump.md).

Of course I have some plan for whole this project that may be updated after getting more experience.

## Plan

So these steps are involved in work of JVM:

 * [startup](JVMStartup/README.md) (and resource preparation???)
 * [class loading](ClassLoading/README.md)
 * [class file parsing](ClassFileParsing/README.md)
 * [native function calling](NativeFunctionCalling/README.md)
 * [main method bytecode execution](BytecodeInterpreter/README.md)