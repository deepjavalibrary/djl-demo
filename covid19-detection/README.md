# Covid19 Detection

This is an example to demonstrate how to use [Deep Java Library](djl.ai) to detect COVID-19 based on X-ray images.

For more details, please follow this [blog post]()

## Setup
run `./gradlew build`

## Run prediction on images
run `./gradlew run -Dmain=com.examples.Covid19Detection --args="/path/to/saved/model /path/to/image`