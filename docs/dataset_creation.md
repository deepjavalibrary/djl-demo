# Creating a CSV Reading Dataset

# Introduction
In this document, you learn how to create a dataset class that can prepare data from a .csv file to be consumed during batch training. To learn more about training, refer to the [training document](training_model.md).

This example uses a .csv file to store the data. The .csv is available under this [third-party repository](https://github.com/incertum/cyber-matrix-ai/tree/master/Malicious-URL-Detection-Deep-Learning).

The CSV file has the following format.

|url   | isMalicious  |
|---|---|
| sample.url.good.com  | 0  |
| sample.url.bad.com  | 1  |


# DJL dataset blueprints

DJL by default provides abstract classes for different styles of Datasets.

1. RandomAccessDataset - A dataset that supports random access of data it using indices.
2. AbstractImageFolder - A dataset to load images from folder structures.
3. ArrayDataSet - An array based extension of RandomAccessDataset.

To read the CSV file, implement a CSVDataset class that extends RandomAccessDataset. The Dataset APIs follow the builder pattern.

## The CSVDataset


The CSVDataset definition looks like the following.


```java
public class CSVDataset extends RandomAccessDataset {
    private static final int FEATURE_LENGTH = 1014;
    private static final String ALL_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789-,;.!?:'\"/\\|_@#$%^&*~`+ =<>()[]{}";
    private List<Character> alphabets;
    private Map<Character, Integer> alphabetsIndex;
    private List<CSVRecord> dataset;

    private Shape initializeShape;
```

The CSVDataset class defines the parameters needed to process the input CSV entry into an encoded NDArray.

Every RandomAccessDataSet extension needs to implement a per-index getter method. The getter method returns a Record object that consists of an encoded input and label.

 ```java
    /** {@inheritDoc} */
    @Override
    public Record get(NDManager manager, long index) {
        NDList datum = new NDList();
        NDList label = new NDList();
        CSVRecord record = dataset.get(Math.toIntExact(index));
        // Get a single data, label pair, encode them using helpers
        datum.add(encodeData(manager, record.get("url")));
        label.add(encodeLabel(manager, record.get("isMalicious")));
        return new Record(datum, label);
    }
```

The ```encodeData()```  method encodes the input text into NDArrays. The following example implements a one-hot encoding based on the work described in [Character-level Convolutional Networks for Text Classification](https://arxiv.org/abs/1509.01626).

```java
/**
 * Convert the URL string to NDArray encoded form
 *
 * @param manager NDManager for NDArray context
 * @param url URL in string format
 */
private NDArray encodeData(NDManager manager, String url) {
    NDArray encoded = manager.zeros(new Shape(alphabets.size(), FEATURE_LENGTH));
    char[] arrayText = url.toCharArray();
    for (int i = 0; i < url.length(); i++) {
        if (i > FEATURE_LENGTH) {
            break;
        }
        if (alphabetsIndex.containsKey(arrayText[i])) {
            encoded.set(new NDIndex(alphabetsIndex.get(arrayText[i]), i), 1);
        }
    }
    return encoded;
}
```

Define  a ```builder``` class, which initializes the dataset object for TRAIN or TEST subsets.

```java
public static final class Builder extends BaseBuilder<Builder> {
        private Usage usage;
        List<CSVRecord> dataset;

        Builder() {
            this.usage = Usage.TRAIN;
        }

        protected Builder self() {
            return this;
        }

        Builder optUsage(Usage usage) {
            this.usage = usage;
            return this;
        }

        CSVDataset build() throws IOException {
            String csvFileLocation = "src/main/resources/malicious_url_data.csv";
            try (Reader reader = Files.newBufferedReader(Paths.get(csvFileLocation));
                    CSVParser csvParser =
                            new CSVParser(
                                    reader,
                                    CSVFormat.DEFAULT
                                            .withHeader("url", "isMalicious")
                                            .withFirstRecordAsHeader()
                                            .withIgnoreHeaderCase()
                                            .withTrim())) {
                List<CSVRecord> csvRecords = csvParser.getRecords();
                int index = (int)(csvRecords.size() * 0.8);
                // split the dataset into training and testing
                switch (usage) {
                    case TRAIN: {
                        dataset = csvRecords.subList(0, index);
                        break;
                    }
                    case TEST: {
                        dataset = csvRecords.subList(index, csvRecords.size());
                        break;
                    }
                }
                return new CSVDataset(this);
            }
        }
    }
```

The following code illustrates a typical call flow to declare a dataset object based on CSVDataset.

```java
// For train subset
int batchSize = 128;
CSVDataset csvDataset =
       new CSVDataset.Builder().optUsage(Usage.TRAIN).setSampling(batchSize, true).build();
```
After this, pass the dataset object to the trainer object. For more information, see the [training documentation](training_model.md).
    
