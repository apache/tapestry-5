[<img src="http://tapestry.apache.org/images/tapestry.png" align="center"/>](http://tapestry.apache.org)

Tapestry is a component-oriented Java web app framework focusing on performance and developer productivity.

A *component* is just a reusable part of a page. It's trivially easy to create your own components, and Tapestry
comes with a large number of components you can use (Form, Loop, Select, Checkbox, Grid, BeanEditor, etc.).

In Tapestry, each page and component is a simple Java POJO with a corresponding HTML template. The HTML template and corresponding Java class have the same name (e.g. "Breadcrumbs.html" and "Breadcrumbs.java"), so you don't have to tell Tapestry which template uses which Java class. It's automatic.

Tapestry features *live class reloading*: change your Java code, refresh the browser and see the changes instantly.

AJAX support allows you to create responsive web interfaces while writing little to no JavaScript. (But if you like
writing JavaScript, great, no problem, Tapestry gets out of your way.)

## Quick Start

Main article: [Getting Started](https://tapestry.apache.org/getting-started.html)

You can let Apache Maven create your initial project for you:

    mvn archetype:generate -DarchetypeCatalog=http://tapestry.apache.org

Maven will prompt you for the archetype to create ("Tapestry 5 Quickstart Project") and the exact version
number (e.g., "5.4.3"). It also asks you for a group id, an artifact id, and a version number. Once Maven
dowloads everything, then you can start the app:

    $ cd newapp
    $ mvn jetty:run

Then just send your browser to http://localhost:8080/newapp

See the [Getting Started](http://tapestry.apache.org/getting-started.html) introduction as well as the [Tapestry Tutorial](http://tapestry.apache.org/tapestry-tutorial.html) for a deeper dive.

## Main Docs

See https://tapestry.apache.org/documentation.html for the details on every Tapestry topic.
