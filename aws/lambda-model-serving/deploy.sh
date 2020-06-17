#!/bin/bash
set -eo pipefail

echo "Deploying lambda to AWS"

if [ ! -f "bucket-name.txt" ]; then
  BUCKET_ID=$(dd if=/dev/random bs=8 count=1 2>/dev/null | od -An -tx1 | tr -d ' \t\n')
  BUCKET_NAME=djl-lambda-$BUCKET_ID
  echo "$BUCKET_NAME" >bucket-name.txt
  aws s3 mb s3://$BUCKET_NAME
else
  BUCKET_NAME=$(cat bucket-name.txt)
fi

aws cloudformation package --template-file template.yml --s3-bucket $BUCKET_NAME --output-template-file out.yml
aws cloudformation deploy --template-file out.yml --stack-name djl-lambda --capabilities CAPABILITY_NAMED_IAM
