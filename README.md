# Attribution Maven Plugin

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jinnovations/attribution-maven-plugin/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.jinnovations/attribution-maven-plugin/)

[![Javadocs](https://www.javadoc.io/badge/com.github.jinnovations/attribution-maven-plugin.svg)](https://www.javadoc.io/doc/com.github.jinnovations/attribution-maven-plugin)


The Attribution Maven Plugin is a tool for producing information about a project's dependencies, in a way that can be consumed by external reporting tools. It currently leverages the Maven Project Info Reports Plugin (https://maven.apache.org/components/plugins/maven-project-info-reports-plugin/) to obtain all of a project's data, and then outputs the information to an XML file, such as: target/attribution.xml.

This plugin came out of the need to get some of the same information produced by the Maven Project Info Reports Plugin, and have it transformed, ready to be consumed by tools which may require input in a certain format, such as a CSV. By producing an attribution XML file, scripts can be created to translate the data into these other consumable formats.

### Configuration

* `outputFile`
  * Location of the output attribution XML file (defaults to `target/attribution.xml`)
* `forceRegeneration`
  * Whether or not regeneration of the attribution.xml file should be forced (defaults to `false`).  Normally, if the plugin detects the timestamp of the attribution.xml file is later than the project's pom.xml file, it will not go through the process of regenerating it.  This parameter will cause it to be regenerated, regardless of file timestamps.
* `dependencyOverrides`
  * Provides one or more `<dependencyOverride>` elements, which allow you to override values placed in the final attribution.xml file.  This feature is handy when for some reason a dependency doesn't contain certain pieces of information in its pom.  See the example below for how this is specified.
* `skipDownloadUrl`
  * This flag allows to skip the retrieval of the downloadUrl info (defaults to `false`). It can highly reduce the run time of the plugin if this info is not relevant as this part is quite time consuming. 
* `includeTransitiveDependencies`
  * Whether or not transitive dependencies should be included in the report (defaults to `true`). Setting it to false will include only the project dependencies. 
* `threads`
  * Number of concurrent thread tasks used to resolve the maven dependencies (defaults to `10`). 
* `skip`
  * If true, no action will take place by the plugin (defaults to `false`).


### Example

```xml
<plugin>
    <groupId>com.github.jinnovations</groupId>
    <artifactId>attribution-maven-plugin</artifactId>
    <version>${attribution-maven-plugin.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>generate-attribution-file</goal>
            </goals>
            <phase>generate-resources</phase>
        </execution>
    </executions>
    <configuration>
        <outputFile>${project.build.directory}/attribution.xml</outputFile>
        <dependencyOverrides>
            <dependencyOverride>
                <forDependency>org.apache.axis:axis</forDependency>
                <projectUrl>https://axis.apache.org/axis/</projectUrl>
                <license>
                    <name>Apache License, Version 2.0</name>
                    <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
                </license>
            </dependencyOverride>
            <dependencyOverride>
                <forDependency>net.iharder:base64</forDependency>
                <license>
                    <name>Public Domain (any license you desire)</name>
                    <url>http://iharder.sourceforge.net/current/java/base64/</url>
                </license>
            </dependencyOverride>
            <dependencyOverride>
                <forDependency>javax.xml:jaxrpc-api</forDependency>
                <projectUrl>https://java.net/projects/jax-rpc/</projectUrl>
                <license>
                    <name>CDDL-1.0</name>
                    <url>https://opensource.org/licenses/cddl1.php</url>
                </license>
            </dependencyOverride>
            <dependencyOverride>
                <forDependency>org.hamcrest:hamcrest-core</forDependency>
                <projectUrl>http://hamcrest.org/JavaHamcrest/</projectUrl>
            </dependencyOverride>
        </dependencyOverrides>
    </configuration>
</plugin>
```