# Json Mapping

Lightweight JSON parsing library for Java available from Nexus.

Generic serialization of Java objects to JSON, and deserialization of JSON to Java objects.
With annotations.

## Google Java Style
This project uses a [maven plugin](https://github.com/Cosium/git-code-format-maven-plugin) to run [google-java-format](https://github.com/google/google-java-format) to enforce the [Google Java Style](https://google.github.io/styleguide/javaguide.html).

### Configuring IntelliJ IDEA
1. Install the [google-java-format](https://plugins.jetbrains.com/plugin/8527-google-java-format) plugin for IntelliJ IDEA. (Settings -> Plugins)
2. Import the [IntelliJ Java Google Style file](https://raw.githubusercontent.com/google/styleguide/gh-pages/intellij-java-google-style.xml). (Settings -> Editor -> Code Style)

#### Manually format files
```mvn git-code-format:format-code```

#### Manually validate files
```mvn git-code-format:validate-code-format``

## Spotbugs

This project uses Spotbugs and FindSecBugs static analysis to ensure code quality. It is run automatically in pipelines, and could be run locally or in IDE.

### Open report in project root folder:
1. Download spotbugsXml.xml from gitlab job
2. ```mvn spotbugs:gui``` -->
3. Ppen spotbugsXml.xml 
4. Save as html 
5. Ppen in browser

