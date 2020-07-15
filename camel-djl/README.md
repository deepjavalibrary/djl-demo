# DJL Component in Apache Camel

An example application that demonstrates simple HTTP-service to classify images using Zoo Model.

### Install the Java Development Kit
For ubuntu:
```
    sudo apt-get install openjdk-11-jdk-headless
```
For centos
```
    sudo yum install java-11-openjdk-devel
```
For Mac:
```
    brew tap homebrew/cask-versions
    brew update
    brew cask install adoptopenjdk11
```
You can also download and install [Oracle JDK](https://www.oracle.com/technetwork/java/javase/overview/index.html)
manually if you have trouble with the previous commands.

If you have multiple versions of Java installed, you can use the ```$JAVA_HOME``` environment
variable to control which version of Java to use.

### Build demo
```sh
    mvn clean package
```

### Start demo
```sh
    mvn exec:java -Dexec.mainClass="one.entropy.demo.camel.djl.Demo"
```  

### Call service demo
```sh
    curl -i -X POST -H "Content-Type: multipart/form-data" -F "image=@src/test/resources/10.png" http://localhost:8080/upload
```  

### Expected response
```json
    HTTP/1.1 200 OK
    Accept: */*
    Connection: keep-alive
    Content-Type: application/json
    Content-Length: 223
    User-Agent: curl/7.64.1
    Date: Sun, 12 Apr 2020 12:47:10 GMT
    
    {
      "0" : 0.99999976,
      "1" : 1.6826518E-10,
      "2" : 2.3258349E-7,
      "3" : 1.7166844E-9,
      "4" : 3.8892398E-10,
      "5" : 4.3790166E-10,
      "6" : 1.8994905E-8,
      "7" : 2.4817006E-9,
      "8" : 2.1708624E-9,
      "9" : 2.9330971E-8
    }
```  
