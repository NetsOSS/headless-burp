
# Installation

## Installing Burp on windows

Before installing [Burp Suite], make sure you have Java 8 up and running, You can verify if you're already good to go with the following commands: 

```bash
java -version
```

!!! warning "headless-burp version requirements"

    headless-burp requires java >= 1.8

Download Burp Suite from [here](https://portswigger.net/burp/download.html "download").

Follow instructions on https://portswigger.net/burp/help/suite_gettingstarted.html

## Installing Burp on a headless machine

```bash
java -jar -Xmx1024m /path/to/burp.jar
```

TODO: Add installation steps and screenshots

[Burp Suite]: https://portswigger.net/burp/


## Building Documentation
```
pip install mkdocs
pip install mkdocs-material
pip install pymdown-extensions

mkdocs serve
mkdocs build --clean

mkdocs gh-deploy --clean
```