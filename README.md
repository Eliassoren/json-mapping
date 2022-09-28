# Json Mapping

Lightweight JSON parsing library for Java, handling [org.json](https://github.com/stleary/JSON-java) objects written with [VAVR](https://www.vavr.io/) at the main driver.
Generic serialization of Java objects to JSON, and deserialization of JSON to Java objects.
With annotations. 

# Development

## Prerequisities

1. Have Java >8 installed ([AdoptOpenJdk](https://adoptopenjdk.net/) is recommended for Atlassian development) and $JAVA_HOME on your path. 
2. Have [maven](https://maven.apache.org/) installed.

## Contributing Code

1. `git checkout main`
2. `git pull`
3. `git checkout -b <name-of-branch>`
4. `git commit <your changes>`
5. `git push -u origin <name-of-branch>`
6. Open a [pull request]()

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
2. ```mvn spotbugs:gui``` 
3. Open spotbugsXml.xml 
4. Save as html 
5. Open in browser

# Usage

JsonMapping has two components, the `Write` component and the `Read` component. It handles fields which are accessible through accessors, but can also populate private, unmodifiable fields. Nested objects are supported, and serializing objects with non-serializable fields may work if you only annotate serializable fields.

Add to your pom:

```xml
<dependencies>
    <!-- ...other dependencies -->
    <dependency>
        <groupId>com.kantegasso</groupId>
        <artifactId>json-mapping</artifactId>
        <version>1.1.15</version> <!-- whatever version is latest --->
    </dependency>
</dependencies>
```

The below example shows usage of the Write and Read component using a simple Java object. The whole API surface exposes objects wrapped in the VAVR Try monad. This library will not throw any excepctions, but wraps them in the result type.

```java
User user = repository.createUser(); // ID = 8777
user.setUsername("jondoe");
user.setEmail("jondoe@example.com");
List<String> groups = Arrays.asList(
  "group 1",
  "group 2"
);
user.setGroups(groups);
Try<JSONObject> maybeJson = JsonMapping.Write.objectAsJson(user);
/* json:
    {
        "ID": 8777,
        "username": "jondoe",
        "email": "jondoe@example.com",
        "groups": ["group 1", "group 2"]
    }
*/
Try<User> deserialized = maybeJson
    .flatMapTry(json -> JsonMapping.Read.valueFromJson(json, User.class));
assertEquals(user, deserialized.getOrNull()); // true
```

@JsonMapper annotation is required on each type or interface that you want to map to / from json.
This is to ensure that only known objects are deserialized, and reduce risk of unknown user-controlled objects causing RCE.
Data objects can (and should) also be annotated with the @JsonProperty annotation to ensure consistency in case of renaming variables. 
You may annotate fields (like shown in User.java), but could also annotate parameters in a constructor or getters (and setters).

```java
@JsonMapper
public class User {

  @JsonProperty("ID")
  private final int ID;

  @JsonProperty("username")
  private String username;

  @JsonProperty("email")
  private String email;

  @JsonProperty("groups")
  private List<String> groups;

  private User() { // empty constructor needed to parse object.
    this.ID = -1;
  }

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

