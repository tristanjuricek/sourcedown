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



## Personas

Joe is a web developer who has an application with some files in C, some in 
PHP, and a bit of JavaScript on the side. He's tired of having Yet Another Tool
put in front of him.

Steve works with Joe, but does some systems adminstration work. His scripts are
typically shell scripts, but thinks, "hey, I should be able to document my stuff
just like Joe's".

Art is the DBA that works with Joe. His stuff is a bunch of SQL scripts, but 
thought, "aw man I can't be left out".

In another company, Jenny is a Spring developer who has created the complete
front end and back end. Because her time is being spent mostly in templates and
javaScript these days, she's trying to find a way to keep the javaScript, 
velocity, and Java and SQL stuffed inside of XML in her system.



## Scenarios

How do people learn about Sourcedown? Blogs? Hacker News?

Joe checks the sourcedown.jar into a "tools" folder in his project. He then
just sets up a simple target in his makefile. Up pops the docs folder, and he 
adds a "publish" target to a FTP folder where apache is running. He makes a few
other edits to set up links to the source documentation, and is off and running.

Joe tells Steve and Art how to set up projects of their own, and they copy the
sourcedown.jar to their own projects and copy and paste the make command. Both
are then.

Jenny first checks for the maven plugin. Realizing that it hasn't been done
yet, she saves the sourcedown jar file locally. 

The company for Jenny complains about the fact that there's Javadoc and this
just doesn't look like what people expect. So she sets up the special "sourceup"
mode that creates HTML inside the comments of the source files, from which she
generates the normal Javadoc format.

Later, Jenny's company realizes that they need some source documentation
generated. So Jenny creates some normal markdown files, and commits them to the
tree. They are included into the normal site for review, but the documentation
specialist wants to edit word documents. So Jenny adds a step to the build to
call markdown2doc, some other tool she found via Google.



## Design

Major categories of language commenting style

* C-style: Java, C++, C, javaScript, Objective-C
* Shell-style: Python, Perl, bash, zsh
* Lisp-style: ; single line, |#, #| multiline
* SQL-style: -- single line, /* */ multiline
* PHP -> delimited by php, then it's C-style with shell addition
* Velocity templates -> ## single line #*, *# multiline

Try to minimize the setup to "make sure Java is there".

Usage:

    java -jar simplelit.jar -o build/docs

The tool will just find source files underneath the current folder and generate
a website following the same structure.

You should be able to turn off the 'find all sources' and just search underneath
indicated paths.

    java -jar simplelit.jar -o build/docs -i example

Basically, there are probably files announced via a stream or something.

I'm going to assume that if people want a fancy schmancy template system, they
can build one. This tool won't do that. Although you should be able to iterate
over the markdown or the html snippets of the code in Java.

    for (String markdown : simplelit.findMarkdown()) {
        // use markdown
    }

    for (String xhtml : simplelit.findXHTML()) {
        // use XHTML
    }

Easy customization: check out the project and rebuild your own version of the
tool and deploy it yourownbadassself.

    for { 
        blocks <- simplelit.findMarkdown()
        xhtml <- myXHTMLWriter.write(blocks)
    } yield xhtml



## Testing 

A custom runner will create input and actual output folders, then run a tree
diff tool. If there are no diffs, we're OK. Otherwise, we dump the diff output.


[1]: http://jashkenas.github.com/docco/
[2]: http://scala-lang.org
[3]: http://tristanhunt.com/projects/knockoff