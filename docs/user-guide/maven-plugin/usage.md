# Burp Maven Plugin

Maven plugin that allows you to run Burp Suite's Proxy and Scanner tools in headless mode.

The plugin is essentially a wrapper around the [Headless Burp Proxy] and [Headless Burp Scanner] extensions. It offers easy way to integrate security testing using [Burp Suite] into the project build lifecycle.

## Full example
    
```xml
<build>
    ...
    <plugins>
        ...
        <plugin>
            <groupId>eu.nets.burp</groupId>
            <artifactId>burp-maven-plugin</artifactId>
            <version>master-SNAPSHOT</version>
            <configuration>
                <burpSuite>burp/burpsuite_pro_v1.7.31.jar</burpSuite>
                <burpProjectFile>target/headless-burp-project.burp</burpProjectFile>
                <burpConfig>burp/config.xml</burpConfig>
                <headless>true</headless>
                <promptOnExit>false</promptOnExit>
                <verbose>true</verbose>
                <skip>false</skip>
            </configuration>
            <executions>
                <execution>
                    <id>start-burp-proxy</id>
                    <phase>pre-integration-test</phase>
                    <goals>
                        <goal>start-proxy</goal>
                    </goals>
                </execution>
                <execution>
                    <id>stop-burp-proxy</id>
                    <phase>post-integration-test</phase>
                    <goals>
                        <goal>stop-proxy</goal>
                    </goals>
                </execution>
                <execution>
                    <id>start-burp-scan</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>start-scan</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ...
    </plugins>
    ...
</build>
```

[Burp Suite]: https://portswigger.net/burp
[Headless Burp Proxy]: ../burp-extensions/headless-burp-proxy.md
[Headless Burp Scanner]: ../burp-extensions/headless-burp-scanner.md