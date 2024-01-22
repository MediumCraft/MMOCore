Official repository for MMOCore

### Useful Links

- Purchase the plugin here: https://www.spigotmc.org/resources/mmocore.70575/
- Development builds: https://phoenixdevt.fr/devbuilds
- Official documentation: https://gitlab.com/phoenix-dvpmt/mmocore/-/wikis/home
- Discord Support: https://phoenixdevt.fr/discord
- Other plugins: https://www.spigotmc.org/resources/authors/indyuce.253965/

### Using MMOCore as dependency

Register the PhoenixDevelopment public repository:

```
<repository>
    <id>phoenix</id>
    <url>https://nexus.phoenixdevt.fr/repository/maven-public/</url>
</repository>
```

And then add both `MythicLib-dist` and `MMOCore-API` as dependencies:

```
<dependency>
    <groupId>io.lumine</groupId>
    <artifactId>MythicLib-dist</artifactId>
    <version>1.6.2-SNAPSHOT</version>
    <scope>provided</scope>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>net.Indyuce</groupId>
    <artifactId>MMOCore-API</artifactId>
    <version>1.12.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```
