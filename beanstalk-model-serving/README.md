# Model Serving on AWS BeanStalk EC2

## Overview
DJL makes it easy to deploy your deep learning models on AWS Beanstalk.
Deploying on AWS Beanstalk is beneficial where there are low latency requirements 
for all inference requests that can happen at any time. 
The cost on the other hand will be higher when compared to using a serverless like lambda
because the EC2 instance that is hosting this will be running all the time. 
Beanstalk also provides other capabilities such as load balancing, if you want 
to host more instances and your model is expected to be used by many users.

## Prerequisites
Make sure you have an AWS account with AWS Beanstalk.

Make sure Java is setup and installed on your machine.

Have something like Postman to make the call to your endpoint.

## Setup
Run the following command to build the project.

```shell script
cd beanstalk-model-serving
./gradlew jar
```

This will create a packaged jar file in 
```
build/libs/beanstalk-model-serving-0.0.1-SNAPSHOT.jar
```

## Deploying
Go to the AWS BeanStalk Console and create a new environment.

Give you application a name.

Set the platform to these options for this example:

![Platform Select Option](https://github.com/awslabs/djl-resources/blob/master/demo/beanstalk-model-serving/images/platform.png?raw=true)

When selecting your Application Code, select the choose file option and 
upload the jar that was created in:
```
build/libs/beanstalk-ec2-model-serving-0.0.1-SNAPSHOT.jar
```

Then hit the `Create Environment` button.

Once the environment is created, we will need to go to the configuration

![Configuration Settings](https://github.com/awslabs/djl-resources/blob/master/demo/beanstalk-model-serving/images/configuration.png?raw=true)

Under the Software Settings, we will set `SERVER_PORT` to 5000.

Under the Capacity Settings, we will set the instance used to `t2.small` for a little bit more memory for our model.

We can now use PostMan to do a post call.

We will be submitting an encoded version of this Smiley Face picture:

![Smiley Face](https://github.com/awslabs/djl-resources/blob/master/demo/beanstalk-model-serving/images/smiley.png?raw=true)

Our Postman Call was using a POST call to:

`DjlDemo-env.eba-tcs36mmm.us-east-1.elasticbeanstalk.com/inference`

and our body contains the encoded byte array that can be found here:

We get a response of:

```
[
  {
    "className": "smiley_face",
    "probability": 0.9874626994132996
  },
  {
    "className": "face",
    "probability": 0.00480476301163435
  },
  {
    "className": "mouth",
    "probability": 0.0015588535461574793
  },
  {
    "className": "chandelier",
    "probability": 0.0012816740199923515
  },
  {
    "className": "goatee",
    "probability": 0.0012701823143288493
  }
]
```

We can see that the smiley face is the most probable one in the image, success!

Now you can add your own custom models and do post calls to your endpoint
for inference calls!
