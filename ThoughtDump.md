# Thought dump

Document describing my thought during different stages of JVM analysis and feature implementation.

Messages are added in reversed order.

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
