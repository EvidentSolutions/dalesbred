# Making releases

First, test your build:

    ./gradlew clean test

If everything went ok, tag your release:

    git tag v1.2.3

Now publish your changes:

    ./gradlew publish

Go to [Sonatype Nexus](https://oss.sonatype.org/) _Staging Repositories_ section, close and release the repository.
After a while, the artifacts will be synced to Maven Central.

Finally, create [release notes in GitHub](https://github.com/EvidentSolutions/dalesbred/releases) using data from
the CHANGELOG.
