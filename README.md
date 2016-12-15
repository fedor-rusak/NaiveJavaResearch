# NaiveJavaResearch

Java is a decent programming language. And it is executed via special monster called JVM.

I do know that this JVM is sort of interpreter for bytecode. But how does it work?

Practice is the best way to learn something new. So why not to try implement some of JVM parts on java?

Off course we are not going to do that using only our assumptions and ideas about real JVM implementation.

We are going to use official specifications, source code for OpenJDK 7 and tools provided with Oracle JDK 7.

## Technical requirements

My workstation is Windows 7 and build scripts for java applications will be batch scripts that should be called via cmd application.

## Specifications and source code

This is original [JAVA SE 7 specification](https://docs.oracle.com/javase/specs/jvms/se7/html/index.html) from Oracle site. We are not planning to implement the most bleeding edge features so 7 version should be enough.

Repositories of [OpenJDK 7](https://github.com/openjdk-mirror/jdk7u-jdk) and [OpenJDK HotSpot](https://github.com/openjdk-mirror/jdk7u-hotspot) will be extremely valuable for our research. To my shame I have not built these projects on Windows but it does not stop us from making our own implementation.

Oracle site is a bit hard to navigate so if you need [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html#jdk-7u80-oth-JPR) then go ahead and download it. If link is dead then you should try to search for archive page where you can get old distributions.

## Goal

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

Of course I have some plan for whole this project that may be updated after getting more experiences.

## Plan

So these steps are involved in work of JVM:

 * [startup](JVMStartup/README.md) (and resource preparation???)
 * [class loading](ClassLoading/README.md)
 * [class file parsing](ClassFileParsing/README.md)
 * [native function calling](NativeFunctionCalling/README.md)
 * [main method bytecode execution](BytecodeInterpeter/README.md)