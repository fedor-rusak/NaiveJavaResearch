# Thought dump

Document describing my thought during different stages of JVM analysis and feature implementation.

Messages are added in reversed order.

## 16.01.2017 — Object instantiation!

Even after reading some bytecode I was sure that object creation means calling a special init method. But as JNI mechanism proves that reality is a bit trickier. Fact of allocating memory in heap is performed not in some initialization method but in special step called newObject in JNI case or by calling operation code 0xbb (new) from java bytecode.

So the thing that is called constructor in java is just another method that describes initial value setup and optional logic steps.

## 16.01.2017 — "Switch case" is monstrous!

I always thought that switch case is compiled into bunch of if cases. And I was wrong! It has its own very special bytecode implementation! First version is *tableswitch* which is used when indices can be arranged in array without too much tricks (like fake indices) and its bonus is that switch value is treated is form of index that points to code... like it is supposed to be some optimization?

Second one is for situations when indices can't be arranged in array and instead we have an array of pairs like key-offset so that we go linearly and search with switch key value in it. It is like optimized group of if statements with integer comparisons.

All this information is quite boring yet if we remember two things about java language: bytecode should be as close to source code as possible and no facility as macro should be used. And that is how we ended in situtation when switch case can work only with integer values and we have to wait for 7 major releases to make it work at least with strings...

## 11.01.2017 — Class loading is tricky

Well if you try to just call Reflection API to load some class and call a method. That is fine and ok. But what if you want to make some sneaky servlet container that can dynamically load new applications? And they sure may have same files and classes? So what is java solution?

I would call it a pretty powerful hack. Because JVM behavior is class-based. So this interpreter requires class loading step before any execution. Then it is logical to provide mechanism for modifying this class loading step with custom logic!

In short java code is not tied to any class loader during compilation. So that code that run "inherits" class loader from class whose code is executed now. So if you load some Thread class with custom class loader then... its code will you custom loader for its execution!

Sorry for such a strange description but this approach is really weird. It feels like inside of java interpreter you may start another java interpreters using these loading mechanism. And it a huge over-engineering without easy-to-suggest usage. And this will definitely add complexity to JVM implementation.

## 30.12.2016 — Year comes to an end

It was a nice year for analyzing JVM internal mechanisms.

I have finished my experiment with invokedynamic and I can say it is extremely complicated and internet provides no useful examples about usage of this pretty interesting feature. It looks like brand new javascript engine for JVM called [Nashorn](http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html) uses this feature a lot.

This bytecode and feature was used for implementation of lambda expressions in JVM. And actually it is fine and techy.

What I really dislike is a fact that there are no good ways to unload classes if you decide to create new classes in runtime. It is complicated process of managing classloaders or writing interpreters on top of interpretes and it is a real shame because with this problem solved JVM platform would be a much better experiment ground for dynamic languages.

## 29.12.2016 — One step closer to use of invokedynamic

So first of all I need some method that can ba called a Bootstrap. I need a way to emit it through my program. Done.

Now I have to change some parts of it like one testMethod to call invokedynamic instead of invokestatic.

And of course I have to add some constants and attributes to register my bootstrap method.

## 28.12.2016 — Can't stop digging

It is really fascinating just to try recreate some facility to generate class files. Of course it started as a way to make some examples with invokedynamic but now it is also interesting emitter experiment! Nothing more here :) for a moment. It is already a huge commit.

## 26.12.2016 — What the hell is invokedynamic

Last piece of class file parsing with bootstrap methods point to a strange invokedynamic bytecode that somehow allows some flexibility with dispatching method calls. Quick search on iternet shows that it was supposed to make implementing dynamic languages for JVM easier.

Yet it is not supported by official java compiler and most of the articles use the simplest examples without providing any useful knowledge for diving into this domain. Some github tickets even show that this feature was slowing down JRuby in Java 8.

So if I have no other choice but to edit bytecode by hand. Then I will facilitate this process a bit and first step was just adding space to a huge string representing hex values from simple helllo-world class file. Libraries like ASM should be ok for this but they *hide* to much.

## 26.12.2016 — Antlr4 allows no cyclic references

Yeah I get it. This tool is great for fast and dirty prototyping. It gives some feedback and is based on some woodoo ATN bullshit. And there is even some hack to allow sort of recursive rules to be used for parser generation.

But if you think that if you have sort of recursive structure then you can use this library well tough luck! It will crash randomly with strange and weird errors at runtime. But to my pleasure this recursive stuff is not important for a moment.

Yet a bit more rules to a grammar for parsing class file structures and adding last details is still not a big issue. Definitely a good library for this task.

## 23.12.2016 — See the invisible

