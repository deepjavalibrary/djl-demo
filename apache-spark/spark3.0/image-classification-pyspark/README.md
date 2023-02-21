# DJL PySpark Image Classification Example

## Introduction

This folder contains a PySpark-based image classification application. This application uses PySpark and the DJL Spark
extension to deploy a model for classifying images. It is implemented in Python and designed to be run on
[EMR on EKS](https://docs.aws.amazon.com/emr/latest/EMR-on-EKS-DevelopmentGuide/emr-eks.html).

## Set up EMR on EKS

### Set up an Amazon EKS cluster

Run the following command to create an EKS cluster and nodes. Replace `my-cluster` and `my-key-pair` with your own
cluster name and key pair name. Replace `us-east-2` with the region where you want to create your cluster.

```bash
eksctl create cluster \
  --name my-cluster \
  --region us-east-2 \
  --with-oidc \
  --ssh-access \
  --ssh-public-key my-key-pair \
  --instance-types=m5.xlarge \
  --managed
```

### Enable cluster access for Amazon EMR on EKS

Allow Amazon EMR on EKS access to the namespace in your cluster.

```bash
eksctl create iamidentitymapping \
    --cluster my-cluster \
    --region us-east-2 \
    --namespace kube-system \
    --service-name "emr-containers"
```

### Create a job execution role

Create a job execution role for Amazon EMR on EKS with the following policy.

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:ListBucket"
            ],
            "Resource": "arn:aws:s3:::example-bucket"
        },
        {
            "Effect": "Allow",
            "Action": [
                "logs:PutLogEvents",
                "logs:CreateLogStream",
                "logs:DescribeLogGroups",
                "logs:DescribeLogStreams"
            ],
            "Resource": [
                "arn:aws:logs:*:*:*"
            ]
        }
    ]
}  
```

### Update the trust policy of the job execution role

Run the following command to update the trust policy. Replace `my-role` with the name of the job execution role that you created.

```bash
aws emr-containers update-role-trust-policy \
       --region us-east-2 \
       --cluster-name my-cluster \
       --namespace kube-system \
       --role-name my-role
```

### Register the Amazon EKS cluster with Amazon EMR

Use the following command to create a virtual cluster for the Amazon EKS cluster and namespace that you set up in previous steps.
Replace `my-virtual-cluster` with your own virtual cluster name.

```bash
aws emr-containers create-virtual-cluster \
  --name my-virtual-cluster \
  --region us-east-2 \
  --container-provider '{
     "id": "djl-eks-cluster",
      "type": "EKS",
      "info": {
          "eksInfo": {
              "namespace": "kube-system"
          }
      }
}'
```

This command will return a virtual cluster ID. You will need this ID in the next step.

```
{
    "id": "m227daji96caa7osp5aaid2e1",
    "name": "my-cluster",
    "arn": "arn:aws:emr-containers:us-east-2:125045733377:/virtualclusters/m227daji96caa7osp5aaid2e1"
}
```

For more information on setting EMR on EKS, see the Amazon EMR documentation
[here](https://docs.aws.amazon.com/emr/latest/EMR-on-EKS-DevelopmentGuide/setting-up.html).

## Pull Docker image

1. Pull the Docker image containing the required dependencies for PySpark and DJL Spark extension from [Docker Hub].

```bash
docker pull deepjavalibrary/djl-spark:latest
```

This image can also be built using the provided
[Dockerfile](https://github.com/deepjavalibrary/djl/blob/master/docker/spark/Dockerfile).

```bash
docker build -t djl-spark .
```

2. Push the Docker image to a registry (such as Amazon ECR) that is accessible from your EKS cluster.

## Run the example

You can now run the example with input images and get prediction results.

Replace `entryPoint` with the path to the `image_classification.py` file.
Replace `entryPointArguments` with your output location.
Replace `virtual-cluster-id` with the virtual cluster ID that you created in the previous step.
Replace `spark.kubernetes.container.image` parameter with the URL of your Docker image in the previous step.

```bash
aws emr-containers start-job-run \
  --virtual-cluster-id m227daji96caa7osp5aaid2e1 \
  --name djl-spark-job \
  --region us-east-2 \
  --execution-role-arn arn:aws:iam::125045733377:role/my-role \
  --release-label emr-6.9.0-latest \
  --job-driver '{
    "sparkSubmitJobDriver": {
      "entryPoint": "s3://djl-misc/spark/scripts/image_classification.py",
      "entryPointArguments": ["s3://output_bucket/my_output"],
      "sparkSubmitParameters": "--conf spark.kubernetes.container.image=125045733377.dkr.ecr.us-east-2.amazonaws.com/djl-spark:latest --conf spark.executor.instances=2 --conf spark.executor.memory=2G --conf spark.executor.cores=4 --conf spark.driver.memory=1G --conf spark.driver.cores=1 --conf spark.hadoop.fs.s3a.connection.maximum=1000"
    }
  }'
```

This is the expected output:

```
+-------------------------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------+
|origin                                                       |topK                                                                                                                                                           |
+-------------------------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------+
|s3://djl-ai/resources/demo/spark/image_classification/car.jpg|{n03770679 minivan -> 0.8499245047569275, n02814533 beach wagon, station wagon, wagon, estate car, beach waggon, station waggon, waggon -> 0.04071871191263199}|
|s3://djl-ai/resources/demo/spark/image_classification/dog.jpg|{n02085936 Maltese dog, Maltese terrier, Maltese -> 0.7925896644592285, n02113624 toy poodle -> 0.11378670483827591}                                           |
|s3://djl-ai/resources/demo/spark/image_classification/cat.jpg|{n02123394 Persian cat -> 0.9232246279716492, n02127052 lynx, catamount -> 0.05277140066027641}                                                                |
+-------------------------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------+
```
