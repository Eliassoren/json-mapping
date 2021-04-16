# Json Mapping

Lightweight JSON parsing library for Java, handling [org.json](https://github.com/stleary/JSON-java) objects written with [VAVR](https://www.vavr.io/) at the main driver.
Generic serialization of Java objects to JSON, and deserialization of JSON to Java objects.
With annotations. Available from Nexus

# Development

## Prerequisities

If you develop for KSSO, see the [internal dev wiki](https://kantega-sso.atlassian.net/wiki/spaces/KSI/pages/345636908/Sett+opp+utviklingsmilj+p+lokal+maskin) for details on how to configure the Atlassian SDK so your local environment is compatible.

1. Have Java 8 installed ([AdoptOpenJdk](https://adoptopenjdk.net/) is recommended for Atlassian development) and $JAVA_HOME on your path. 
2. Have [maven](https://maven.apache.org/) installed, preferrably version 5.3.4 (which matches Atlassian SDK 8.0.16 currently used in KSSO). 
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
```mvn git-code-format:validate-code-format```

## Spotbugs

This project uses Spotbugs and FindSecBugs static analysis plugins to ensure code quality and security. It is run automatically in pipelines, and could be run locally in maven or in IDEA.

### Local

1. `mvn compile`
2. `mvn spotbugs:spotbugs`
3. `mvn spotbugs:gui`
4. Open `target/spotbugsXml.xml`
5. Save as html 
6. Open in browser


### Open report from CI/CD pipeline

1. Download spotbugsXml.xml from gitlab CI/CD job
2. ```mvn spotbugs:gui``` -->
3. Open spotbugsXml.xml 
4. Save as html 
5. Open in browser

# Usage

JsonMapping has two components, the `Write` component and the `Read` component. It handles fields which are accessible through accessors, but can also populate private, unmodifiable fields.


```java
    User user = repository.createUser(); // ID = 8777
    user.setUsername("jondoe");
    user.setEmail("jondoe@example.com");
    List<String> groups = Arrays.asList(
      "group 1",
      "group 2"
    );
    user.setGroups(groups);
    JSONObject json = JsonMapping.Write.objectAsJson(expected).getOrNull();
    /* json:
        {
            "ID": 8777,
            "username": "jondoe",
            "email": "jondoe@example.com",
            "groups": ["group 1", "group 2"]
        }
    */
    User deserialized = JsonMapping.Read.valueFromJson(json, User.class);
    assertEquals(user, deserialized); // true
```

Data objects can (and should) be annotated with the @JsonProperty annotation to ensure consistency in case of renaming variables.

```java
public class User {
    @JsonProperty("ID")
    private final int ID;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("groups")
    private List<String> groups;

    User(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public void addGroup(String group) {
        groups.add(group);
    }
}
```

