# CTR prediction using Apache Beam and Deep Java Library(DJL)

In this demo, we will show you how to run CTR prediction on Ads Data at scale using [Apache Beam](https://beam.apache.org/) and [Deep Java Library](https://djl.ai)

Utilizing Apache Beam, you can train you model once and deploy using different big data solutions([Flink](https://flink.apache.org/), [Spark](https://spark.apache.org/), and more) that suits your needs. 


## Train your model
To Learn more background about Deep Learning and training,
we highly recommend you read the [Dive into Deep Learning book](https://d2l.ai).

The model we used is based on [DeepFM](https://arxiv.org/abs/1703.04247), you can read more about it in chapter: 
[Feature-Rich Recommender Systems](https://d2l.ai/chapter_recommender-systems/ctr.html)
and [Deep Factorization Machines](https://d2l.ai/chapter_recommender-systems/deepfm.html)

We have prepared a trained model [here](https://djl-ai.s3.amazonaws.com/resources/demo/mxnet/deepfm.zip)

## Deploy using Apache Beam and DJL

###1. Prepare model

Download the above [trained model](https://djl-ai.s3.amazonaws.com/resources/demo/mxnet/deepfm.zip) and unzip it, or train your own model.
The model folder will contain the following files:
```bash
deepfm
├── deepfm-0001.params
├── deepfm-symbol.json
└── feature_map.json 
```
###2. Download test data
You can download the sample dataset provided by the book from [here](http://d2l-data.s3-accelerate.amazonaws.com/ctr.zip).
Unzip it and you will have the following files:
```bash
ctr
├── test.csv
└── train.csv
```

###3. Run inference using Apache Beam
You can run inference using different runners supported by Apache Beam. Refer to Apache Beam [Documentation](https://beam.apache.org/get-started/beam-overview/) for more runners.

Here we will show you local runner, Flink runner, and Spark runner:

Remember to specify:
* the main class `CtrPrediction`
* model location using `-Dai.djl.repository.zoo.location=./deepfm`
* input file location `--inputFile=./ctr/test.csv`


#### Using local runner
```bash
 mvn compile  exec:java -Dexec.mainClass=ctr_prediction.CtrPrediction -Dai.djl.repository.zoo.location=./deepfm -Dexec.args="--inputFile=./ctr/test.csv" -Pdirect-runner
```


#### Using Flink runner
```bash
mvn compile  exec:java -Dexec.mainClass=ctr_prediction.CtrPrediction -Dai.djl.repository.zoo.location=./deepfm -Dexec.args="--runner=FlinkRunne --inputFile=./ctr/test.csv" -Pflink-runner
```


#### Using Spark runner
```bash
mvn compile  exec:java -Dexec.mainClass=ctr_prediction.CtrPrediction -Dai.djl.repository.zoo.location=./deepfm -Dexec.args="--runner=SparkRunner --inputFile=./ctr/test.csv" -Pspark-runner
```


You can find the results in "ctr-*" files.
First column is record ids, and second column is predicted ctr.
```bash
d3bb711c-fe45-4cf9-a73e-70ff0bb2bee3	1.0
bc48f39c-e779-47dc-9924-5100ebca8b5e	0.99999917
87b5fb12-c990-499b-b67a-986b46122d1e	1.0
```

