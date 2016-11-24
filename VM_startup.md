# OpenJDK 7 startup logic

I had less then good understanding of steps involved in startup of official HotSpot JVM.

I have found some code parts that show some C/C++/Java flow involved in loading of main class and calling __main__ method.

These are useful github links if you want to see it by yourself (Windows specific):

  1. [C main method for java.exe](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/bin/main.c#L96)
  2. [Some confusing method call](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/bin/main.c#L106)
  3. [Some Java Launcher I(interface)?](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/bin/java.c#L171)
  4. [Extremely interesting function name](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/bin/java.c#L246)
  5. [Next important step. Init!](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/bin/java.c#L297)
  6. [That is awkward...](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/windows/bin/java_md.c#L1332)
  7. [Land of Threads begin!](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/windows/bin/java_md.c#L1337)
  8. [Still digging...](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/bin/java.c#L1824)
  9. [What with that zero?](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/bin/java.c#L1854)
  10. [BLock... wtf?](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/windows/bin/java_md.c#L1111)
  11. [That is from process.h from Windows stuff](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/windows/bin/java_md.c#L1126)
  12. [C(urrent) thread will do this.](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/windows/bin/java_md.c#L1176)
  10. [C thread. Implementation in AWT???](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/windows/native/sun/windows/awt_Toolkit.h#L284)
  11. [N(ew) thread. Does this.](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/bin/java.c#L339)
  12. [N thread. Our main method!](https://github.com/openjdk-mirror/jdk7u-jdk/blob/f4d80957e89a19a29bb9f9807d2a28351ed7f7df/src/share/bin/java.c#L443)