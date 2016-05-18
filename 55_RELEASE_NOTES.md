Scratch pad for changes destined for the 5.5 release notes page.

# Java 8 required
The minimum Java release required to run apps created with Tapestry 5.5 is Java 8.

# Updates to embedded Tomcat and Jetty versions (TAP5-2548)
With Java 8, we made the switch to servlet-api 3.0. We updated the embedded Tomcat and Jetty containers to the respective versions. Unfortunately, we had to rename Jetty7Runner to JettyRunner and Tomcat6Runner to TomcatRunner in the tapestry-runner package.