So it was not that hard to get the attribute called RuntimeInvisibleAnnotations. I just added a local annotation into java class and after compilcation it appeared. What a strange way to make this annotation unaccessible through Reflection API.

As simple searching describes by default annotations are not visible in runtime yet the data about them is kept in class file structure. It is called Retention and its and additional Annotation that should be marked on your (Inception!) annotation in order for it to be visible at Runtime. Oh boy that is weird.

## 23.12.2016 — So I refactored a bit and WTF with default package

I just had implemented some pieces of glue code and some more lines and got the point when keeping everything in one class was hard to read and understand. So I changed a bit my build scripts and folder structure and now everything is smaller in lines yet bigger in quantity. And of course more import statements.

Still I want to point out that it makes no sense that I can't reference class from default package when writing class with certain package declaration. Maybe it helps with something like security or optimization or hell knows what.

And my second thought it that maybe it gives more predictability with loading dependencies but NO. If you reference default class what is difference from packaged class? On the level of bytecode it is just a constant showing path to class. And I am sure bytecode can be tweaked and JVM would handle it properly.

After some research I came to conclusion that it was designed in such manner to minimize problems with name clashing when integrating multiple libraries. If you have to add package name to a class name chance of finding two classes with same details is lower than if you had to specify only name. Ok, some sort of best practice injected in language specification.

## 22.12.2016 — Deprecated handling is deprecated?!

Nothing really interesting here but small back-compatibility thing. Deprecated annotation was introduced in java before the concept of annotations. And that is why on level of class file it is ok that method has *attribute* "Deprecated". But in newer version compiler generates attribute called *RuntimeVisibleAnnotations* and "Deprecated" is treated just like another annotation.

Technically speaking it means that newer version of JVM should run old java bytecode without problems. Even without recompiling. For the price of keeping complexity in specification and implementation. Business value of stability over technical sense of keeping things simple. Sigh...

## 22.12.2016 — ANTLRv4 is slow (for me) [fixed!!!]

It is not horribly slow like hours or days of work time. But for my simple grammar with about 60 rules parsing of 660 bytes class file takes about 13 seconds. Of course I may have a slow computer or ram but it seems ok for everything else on my computer like IntellijIdea or Firefox Browser.

I assume that semantic predicates are responsible for some slow down plus I use library in a bit wrong way so maybe some refactoring may help me to sped up things significantly. Blast. I just went to internet and tried to find out why it happens in such way and I have something to show.

So I tried to use jVisualVM for profiling. And I while it is obvious that some ATN stuff takes too long I also can see some class with names of parser rules and id toes not make to much sense. Except the fact that the more rules you add the slower parsing gets.

After some non-intuitive research I have found that there is some prediction mode involved in parsing process. And for some weird reason using the one called SLL results in like 100x speed increase! And to my shame this was the last paragraph of official FAQ that I have skimped in search for useful information. Great documentation!

## 22.12.2016 — So when you want to work with parse tree

You have two options... well actually one. If in previous version of this library you would be able to embed some tree modifying instructions. In current state you can only generate parse tree and then use existing APIs to modify it or create a new one by hand.

Well there is great helper functions like subset of XPath and some tree pattern matching functionality buuuuut I have problems with later one. While XPath is parsing rule names from query to walk down the tree this sneaky tree pattern matching requieres a Lexer which uses LexerATNSimulator with all DFA rules and blabla. How I know that? Because I am not using Lexer generated by ANTLR because I need simple bytes. But When I tried creating this tree pattern matchers I found that I still need whole objects and conventions of generated Lexer.

It is not a deal breaker yet an important design decision of library author. That these lexer and parser are supposed to be used together even if they can be generated in separate way.

## 20.12.2016 — So can partially-real JVM be written in a weekend?

I guess it is not. Well you can examine different bits and pieces. But each of them requires its own weekend and state of mind to accomplish. So I guess it can be said that it is hard for most of programmers with real-life things like family, leisure, friends and health.

Yet is not it exciting to get under the hood of widely adopted technology to see that it is quite strange and odd in the way it works. Just to imagine what were they thinking when they were doing it? What were thinking guys who thought that it is worth to use for servers, mobile devices and even credit cards?

So maybe this knowledge even be harmful for people who want to think about programming as a process of solving business problems. Because it reminds you about computers and resource management and arithmetic overflow and number representation. It can be nice if you plan to write some tools or embed language in your existing application. It can be really nice...

## 20.12.2016 — Complexity is bad?

Some things are hard just because they are somehow configurable and general and multi-purpose. And doing such things sucks hard. Because you have to make happy everyone! Except the guys who will implement all these crazy features you just sold to your customers.

