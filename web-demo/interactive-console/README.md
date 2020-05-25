# Interactive JConsole for DJL

## Introduction
This is a demo application designed for user to use JConsole and DJL online.
Hosted by [AWS Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/),
it contains all Java 11 feature. Try it out [here](https://aws-samples.github.io/djl-demo/web-demo/interactive-console/src/main/webapps/).

![terminal](img/terminal.gif)

We did some pre-configuration for each JConsole as follows:

```
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.types.Shape;
import ai.djl.ndarray.index.NDIndex;
NDManager manager = NDManager.newBaseManager();
```

Backend built on top of [Spring Boot](https://spring.io/projects/spring-boot)
and frontend built on top of [xterm.js](https://xtermjs.org/).

Currently, this console only offers limited operations like:
- backspace to remove content
- left and right arrow to move cursor
- copy/paste option to paste to command line
- clear keyword to clear the screen
