# Run TensorFlow model on GraalVM

## Overview

[GraalVM](https://www.graalvm.org/) is an alternative to HotSpot JVM that allows you to compile the application
code ahead of time (AOT) into a native execute.

This is an example to demonstrate how to use Deep Java Library to run a TensorFlow model on GraalVM.  

## Setup

Follows the [GraalVM installation instruction](https://www.graalvm.org/getting-started/#install-graalvm) to set up
GraalVM on your system. Make sure the `native-image` tool also gets installed:
- Configure $GRAALVM_HOME and $JAVA_HOME environment variable to the location you installed
    ```shell script
    # for example:
    export $GRAALVM_HOME=/path/to/graalvm/
    export $JAVA_HOME=$GRAALVM_HOME
    ```
- [native-image](https://www.graalvm.org/getting-started/#native-images) is installed
    ```shell script
    $GRAAL_HOME/bin/gu install native-image    
    ```
  
## Build native image

Run the following command to build the native executable for this project:
```shell script
./mvnw clean package
```

You will find a native executable file generated: `target/image-classification`

# Run application

```shell script
# set environment variable to suppress TensorFlow logging:
export TF_CPP_MIN_LOG_LEVEL=1
target/image-classification

[
	class: "Pneumonia", probability: 0.99778
	class: "Normal", probability: 0.00221
]
```
