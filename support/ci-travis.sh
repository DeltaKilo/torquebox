
function switch_jdk () {
 sudo update-java-alternatives -s $1
 JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")
 echo "Java switched to"
 java -version
}

echo "*** Updating enviroment ***"
sudo apt-get -qq update
sudo apt-get -qq install openjdk-6-jdk openjdk-7-jdk icedtea-6-plugin icedtea-7-plugin oracle-java7-installer
switch_jdk java-7-oracle

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

echo "*** Memory, CPU & kernel info ***"
free
lscpu
uname -a

echo "*** Setting up Maven repository***"
mkdir ~/m2repo
export M2REPO="~/m2repo"

echo "*** PWD ***"
echo $PWD

echo "*** Starting Build ***"

echo "Performing core build skipping tests"
mvn -Dmaven.repo.local=$M2_REPO -U -s $TRAVIS_BUILD_DIR/support/settings.xml install -Dmaven.test.skip=true

echo "Performing integ build on JDK7"
mvn clean -Pinteg
cd integration-tests && mvn -Dmaven.repo.local=$M2_REPO -U -s $TRAVIS_BUILD_DIR/support/settings.xml test

echo "Performing integ build on JDK6"
cd $TRAVIS_BUILD_DIR
switch_jdk java-6-openjdk
mvn clean -Pinteg
cd integration-tests && mvn -Dmaven.repo.local=$M2_REPO -U -s $TRAVIS_BUILD_DIR/support/settings.xml test
