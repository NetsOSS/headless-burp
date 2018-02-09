# Burp and Automation

Burp does not lend itself easily to automation. There is not built in way to run Burp headless, i.e on a server without a graphical user interface. This is a requirement if you want to automate the job. Burp documentation states that this might come in future versions. 

However, you can build an extension to burp and get burp to load it. Several programming languages are supported, including Java, Python and Ruby. 

And there are several developers who have already done this and made their extensions available through [BApp Store]. That is the Burp community for sharing these extensions. 

One of the most popular extensions for running Burp headless is [Carbonator]. By giving it a target scope, it spiders the scope and performs a scan and a HTML report is generated at the end.  

However, we wanted more!
We wanted JUnit like output in Jenkins and a failed build whenever any vulnerabilities were found. And we also wanted the possibility to flag false positives. That is after a potential security hole has been found and reported, you investigate it in your code and find that no way this can happen, you want to configure this so that it will not be reported on the next run. 

And just performing a scan on a target doesnt really give that much in our javascript web application world today by reasons I’ll get back to later. So we also had to be able to automatically proxy the application to build that sitemap. 

So we first decided to port [Carbonator] to Java (as we didnt know python very well), and built our own extension.

[Carbonator]: https://github.com/integrissecurity/carbonator
[BApp Store]: https://portswigger.net/bappstore