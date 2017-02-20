# Static Analysis

When just looking at your IDE to understand all the connections is not enough you can do it the hard way.

This is my attempt to make a simple toolchain for analysis of module dependencies in java project.

First you should gather all the sources of java-class folders and archives in one place.

Then you have to parse java-classes and get the important data from them. There is a *simple* way to read names of all the classes which are used inside each class.

Then you have to combine all the class-data about one module into one file. It is still an array of data but it not a folder structure anymore.

Third step I called compressing. At this step we are transforming data about signle classes into structure that contains classes provided, classes required for them to operate, plus module name as separate entity.

Forth step is getting all the compressed data about multiple modules and find how their provided and required data are interconnected. So as a result we have an array of modules which have list of modules required for them. For comfort there are two versions, one full with all the classes pointing to correct modules and other minified which omits this.

Fifth step is adding some javascript so that minified data can be easily used from html-page. So that now we can just open an Analysis page and see visualized connections between our chosen modules.

## Hints

You can sometimes cheat and don't recompute everything. Because for example JRE runtime classes are about 20k in number. It can take a while before they will be parsed.

To use these steps just try to use batch scripts (starting with *clean_*) provided in each folder corresponding to stages. And it is working by just clicking on them %) .

Have fun!