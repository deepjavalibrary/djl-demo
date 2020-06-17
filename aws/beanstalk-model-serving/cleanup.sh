#!/bin/bash

aws elasticbeanstalk terminate-environment --environment-name djl-demo-env
aws elasticbeanstalk delete-configuration-template --application-name djl-demo-app --template-name djl-template-v1
aws elasticbeanstalk delete-application-version --application-name djl-demo-app --version-label v1
aws elasticbeanstalk delete-application --application-name djl-demo-app
aws iam detach-role-policy --policy-arn arn:aws:iam::aws:policy/AWSElasticBeanstalkWebTier --role-name djl-elasticbeanstalk-role
aws iam detach-role-policy --policy-arn arn:aws:iam::aws:policy/AWSElasticBeanstalkWorkerTier --role-name djl-elasticbeanstalk-role
aws iam detach-role-policy --policy-arn arn:aws:iam::aws:policy/AWSElasticBeanstalkMulticontainerDocker --role-name djl-elasticbeanstalk-role
aws iam delete-role --role-name djl-elasticbeanstalk-role

if [ -f bucket-name.txt ]; then
    BUCKET_NAME=$(cat bucket-name.txt)
    if [[ ! $BUCKET_NAME =~ djl-eb-[a-z0-9]{16} ]] ; then
        echo "Bucket was not created by this application. Skipping."
    else
        while true; do
            read -p "Delete deployment artifacts and bucket ($BUCKET_NAME)? (y/n)" response
            case $response in
                [Yy]* ) aws s3 rb --force s3://$BUCKET_NAME; rm bucket-name.txt; break;;
                [Nn]* ) break;;
                * ) echo "Response must start with y or n.";;
            esac
        done
    fi
fi
