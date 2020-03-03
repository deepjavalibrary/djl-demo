# Defining the Character-level CNN Imperative Model

This section provides information on defining the character-level convolutional neural network imperative model by using the Deep Java Library block API.

The model architecture is as follows:

```
  (0): Conv1D(None -> 256, kernel_size=(7,), stride=(1,))
  (1): MaxPool1D(size=(3,), stride=(3,), padding=(0,), ceil_mode=False)
  (2): Conv1D(None -> 256, kernel_size=(7,), stride=(1,))
  (3): MaxPool1D(size=(3,), stride=(3,), padding=(0,), ceil_mode=False)
  (4): Conv1D(None -> 256, kernel_size=(3,), stride=(1,))
  (5): Conv1D(None -> 256, kernel_size=(3,), stride=(1,))
  (6): Conv1D(None -> 256, kernel_size=(3,), stride=(1,))
  (7): Conv1D(None -> 256, kernel_size=(3,), stride=(1,))
  (8): MaxPool1D(size=(3,), stride=(3,), padding=(0,), ceil_mode=False)
  (9): Flatten
  (10): Dense(None -> 1024, Activation(relu))
  (11): Dropout(p = 0.5)
  (12): Dense(None -> 1024, Activation(relu))
  (13): Dropout(p = 0.5)
  (14): Dense(None -> 2, linear)

```

The input shape is (69,1014), where 69 is the total number of characters and 1014 is the maximum string length. The NDArray is one-hot encoded per character. 

The output layer represents two possible classifications, malicious or benign. The convolutional layers have 256 single-dimensional filters of varying kernel sizes. They feed into FullyConnected layers after pooling.


## Defining the model in DJL

DJL uses a block level definition of various operators. Each block can have sub-blocks. Individual blocks have their own parameters defined. The model consists of a single block that contains many sub-blocks.

```java
// Declare a model
Model model = model.newInstance();

// Define primary block

SequentialBlock mainBlock = new SequentialBlock();
        float dropoutProbability = (float) 0.5;
        int fullyConnected = 1024;
        int numberOfFilters = 256;
        /**
        * Every block returns a NDList which feeds into the consecutive Block
        * 1D Pooling is defined as a LambdaBlock 
        */
        mainblock.add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(7))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        ndList ->
                                new NDList(
                                        Pool.maxPool(
                                                ndList.singletonOrThrow(),
                                                new Shape(3),
                                                new Shape(3),
                                                new Shape(0))))
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(7))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        ndList ->
                                new NDList(
                                        Pool.maxPool(
                                                ndList.singletonOrThrow(),
                                                new Shape(3),
                                                new Shape(3),
                                                new Shape(0))))
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(3))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(3))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(3))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        new Conv1D.Builder()
                                .setKernel(new Shape(3))
                                .setNumFilters(numberOfFilters)
                                .build())
                .add(Activation.reluBlock())
                .add(
                        ndList ->
                                new NDList(
                                        Pool.maxPool(
                                                ndList.singletonOrThrow(),
                                                new Shape(3),
                                                new Shape(3),
                                                new Shape(0))))
                .add(Blocks.batchFlattenBlock())
                .add(new Linear.Builder().setOutChannels(fullyConnected).build())
                .add(Activation.reluBlock())
                .add(new Dropout.Builder().optProbability(dropoutProbability).build())
                .add(new Linear.Builder().setOutChannels(fullyConnected).build())
                .add(Activation.reluBlock())
                .add(new Dropout.Builder().optProbability(dropoutProbability).build())
                .add(new Linear.Builder().setOutChannels(2).build());

//Set the mainblock as model's starting point

model.setBlock(mainBlock);

```
