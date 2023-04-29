#!/usr/bin/env python
#
# Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
# except in compliance with the License. A copy of the License is located at
#
# http://aws.amazon.com/apache2.0/
#
# or in the "LICENSE.txt" file accompanying this file. This file is distributed on an "AS IS"
# BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied. See the License for
# the specific language governing permissions and limitations under the License.

import sys
from pyspark.sql.session import SparkSession
from djl_spark.task.text import Text2TextGenerator


if __name__ == "__main__":
    """
        Usage: text2text_generation_alpaca.py [output_path]
    """
    output_path = sys.argv[1] if len(sys.argv) > 1 else None
    spark = SparkSession \
        .builder \
        .appName("Text2TextGenerationAlpacaExample") \
        .getOrCreate()
    sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    df = spark.createDataFrame(
        [
            ("1", "Give three tips for staying healthy."),
            ("2", "What are the three primary colors?"),
            ("3", "Describe the structure of an atom."),
            ("4", "How can we reduce air pollution?"),
            ("5", "Describe a time when you had to make a difficult decision.")
        ],
        ["id", "text"]
    )
    df.show(truncate=False)
    # +---+----------------------------------------------------------+
    # |id |text                                                      |
    # +---+----------------------------------------------------------+
    # |1  |Give three tips for staying healthy.                      |
    # |2  |What are the three primary colors?                        |
    # |3  |Describe the structure of an atom.                        |
    # |4  |How can we reduce air pollution?                          |
    # |5  |Describe a time when you had to make a difficult decision.|
    # +---+----------------------------------------------------------+

    # Text2Text generation using Flan-Alpaca model
    generator = Text2TextGenerator(input_col="text",
                                   output_col="prediction",
                                   hf_model_id="declare-lab/flan-alpaca-base")
    outputDf = generator.generate(df, do_sample=True, max_length=128)

    if output_path:
        print("Saving results S3 path: " + output_path)
        outputDf.write.mode("overwrite").csv(output_path)
    else:
        print("Printing results output stream")
        outputDf.printSchema()
        # root
        #  |-- id: string (nullable = true)
        #  |-- text: string (nullable = true)
        #  |-- prediction: string (nullable = true)

        outputDf.show(truncate=False)
        # +---+----------------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
        # |id |text                                                      |prediction                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
        # +---+----------------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
        # |1  |Give three tips for staying healthy.                      |Three tips for staying healthy are: 1) Practice mindful eating and snacks at home 2. Find time to do things you enjoy 3. Exercise regularly to increase quality of life.                                                                                                                                                                                                                                                                                                                                                                    |
        # |2  |What are the three primary colors?                        |The three primary colors are red, blue, and yellow.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
        # |3  |Describe the structure of an atom.                        |Atoms are objects made up of two or more protons, which form a complex bond between them. They come together together due to their structure, and they have the main structure of an atom. The atom's structure is composed of two atoms connected by a single electron, which moves between them. Generally, an atom with a double nucleus, is the most common atom in the universe.                                                                                                                                                       |
        # |4  |How can we reduce air pollution?                          |One way to reduce air pollution is to start taking public transportation, replace vehicles with electric vehicles, and switch to renewable energy sources. Regular maintenance and energy efficiency measures can also help reduce the amount of air pollutants released into the atmosphere. It is also important to increase the use of renewable energy sources, such as solar, wind, and geothermal, and increase access to air-quality education.                                                                                      |
        # |5  |Describe a time when you had to make a difficult decision.|As I got older, I had to make a difficult decision by thinking about the things I could not do. My parents were too busy at work, yet the whole family were excited to be home. One morning, I asked my parents to go out to dinner, to celebrate with me and to get me the food. I was hesitant, yet they suggested that we do it together, and the two of us decided we could do it together. I had a difficult decision to make, weighing the pros and cons, and decided that the best way to be home was to choose it instead. I did it!|
        # +---+----------------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

    spark.stop()
