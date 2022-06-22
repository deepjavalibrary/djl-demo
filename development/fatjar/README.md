# How to distribute DJL application

This is an example of how to create distribution package for DJL application.

## Gradle

Run/test application with gradle

```
cd development/fatjar

./gradlew run
```

Distribute with `.zip` or `.tar` file

```shell
./gradlew assembleDist
```

You will find both `.zip` and `.tar` file in `build/distributions` folder.

Create fatjar:

```shell
./gradle shodowJar

java -jar build/libs/fatjar-1.0-SNAPSHOT-all.jar
```

## Maven

Run/test application with maven

```
cd development/fatjar

mvn exec:java
```

Create fatjar:

```shell
mvn package

java -jar target/fatjar-1.0-SNAPSHOT.jar
```

## SBT

Run/test application with sbt

```
cd development/fatjar

sbt run
```

Create fatjar:

```shell
sbt assembly

java -jar target/fatjar-assembly-1.0-SNAPSHOT.jar
```
