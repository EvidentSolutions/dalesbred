# Making releases

First, test your build:

    ./gradlew clean test

If everything went ok, tag your release:

    git tag v1.2.3

Now publish your changes:

    ./gradlew publish

Finally close and promote the stating repository of [Sonatype Nexus](https://oss.sonatype.org/):

    ./gradlew closeAndReleaseRepository

After a while, the artifacts will be synced to Maven Central.

Finally, create [release notes in GitHub](https://github.com/EvidentSolutions/dalesbred/releases) using data from
the [CHANGELOG.md](../CHANGELOG.md).
