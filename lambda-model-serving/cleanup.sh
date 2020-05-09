#!/bin/bash
set -eo pipefail

STACK=djl-lambda
FUNCTION=$(aws cloudformation describe-stack-resource --stack-name $STACK --logical-resource-id function --query 'StackResourceDetail.PhysicalResourceId' --output text)
aws cloudformation delete-stack --stack-name $STACK

echo "Deleted $STACK stack."

if [ -f bucket-name.txt ]; then
    BUCKET_NAME=$(cat bucket-name.txt)
    if [[ ! $BUCKET_NAME =~ djl-lambda-[a-z0-9]{16} ]] ; then
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

while true; do
    read -p "Delete function log group (/aws/lambda/$FUNCTION)? (y/n)" response
    case $response in
        [Yy]* ) aws logs delete-log-group --log-group-name /aws/lambda/$FUNCTION; break;;
        [Nn]* ) break;;
        * ) echo "Response must start with y or n.";;
    esac
done

rm -f out.yml out.json
