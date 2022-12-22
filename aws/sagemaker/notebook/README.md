# Use DJL Notebook on SageMaker Notebook

Amazon SageMaker Notebook is a all-you-can host Notebook instance with support for different ML tasks. Currently DJL contains Scala and Java notebooks that can be easily run on SageMaker Notebook. Please follow the instructions below to setup. You can choose between "Setup Java" and "Setup Scala" to setup for the corresponding environment.

## Setup Java

Let's start by spawn a terminal in JupyterLab.

We will use [IJava](https://github.com/SpencerPark/IJava) Kernel for Java API. You can run the following command to install it.

```bash
curl -L https://github.com/SpencerPark/IJava/releases/download/v1.3.0/ijava-1.3.0.zip -o ijava-kernel.zip &> /dev/null
unzip -q ijava-kernel.zip -d ijava-kernel && cd ijava-kernel && python3 install.py --sys-prefix &> /dev/null
jupyter kernelspec list
```

Wait for approximately 20 second and refresh the JupyterLab. You will see the Java kernel available on the right-hand side.

Now, you can try with different Notebooks provided by DJL:

- [DJL Jupyter Notebooks](https://github.com/deepjavalibrary/djl/tree/master/jupyter)
- [Dive into Deep Learning (DJL edition)](https://github.com/deepjavalibrary/d2l-java)

## Setup Scala

Let's start by spawn a terminal in JupyterLab.

We need to install the [Almond](https://almond.sh/)(Scala Jupyter Kernel). You can run the following command to install it:

```bash
curl -Lo coursier https://git.io/coursier-cli && chmod +x coursier
./coursier launch --fork almond --scala 2.12.10 -- --install && rm -f coursier
jupyter kernelspec list
```

Wait for approximately 20 second and refresh the JupyterLab. You will see the Scala kernel available on the right-hand side.

Now you can try with our Scala Notebooks provided by DJL:

- [DJL Spark application](https://github.com/deepjavalibrary/djl-demo/tree/master/apache-spark/notebook/Image_Classification_Spark.ipynb)

## Use GPU on Notebook

Currently DJL offering several GPU computing platforms. Please go do our main [docsite](http://docs.djl.ai/) to check CUDA support for different DL engines. To switch between different CUDA version in SageMaker Notebook through opening a terminal and follow [this instruction](https://docs.aws.amazon.com/dlami/latest/devguide/tutorial-base.html).
