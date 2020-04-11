# Visualizing Training with DJL

This module contains UI TrainingListener that provides training visualization UI.

Currently, implemented features:

- Training/Validating progress
- Training Metrics Visualization chart

## Prerequisites

* You need to have Java Development Kit version 11 or later installed on your system.
* You should be familiar with the API documentation in the DJL [Javadoc](https://javadoc.djl.ai/api/0.4.0/index.html).

### Install the Java Development Kit
For ubuntu:

sudo apt-get install openjdk-11-jdk-headless
For centos

sudo yum install java-11-openjdk-devel
For Mac:

brew tap homebrew/cask-versions
brew update
brew cask install adoptopenjdk11

You can also download and install [Oracle JDK](https://www.oracle.com/technetwork/java/javase/overview/index.html)
manually if you have trouble with the previous commands.

If you have multiple versions of Java installed, you can use the $JAVA_HOME environment
variable to control which version of Java to use.

# How to

## Add the Deep Java Library UI dependency to your project.
  ```
        <dependency>
            <groupId>ai.djl</groupId>
            <artifactId>ui</artifactId>
            <version>0.5.0-SNAPSHOT</version>
        </dependency>
  ```

## Add UI Training Listener in a Training Configuration.
  ```
       .addTrainingListeners(new UiTrainingListener())
  ```


# Getting started: 30 seconds to run an example

## Build and install DJL UI

This component supports building with Maven. To build, use the following command:

* Maven build
    ```sh
    mvn clean install -DskipTests -Pfrontend -f djl-ui
    ```

## Build example project

To build, use the following command:

* Maven build
    ```sh
    mvn package -DskipTests -f djl-ui-demo

## Run example code

Run Handwritten Digit Recognition example

* Maven
    ```sh
    mvn exec:java -Dexec.mainClass="org.example.MnistTraining" -f djl-ui-demo
    ```
  
## Open browser

Open http://localhost:8080 to get:

![Screenshot](djl-ui.gif)

