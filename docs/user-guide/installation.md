# Prerequisites

Before installing [Burp Suite] with headless-burp extensions, make sure you have Java 8 installed, you can verify if you're already good to go with the following commands: 

```bash
java -version
```

!!! warning "headless-burp version requirements"

    headless-burp requires java >= 1.8

# Installing Burp

Download Burp Suite from [here][Download Burp Suite] and follow instructions from [getting started with Burp Suite]

## Installing Burp on a headless machine

```bash
java -jar -Xmx1024m /path/to/burp.jar
```

Follow the instructions on the prompt. To install the license manually on a headless environment, refer the "Manual Activation" section from [activating your burp license key]

[Burp Suite]: https://portswigger.net/burp/
[Download Burp Suite]: https://portswigger.net/burp/download.html "download"
[getting started with Burp Suite]: https://portswigger.net/burp/help/suite_gettingstarted.html
[activating your burp license key]: https://support.portswigger.net/customer/en/portal/articles/2327558-Installing_License.html