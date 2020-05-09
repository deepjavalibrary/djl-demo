# Serverless Model Serving with DJL 

## Overview
It's quite complicated to host a deep learning model and usually the cost is high as well.
AWS Lambda provides a low cost and low maintenance solution. However, deploy DL models with Lambda is pretty challenging:
- DL framework binary is big, it hard to package it into a standalone zip file for AWS Lambda.
- Python DL framework usually contains multiple dependencies, manage dependencies is non-trivial. 
- DL model file usually is large, packing models is difficult.

In this demo, we are going to show you how [Deep Java Library (DJL)](http://djl.ai) resolve above issues.

The Lambda Function we are creating is an image classification application that predicts labels along with their
probabilities using a pre-trained MXNet model.

## Preparation
- [You need to install aws cli on your system](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)
- [Configure your aws cli with credential and region](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html#cli-quick-configuration)
- [Setup Java environment](https://github.com/awslabs/djl/blob/master/docs/development/setup.md#install-the-java-development-kit)

## Build and deploy to AWS
Run the following command to deploy to AWS:
```shell script
cd lambda-model-serving
./gradlew deploy
```

Above command will create:
- a S3 bucket, the bucket name will be stored in `bucket-name.txt` file 
- a cloudformation stack named `djl-lambda`, a template file named `out.yml` will also be created 
- a Lambda Function named `DJL-Lambda`

## Invoke Lambda Function 
```shell script
aws lambda invoke --function-name DJL-Lambda --payload '{"inputImageUrl":"https://djl-ai.s3.amazonaws.com/resources/images/kitten.jpg"}' output.json

cat output.json
```

The output will be stored in output.json file:

    [
      {
        "className": "n02123045 tabby, tabby cat",
        "probability": 0.48384541273117065
      },
      {
        "className": "n02123159 tiger cat",
        "probability": 0.20599405467510223
      },
      {
        "className": "n02124075 Egyptian cat",
        "probability": 0.18810519576072693
      },
      {
        "className": "n02123394 Persian cat",
        "probability": 0.06411759555339813
      },
      {
        "className": "n02127052 lynx, catamount",
        "probability": 0.01021555159240961
      }
    ]


## Clean up
Use the following command to clean up resources created in your AWS account:
```shell script
./cleanup.sh
```

## Limitations
AWS Lambda has the following limitations:
- GPU instance is not yet available
- 512 MB /tmp limit
- Slow startup if not frequently used
