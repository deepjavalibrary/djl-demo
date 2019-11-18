# Creating a CSV Reading Dataset

# Introduction
In this document we discuss how to create a dataset class that prepares data from the CSV file, to be consumed during batch training. To learn more about training refer to the [training document](training_model.md).

We use a csv file to store our dataset. This available under [dataset/malicious_url_data.csv](../dataset/malicious_url_data.csv)

The CSV file is of the format

|url   | isMalicious  |
|---|---|
| sample.url.good.com  | 0  |
| sample.url.bad.com  | 1  |


# DJL Dataset blueprints

DJL by default provides abstract classes for different styles of Datasets

1. RandomAccessDatset - A dataset that supports random access of data from dataset , by using indices.
2. AbstractImageFolder - A dataset to load images from folder structures.
3. ArrayDataSet - An array based extension of RandomAccessDataset.

For reading the CSV file we implement a CSVDataset class that extends RandomAccessDataset. The Dataset APIs follow the builder pattern.

## CSVDataset


The CSVDataset definition looks like the following.


```java
public class CSVDataset extends RandomAccessDataset {
    // All characters we recogonize using CharCNN
    private String allChars =
            "abcdefghijklmnopqrstuvwxyz0123456789-,;.!?:'\"/\\|_@#$%^&*~`+ =<>()[]{}";
    //Maximum length of input text
    private static final int FEATURE_LENGTH = 1014;
    private List<Character> alphabets;
    private Map<Character, Integer> alphabetsIndex;
    private NDManager manager;
    private Usage usage;
    // All CSV entries from CSV file.
    private List<CSVRecord> csvRecords;
    // TRAIN vs TEST split for records
    private List<CSVRecord> subRecord;
    //Size of current instance, depends on Dataset Split
    private int size;
```

The CSVDataset class defines, all the parameters , needed to process the input CSV entry into encoded NDArray.

Every RandomAccessDataSet extension needs to implement a per index getter method. The getter method returns a Record object, that consists of a encoded input and label.

 ```java
    /** {@inheritDoc} */
    @Override
    public Record get(NDManager manager, long index) {
        NDList datum = new NDList();
        NDList label = new NDList();
        // From the TRAIN or TEST split, get a record at index
        CSVRecord record = subRecord.get((int) index);
        // Get a single data, label pair, encode them using helpers
        datum.add(encodeData(record.get("url")));
        label.add(encodeLabel(record.get("isMalicious")));
        datum.attach(manager);
        label.attach(manager);
        return new Record(datum, label);
    }
```

The ```encodeData()```  method encodes the input text into NDArrays. Based on the [Character CNNs paper's](https://arxiv.org/abs/1509.01626), we implement a one-hot encoding.

```java
 /**
     * Convert the URL string to NDArray encoded form
     *
     * @param url URL in string format
     */
    private NDArray encodeData(String url) {
        FloatBuffer buf = FloatBuffer.allocate(alphabets.size() * FEATURE_LENGTH);
        char[] arrayText = url.toCharArray();
        for (int i = 0; i < url.length(); i++) {
            if (i > FEATURE_LENGTH) {
                break;
            }
            if (alphabetsIndex.containsKey(arrayText[i])) {
                int index = arrayText[i] * alphabets.size() + i;
                buf.put(index, 1);
            }
        }
        return manager.create(buf, new Shape(alphabets.size(), FEATURE_LENGTH));
    }
```

We also define a ```prepareData()``` method, which initializes the dataset object for TRAIN or TEST subsets.

```java
    /**
     * Divide CSVRecords to TRAIN and TEST datasets Needed subuset is set
     *
     * @param usage TRAIN or TEST usage
     */
    void prepareData(Usage usage) {
        // 80% of records for TRAIN rest for TEST
        int splitIndex = (int) (csvRecords.size() * 0.8);
        switch (usage) {
            case TRAIN:
                {
                    subRecord = csvRecords.subList(0, splitIndex);
                    size = subRecord.size();
                    break;
                }
            case TEST:
                {
                    subRecord = csvRecords.subList(splitIndex, csvRecords.size());
                    size = subRecord.size();
                    break;
                }
            default:
                {
                    throw new RuntimeException("Wrong usage passed");
                }
        }
    }
```

A typical call flow for declaring a dataset object based on CSVDataset would be as follows.

```java
// For train subset
int batchSize = 128;
CSVDataset trainSet = CSVDataset.builder(manager).optUsage(Usage.TRAIN).setSampling(batchSize, true).build();
//prepare for trainset
trainset.prepareData();
```
Pass the dataset object to Trainer object, after this, refer [training documentation](training_model.md) for more information.

    