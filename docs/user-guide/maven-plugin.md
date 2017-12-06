# Maven Plugin

Maven plugin that allows you to run the Burp Proxy and Scanner tools in headless mode.

## Scanner
    
```xml
...
<build>
    <plugins>
        <plugin>
            <groupId>eu.nets.burp</groupId>
            <artifactId>burp-maven-plugin</artifactId>
            <version>master-SNAPSHOT</version>
            <configuration>
                <burpSuite>burp/burpsuite_pro_v1.6.26.jar</burpSuite>
                <burpConfig>burp/config.xml</burpConfig>
                <headless>true</headless>
                <burpState>target/headless-burp-proxy.state</burpState>
                <promptOnExit>false</promptOnExit>
                <verbose>true</verbose>
                <skip>false</skip>
            </configuration>
        </plugin>
    </plugins>
</build>
...
```

TODO

## Proxy
    
```xml
...
<build>
    <plugins>
        <plugin>
            <groupId>eu.nets.burp</groupId>
            <artifactId>burp-maven-plugin</artifactId>
            <version>master-SNAPSHOT</version>
            <configuration>
                <burpSuite>burp/burpsuite_pro_v1.6.26.jar</burpSuite>
                <burpConfig>burp/config.xml</burpConfig>
                <headless>true</headless>
                <burpState>target/headless-burp-proxy.state</burpState>
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
                    <configuration>
                        <burpState>burp/initial-burp-state</burpState>
                    </configuration>
                </execution>
                <execution>
                    <id>stop-burp-proxy</id>
                    <phase>post-integration-test</phase>
                    <goals>
                        <goal>stop-proxy</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
...
```