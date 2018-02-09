# Burp

Burp is an automated tool for finding those security holes that exists in your application. It is a tool that contains a lot of features for any type of security testing. We are not using the whole suite of components that Burp offers but are using those features that lend themselves to automation. 

First the proxy is used to map the application. In a manual security test you would use the proxy to intercept all your traffic while going through your applications functionality. Getting burp to build up a site map of all your requests and parameters. 

The spider can then be used as a tool to crawl through the rest of the application you missed in the proxying. A spider will take a URL and try to access all resources on that URL recursively until it has covered everything. 

This site map built by the proxy and spider is then what the scanner uses to test your application for vulnerabilities. It performs this test by automatically attack your site using a number of known hacking techniques, and reports back to you any security issues it finds. 

And these are the three tools we wanted to automate in our PoC. 