And let's be honest I have got nothing against complex yet sophisticated tools. That help you achieve somethibg awesome. Like utilize specialized hardware to sped up simulation of liquids and etc. But when you see a complexity that is just pile like a garbage one small piece after another piece it starts to bother you. Like a lot.

Of course this is just my opinion and you may not care about the fact that java code is compiled in such a manner that you can have invisible annotation with crazy-ass type element value pairs. Or that code attribute of your method element contains some woodoo about frame relations for hmm.. sped up in class loading?

All this open source solution looks like some number of great ideas which are cluttered in some compatibility ambitions plus some weird choices about tight control on really strange scripting language (bytecode). And I still think that good ideas are worth knowing and using even if they only can be rediscovered by your all new implementation or reverse-enginering from analysis of existing codebase.

## 19.12.2016 — Is ANTLR stuff great?

After writing some addditional predicates and actions (code parts) in java. Which combines $ symbols with strange blocks like locals, returns and parameter listings. I am starting to miss for simple java code highlighting. And it can be done but only through some complexity that tries to understand this mish-mash of grammar-syntax plus my imperative abomination.

And while there is some abstraction of grammar and predicates it is still leaky. If I write some exception throwing then there are java compiler complaints. Because in generated file particular piece of code is unreachable after mandatory exception and I have use stupid workarounds. And working with results of parsing during parsing process requires to work with java objects using clumsy dot notation from context and then getting text to parse. Well I hope you get the idea.

Yet there is some interesting concept behind this. When you trying to script with your code and some autogenerated function some sort of state machine that would adapt her behaviour to a stream of bytes in order to give you some sort of organized structure. It is by no means perfect yet it forces you to add constants and names from documentation that partially make it closer to specification and not to a simple java program.

## 19.12.2016 — Global offensive?

I do like to make things easy and in some way dumb. If you want function that should return you String from bytes? Just do it. Then you have step where you need result from really distant step and you may think... what is the easiest way? Go for global variable and be cool? WRONG!

It may sound that there is no problem in one or two variables. Because you don't need to pass them as variables, you can call and change them from whatever you want. And that is exactly the case. Even with getter/setter bullshit you will have a spaghetti of code that relies on some side-effects from some weird step that you may not even know about.

Don't use global just for economy of some words or 5 minutes of your time. Because they will hurt readability and most of all predictability of your code. You will lose the most important point of writing parser through some specialized syntax — readily available dependencies between steps and structures.

## 19.12.2016 — This field attribute annotation elemen-value pair arrrrgh

Some parts of this approach I just mind bending you know. That each field in class has its type and value. No problems. Then it got some interesting thing called attributes where it can have things like constantValue, deprecated, Synthetic (?), Signature (???), runtimeInvisibleAnnotation (?????) wait what?

And of course each field with attribute annotations may have multiple annotation and you know what? Each annotation may have an additional number of element-value pairs! Each of them requiring type! And combine this with fucked up sceme of arrays of arrays plus cryptic naming of primitive type (J is long!) and you have a joy in front of you.

Hated to do this stuff in code. Hated to do it with grammar approach. Yet the latter looks a bit better? Maybe?

## 16.12.2016 — This specification is plain beautiful

So today I have finished part of parser that is responsible for constant pool. This structure that looks like serialized array but with one stupid twist. There are constants that should be considered to use TWO indices of array for their storage.

Here is the official [link](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.5). When documentation has such words: *In retrospect, making 8-byte constants take two constant pool entries was a poor choice.* — you know that you see some mistakes of the past.

## 15.12.2016 — Git ignores letter case...

I just found out when tried to create link from one markdown file to another. That if you change case of letters in name of already tracked folder or file then... git does not give a damn about it!

## 15.12.2016 — Time to write better documentation

So today as I was planning to work a bit more on class file parser or read documentation about method and variable resolution during java bytecode execution. And I thought that maybe I should write something about my reasearch. So that later when I leave it for a while there will be a change for older-me to remember the details of this project.

I decided that it can be interested to keep some diary about the things I found during my naive attempt to write a small JVM using java language. Even after some weeks or months of thinking about this idea — writing your own JVM. I think it is challenging yet it should be possible for any developer. If only documentation and explanation of it was good enough.

My personal interest lies in my dreams about java being great technology that makes something awesome! While at work I can see that it just some tool that can be used via strange syntax and for whatever reason people concentrate their attention on thing that I find truly boring. Like some blotware API conventions or troubles with reasoning about inheritance behaviour. While truly interesting stuff like DSL implementation and native library usage is never mentioned.

And I also like javascript language a lot and was really curious why some VM developers say that making Javascript interpreter really fast is right impossible. Or why some VMs are stack-based while others are register-based and does it really matter? And of course it some kind of technical desire to implement some really hard technical project. So why not try implementing parts of JVM?
