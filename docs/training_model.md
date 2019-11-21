# Training the Character-level CNN model on CSV Dataset

This document covers Training on an imperative model. To learn about defining an imperative model, refer to this [document](define_model.md).


To simply train the model as is, run the following command 



```bash
# Download the dataset
$ wget -O src/main/resources/malicious_url_data.csv https://raw.githubusercontent.com/incertum/cyber-matrix-ai/master/Malicious-URL-Detection-Deep-Learning/data/url_data_mega_deep_learning.csv
# Run train command
$ ./gradlew train
```
The default training runs for 10 epochs. with pre-set training configurations, we discuss in detail below.


## Training Configuration

DJL provides a [TrainingConfig](https://github.com/awslabs/djl) class to define hyperparameters for training. Hyperparameters including learning rate, optimizer is provided to the Trainer object using the TrainingConfig

For example
```java
    /**
    *Learning Rate definition, FactorTracker is a parent for simimlar hyperparameteres
    * Sets learningRate and number of steps
    */
    int learningRate = 0.01;
    int stepSize = inputDataSize/batchSize;
    FactorTracker factorTracker =
            LearningRateTracker.factorTracker()
                    .optBaseLearningRate(learningRate)
                    .setStep(stepSize)
                    .build();
    // Setting optimizer object, in this case build an SGD optimizer with momentum
    Optimizer optimizer =
            Optimizer.sgd()
                    .setRescaleGrad(1.0f / batchSize)
                    .setLearningRateTracker(factorTracker)
                    .optWeightDecays(0.00001f)
                    .optMomentum(0.9f)
                    .build();

    // Define loss and Initializer 
     Loss loss = Loss.softmaxCrossEntropyLoss();
     Initializer initializer = new XavierInitializer(
                                XavierInitializer.RandomType.UNIFORM,
                                XavierInitializer.FactorType.AVG,
                                2.24);
     //Set distribution of Initializer Randamozier to uniform and Factor to be average, with magintude.
     
    //Use the above to create a TrainingConfig object
  
    TrainingConfig trainingConfig = new DefaultTrainerConfig(initializer, loss)
                                       .setOptimizer(optimizer)
                                        .setBatchSize(batchSize)
                                       .setDevices(new Device[] {Device.defaultDevice()});
```
This training Configuration is then used to initialize the Trainer object, which in turn initializes the parameters of the model.

## Trainer 

The Trainer, is a per model instance object, that provides, training functionality using simple API

To initialize trainer and use trainer.

```java
//Define and load model
Model model = Model.newInstance();
model.load(modelName, modelName);
//create a Trainer with the Training Config
Trainer trainer = model.newTrainer(trainingConfig);


//initialize the parameters , pass shape of input.
trainer.initialize(inputShape);

//train on dataSet for epochs

for (int epoch = 0; epoch < 10; epoch++) {
   for (Batch batch: trainer.iterateDataset(trainDataset) { // trainDataset is a Dataset Object, containing TRAIN split
       trainer.trainBatch(batch);
       trainer.step();
       batch.close();
    }
   //Validate batch with Validate split
    for (Batch batch : trainer.iterateDataset(validateDataset)) {
        trainer.validateBatch(batch);
        batch.close();
    }
    // Save Model after current epoch
    Model model = trainer.getModel();
    model.setProperty("Epoch", String.valueOf(epoch));
    model.save(Paths.get(outputDir), "modelName");
}
```

As shown above , the trainer object, manages the lifcycle of training, form initialization to backPropagation on model. Trainer handles, loading dataset and batching during training.