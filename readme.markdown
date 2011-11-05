# Sourcedown

Markdown + Source code = Documentation

A [docco][1]-style documentation tool written in [Scala][2].

Why rebuild docco?

- It's actually kind of hard to install; node.js doesn't install against my
  company's proxy, for example.

- It doesn't handle block comments as of the current version.

This might seem crazy, but I just want a tool that can handle javaScript, Scala,
Java, and possibly C++ without a lot of extreme effort. And I've written a 
[markdown converter][3].

Other benefits:

1. Plugin integration into maven-style project management systems, which are 
   JVM-oriented.


[1]: http://jashkenas.github.com/docco/
[2]: http://scala-lang.org
[3]: http://tristanhunt.com/projects/knockoff