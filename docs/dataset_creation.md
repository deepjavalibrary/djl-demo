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