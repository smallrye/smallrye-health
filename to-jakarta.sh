#!/usr/bin/env bash

# move to jakarta parent
find . -type f -name 'pom.xml' -exec sed -i 's/smallrye-parent/smallrye-jakarta-parent/g' {} +
# java sources
find . -type f -name '*.java' -exec sed -i 's/javax./jakarta./g' {} +
# service loader files
find . -type f -name "javax*" -exec sh -c 'mv "$0" "${0/javax/jakarta}"' '{}' \;
# docs
find doc -type f -name '*.adoc' -exec sed -i 's/javax./jakarta./g' {} +

mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.nextMajorVersion}.0.0-SNAPSHOT
find examples -depth 1 -type d | xargs -I{} mvn -pl {} build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.nextMajorVersion}.0.0-SNAPSHOT

mvn versions:set-property -Dproperty=version.eclipse.microprofile.health -DnewVersion=4.0
mvn versions:update-property -Dproperty=version.eclipse.microprofile.config -DnewVersion=3.0.1
mvn versions:update-property -Dproperty=version.jakarta.servlet -DnewVersion=6.0.0
mvn versions:update-property -Dproperty=version.smallrye-config -DnewVersion=3.0.0
mvn versions:update-property -Dproperty=version.smallrye-common -DnewVersion=2.0.0
mvn versions:set-property -Dproperty=version.wildfly -DnewVersion=27.0.0.Alpha4
mvn versions:set-property -Dproperty=version.testng -DnewVersion=7.5
mvn versions:update-property -Dproperty=version.smallrye.testing.utilities -DnewVersion=2.0.0
