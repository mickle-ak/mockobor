## Code Style

Code style used in Mockobor can be imported from `config`-subdirectory.

- for IntelliJ IDEA — [codestyle-idea.xml](config/codestyle-idea.xml)
- for Eclipse — [codestyle-eclipse.xml](config/codestyle-eclipse.xml)

## Releasing

### Create a new release

In "**develop**" branch:

1. Update section "**In the next Version**" in change log file [CHANGELOG.md](CHANGELOG.md) (_don't change version, only add changes in
   the section_).
2. Commit all changes
3. Clean git history, if necessary
4. Start ``> ./gradlew -q createRelease``. It does follows:
    - updates versions in [CHANGELOG.md](CHANGELOG.md) and [README.md](README.md) files
    - commits all changes (with message "Release version: **${version}**")
    - adds git tag "**v${version}**" (_the tag starts with "**v**", ${version} - just created release version_)

In "**master**" branch:

1. Merge (fast-forward) "develop" into "master"
2. Push both branches to GitHub: ``git push origin master develop``
3. Push new created release tag: ``git push origin v${version}`` (_replace ${version} with just created release version_)
    - It stars the GitHub action on new tag `v*` to
        - create a new GutHub-Release
        - create a new artifact
        - publish it to Maven Central via Sonatype OSSRH

#### Create a new minor or major release

To create a new minor (middle) or major version use `versionIncrementer=incrementMinor` or `release.versionIncrementer=incrementMajor`
parameters:
```
> ./gradlew -q createRelease "-Prelease.versionIncrementer=incrementMinor"
```

#### Force release version

To create a version with the custom version number use `release.forceVersion` parameter:
```
> ./gradlew -q createRelease "-Prelease.forceVersion=x.x.x"
```

### Check the current version

start ``./gradlew -q currentVersion``

### Used plugins

- versioning — [Aion Release Plugin](https://github.com/allegro/axion-release-plugin)
    - [documentation](https://axion-release-plugin.readthedocs.io/en/latest/)
- publishing — [Gradle Nexus Publish Plugin](https://github.com/gradle-nexus/publish-plugin)
