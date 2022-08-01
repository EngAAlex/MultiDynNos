# ./bin/sh

echo Starting MultiDynNoS script installer for Linux
echo

echo Installing Prerequisites...

cd ..

sudo apt-get update -qq
sudo apt-get install -y maven graphviz openjdk-11-jdk

echo
echo DONE!
echo 
echo
echo Building MultiDynNoS
echo
echo 

mvn clean package -DskipTests

echo 
echo "#### ALL DONE. MultiDynNoS is ready to run. The compiled binary is available in the ../target folder. Run java -jar ../target/multidynnos-1.0.0-complete.jar for help text."
echo