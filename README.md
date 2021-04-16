# Json Mapping

Lightweight JSON parsing library for Java, handling [org.json](https://github.com/stleary/JSON-java) objects written with [VAVR](https://www.vavr.io/) at the main driver.
Generic serialization of Java objects to JSON, and deserialization of JSON to Java objects.
With annotations. Available from Nexus

# Development

## Prerequisities

If you develop for KSSO, see the [internal dev wiki](https://kantega-sso.atlassian.net/wiki/spaces/KSI/pages/345636908/Sett+opp+utviklingsmilj+p+lokal+maskin) for details on how to configure the Atlassian SDK so your local environment is compatible.

1. Have Java 8 installed ([AdoptOpenJdk](https://adoptopenjdk.net/) is recommended for Atlassian development) and $JAVA_HOME on your path. 
2. Have [maven](https://maven.apache.org/) installed, preferrably version 5.3.2 (which matches Atlassian SDK). 
3. Download [settings.xml from the Kantega SSO Wiki](https://kantega-sso.atlassian.net/wiki/download/attachments/345636908/settings.xml) and add to ~/.m2.

## Contributing Code

1. `git checkout master`
2. `git pull`
3. `git checkout -b <name-of-branch>`
4. `git commit <your changes>`
5. `git push -u origin <name-of-branch>`
6. Open a [merge request](https://ksso-gitlab.kantega.org/ksso/json-mapping/-/merge_requests)

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

