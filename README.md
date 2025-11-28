<h1>Duels</h1>

[![](https://jitpack.io/v/dumbo-the-developer/Duels.svg)](https://jitpack.io/#dumbo-the-developer/Duels)

---

* **[Wiki](https://docs.meteordevelopments.com)**
* **[Support Discord](https://discord.meteordevelopments.com)**

### Getting the dependency

#### Repository

Gradle:

```groovy
maven {
    name 'jitpack-repo'
    url 'https://jitpack.io'
}
```

Maven:

```xml
<repository>
  <id>jitpack-repo</id>
  <url>https://jitpack.io</url>
</repository>
```

#### Dependency

Gradle:

```groovy
implementation 'com.github.dumbo-the-developer.Duels:duels-api:VERSION'
```  

Maven:

```xml
<dependency>
    <groupId>com.github.dumbo-the-developer.Duels</groupId>
    <artifactId>duels-api</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

### plugin.yml

Add Duels as a soft-depend to ensure Duels is fully loaded before your plugin.

```yaml
soft-depend: [Duels]
```

### Getting the API instance

```java
@Override
public void onEnable() {
  Duels api = (Duels) Bukkit.getServer().getPluginManager().getPlugin("Duels");
}
```
