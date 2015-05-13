# Making releases

First, create a release:

    ./gradlew clean release

This will run all tests, check that you don't have unpushed commits and prompt you for released version. 
Then it will tag the release to version control and bump the version to next development version.
It won't publish the artifacts, though. Next, we'll do that. Start by checking out the created release-tag:

    git checkout v<release-version>

Then publish all artifacts and docs:

    ./gradlew clean uploadArchives publishGhPages

Go to [Sonatype Nexus](https://oss.sonatype.org/) _Staging Repositories_ section, close and release the repository.
After a while, the artifacts will be synced to Maven Central.

Now that the release is done, go back to master:

    git checkout master
