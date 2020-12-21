# Serverless Model Serving with DJL 

## Overview
It's quite complicated to host a deep learning model and usually the cost is high as well.
AWS Lambda provides a low cost and low maintenance solution. However, deploying DL models with Lambda is pretty challenging:
- DL framework binary is big, it is hard to package it into a standalone zip file for AWS Lambda.
- Because a Python DL framework usually contains multiple dependencies, managing dependencies is non-trivial. 
- DL model files are usually large, packing these models is difficult.

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

# for Linux/macOS:
./gradlew deploy

# for Windows:
..\..\gradlew deploy
```

Above command will create:
- a S3 bucket, the bucket name will be stored in `bucket-name.txt` file 
- a cloudformation stack named `djl-lambda`, a template file named `out.yml` will also be created 
- a Lambda Function named `DJL-Lambda`

## Invoke Lambda Function 
```shell script
aws lambda invoke --function-name DJL-Lambda --payload '{"inputImageUrl":"https://djl-ai.s3.amazonaws.com/resources/images/kitten.jpg"}' build/output.json

cat build/output.json
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
```shell
./cleanup.sh
```

## Design choices

### Minimize package size
DJL can download deep learning framework at runtime. In this demo we use the following dependency:
```
    runtimeOnly "ai.djl.mxnet:mxnet-native-auto:1.7.0-backport"
```
With this auto detection dependency, the final `.zip` file is less then 3M.
The extracted MXNet native library file will be stored in `/tmp` folder, and it's around 155M, this can be further
reduced to less than 50M if use a custom build MXNet without MKL support.

The MXNet native library is store in S3, the download latency compare to Lambda startup time is negligible.

### Model loading
DJL ModelZoo design allows you to deploy model in three ways:
- Bundle the model in .zip file
- Load models from your own model zoo
- Load models from S3 bucket. DJL supports SageMaker trained model (.tar.gz) format.

In this demo, we are using DJL built-in MXNet model zoo. By default, it uses `resnet-18` model.
You can try different pre-trained model by passing `artifactId` parameter in the request:

```shell script
aws lambda invoke --function-name DJL-Lambda --payload '{"artifactId": "squeezenet", "inputImageUrl":"https://djl-ai.s3.amazonaws.com/resources/images/kitten.jpg"}' build/output.json
```

## Limitations
AWS Lambda has the following limitations:
- GPU instance is not yet available
- 512 MB /tmp limit
- Slow startup if not frequently used
