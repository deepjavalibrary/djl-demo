# Sentence Encoding using Apache Flink


This project runs a Sentence Encoding application with [Apache Flink](https://flink.apache.org/) Stream API.

You can interactively type in sentences to simulate stream data,
and Flink will process and show the result in the terminal.

The model is from TFHub [Universal Sentence Encoder](https://tfhub.dev/google/universal-sentence-encoder/4).
## Step to run

Firstly you need to create a simple text server by doing the following in the terminal:

### Linux/Mac

```
nc -l 9000
```

### Windows

```
nc -l -p 9000
```

After that you can open another terminal in the project and run the follows:

```
./gradlew run --args="--port 9000"
```

On Windows use `gradlew.bat` instead.

Try to type in anything from your text server:

```
$ nc -l 9000
The quick brown fox jumps over the lazy dog.
```

result shows on Flink application:

```
Loading:     100% |████████████████████████████████████████|
[-0.031330183, -0.06338634, -0.016074996, -0.010348981, -0.046500977, 0.03723154, 0.0059158537, ...]
```
