#!/bin/sh

echo "*** Unsetting variables ***"
echo "Before:"
echo "     GEM_HOME: $GEM_HOME"
echo "     GEM_PATH: $GEM_PATH"
echo "  BUNDLE_PATH: $BUNDLE_PATH"

unset GEM_HOME
unset GEM_PATH
unset BUNDLE_PATH

echo "After:"
echo "     GEM_HOME: $GEM_HOME"
echo "     GEM_PATH: $GEM_PATH"
echo "  BUNDLE_PATH: $BUNDLE_PATH"

echo "*** Environment ***"
set

echo "*** Setting up Maven repository***"
mkdir ~/m2repo
export M2REPO="~/m2repo"

echo "*** PWD ***"
echo $PWD

echo "*** Choosing JDK7 as the build enviroment"
jdk_switcher use oraclejdk7

echo "*** Starting Build ***"

echo "Performing core build skipping tests"
mvn -Dmaven.repo.local=$M2_REPO -U -s $TRAVIS_BUILD_DIR/support/settings.xml install -Dmaven.test.skip=true

echo "Performing integ build on JDK7"
cd integration-tests && mvn -Dmaven.repo.local=$M2_REPO -U -s $TRAVIS_BUILD_DIR/support/settings.xml test

echo "Performing integ build on JDK6"
cd $TRAVIS_BUILD_DIR
jdk_switcher use openjdk6
mvn clean -Pdist
cd integration-tests && mvn -Dmaven.repo.local=$M2_REPO -U -s $TRAVIS_BUILD_DIR/support/settings.xml test
