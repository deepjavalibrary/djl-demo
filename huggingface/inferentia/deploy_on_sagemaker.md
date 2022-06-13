# Deploy Huggingface model on SageMaker

This demo will show you how to deploy your Huggingface model on SageMaker step by step.

## Prerequisite
- [You need to install aws cli on your system](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)
- [Configure your aws cli with credential and region](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html#cli-quick-configuration)
- [Setup Java environment](https://github.com/deepjavalibrary/djl/blob/master/docs/development/setup.md#install-the-java-development-kit)

## Prepare your model

1. Follow [the instruction](README.md#compile-your-model-into-neuron-traced-model) to compile your
model into Neuron traced model.
2. Create a `.tar.gz` file, by default this example will use djl PyTorch engine to serve the model.
If you want to serve with Python engine, remove [serving.properties](https://github.com/deepjavalibrary/djl-demo/blob/master/huggingface/inferentia/question_answering/serving.properties) file

```shell
cd huggingface/inferentia

tar cvfz question_answering.tar.gz question_answering
```

## Register your docker image in AWS ECR

You can download **djl-serving** docker image from docker hub. See [djl-serving docker file](https://github.com/deepjavalibrary/djl-serving/tree/master/serving/docker)
for more detail.

```shell
AWS_ACCOUNT=$(aws sts get-caller-identity | jq -r .Account || true)

aws ecr get-login-password | docker login --username AWS --password-stdin $AWS_ACCOUNT.dkr.ecr.us-east-1.amazonaws.com
aws ecr create-repository --repository-name djl-inf1

docker pull deepjavalibrary/djl-serving:0.14.0-inf1
docker tag deepjavalibrary/djl-serving:0.14.0-inf1 $AWS_ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/djl-inf1
docker push $AWS_ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/djl-inf1
```

## Upload your model to AWS S3 bucket

```shell
BUCKET_NAME=YOUR_BUCKET_NAME
aws s3 cp question_answering.tar.gz s3://$BUCKET_NAME/serving/
```

## Create SageMaker execution role

```shell
aws iam create-role --role-name AmazonSageMaker-ExecutionRole-djl \
    --assume-role-policy-document file://SageMaker-Role-Policy.json
aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AmazonSageMakerFullAccess \
    --role-name AmazonSageMaker-ExecutionRole-djl
aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess \
    --role-name AmazonSageMaker-ExecutionRole-djl
```

## Create SageMaker model

```shell
aws sagemaker create-model --model-name bertqa --primary-container \
    "Image=${AWS_ACCOUNT}.dkr.ecr.us-east-1.amazonaws.com/djl-inf1,ModelDataUrl=s3://$BUCKET_NAME/serving/question_answering.tar.gz" \
    --execution-role-arn "arn:aws:iam::${AWS_ACCOUNT}:role/AmazonSageMaker-ExecutionRole-djl"
```

## Create SageMaker endpoint config

```shell
aws sagemaker create-endpoint-config --endpoint-config-name bertqa-inf1-config \
    --production-variants VariantName=AllTraffic,ModelName=bertqa,InitialInstanceCount=1,InstanceType=ml.inf1.2xlarge,InitialVariantWeight=1.0
```

## Create SageMaker endpoint

```shell
aws sagemaker create-endpoint --endpoint-name bertqa --endpoint-config-name bertqa-inf1-config
```

## Test your model

Now you can invoke SageMaker endpoint and run inference. You can use `awscurl` command to try it
(the `awscli` command line tools requires java).

```shell
curl -O https://github.com/frankfliu/junkyard/releases/download/0.2.0/awscurl
chmod +x awscurl

awscurl -n sagemaker https://runtime.sagemaker.us-east-1.amazonaws.com/endpoints/bertqa/invocations \
    -H "Content-Type: application/json" \
    -d '{"question": "How is the weather", "paragraph": "The weather is nice, it is beautiful day"}'  
```

## Benchmark your model

```shell
awscurl -n sagemaker https://runtime.sagemaker.us-east-1.amazonaws.com/endpoints/bertqa/invocations \
    -H "Content-Type: application/json" \
    -d '{"question": "How is the weather", "paragraph": "The weather is nice, it is beautiful day"}' \
    -c 5 -N 100
```
