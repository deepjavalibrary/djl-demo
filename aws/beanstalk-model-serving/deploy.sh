#!/bin/bash
set -eo pipefail

echo "Deploying package to AWS Elastic Beanstalk"

# Create S3 Bucket
if [ ! -f "bucket-name.txt" ]; then
  BUCKET_ID=$(dd if=/dev/random bs=8 count=1 2>/dev/null | od -An -tx1 | tr -d ' \t\n')
  BUCKET_NAME=djl-eb-$BUCKET_ID
  echo "$BUCKET_NAME" >bucket-name.txt
  aws s3 mb s3://$BUCKET_NAME
else
  BUCKET_NAME=$(cat bucket-name.txt)
fi

# Upload jar to S3 Bucket
aws s3 cp build/libs/beanstalk-model-serving-0.0.1-SNAPSHOT.jar s3://$BUCKET_NAME/

# Create EC2 instance profile role: djl-elasticbeanstalk-role
aws iam create-role --role-name djl-elasticbeanstalk-role --assume-role-policy-document file://elasticbeanstalk-role.json
aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AWSElasticBeanstalkWebTier --role-name djl-elasticbeanstalk-role
aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AWSElasticBeanstalkWorkerTier --role-name djl-elasticbeanstalk-role
aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AWSElasticBeanstalkMulticontainerDocker --role-name djl-elasticbeanstalk-role

aws elasticbeanstalk create-application --application-name djl-demo-app
aws elasticbeanstalk create-application-version --application-name djl-demo-app --version-label v1 --source-bundle S3Bucket=$BUCKET_NAME,S3Key=beanstalk-model-serving-0.0.1-SNAPSHOT.jar
aws elasticbeanstalk create-configuration-template --application-name djl-demo-app --template-name djl-template-v1 --solution-stack-name "64bit Amazon Linux 2 v3.0.2 running Corretto 11"
aws elasticbeanstalk create-environment --cname-prefix $BUCKET_NAME --application-name djl-demo-app --template-name djl-template-v1 --version-label v1 --environment-name djl-demo-env --option-settings file://options.json

REGION=`aws configure get region`

echo "==================================================="
echo "Elastic Beanstalk instance created successful. It may takes a few minutes to be available."
echo "Use 'aws elasticbeanstalk describe-environments --environment-names djl-demo-env' check instance status."
echo ""
echo "Visit your Elastic Beanstalk instance: http://$BUCKET_NAME.$REGION.elasticbeanstalk.com"
