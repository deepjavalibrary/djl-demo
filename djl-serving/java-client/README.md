# DJLServing Java Client Example

This demo project include several examples to demonstrate how to make inference requests against DJL Serving using Java [HttpClient](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html) API.

Note that this demo assumes that DJLServing is already started. Refer [here](https://github.com/deepjavalibrary/djl-serving/blob/master/serving/docs/starting.md) on how to start DJL Serving.

## HttpUtils

This demo provides a `postRequest` utility method that can create a POST `HttpRequest` with the following arguments:

- `url`: The URL string.
- `params`: The URL parameters to append to the URL.
- `contentType`: The content type of the request.
- `data`: The body in bytes or text to attach to the request.
- `file`: The file to upload in the request.

```java
public static String postRequest(String url, Map<String, String> params, String contentType,
                                 String data, Path file) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    HttpRequest.Builder builder = HttpRequest.newBuilder();
    if (params != null) {
        int i = 0;
        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (i > 0) {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            i++;
        }
        url = sb.toString();
    }
    builder.uri(URI.create(url));

    if (contentType != null) {
        builder.header("Content-Type", contentType);
    }

    if (data != null) {
        builder.POST(HttpRequest.BodyPublishers.ofString(data));
    } else if (file != null) {
        builder.POST(HttpRequest.BodyPublishers.ofFile(file));
    } else {
        builder.POST(HttpRequest.BodyPublishers.noBody());
    }

    HttpRequest request = builder.build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
}
```

## Example 1

In the first example, let's load an [Image Classification model](https://resources.djl.ai/demo/pytorch/traced_resnet18.zip).

First we need to download the input image.

```
curl -O https://resources.djl.ai/images/kitten.jpg
```

To register the model and make predictions:

```java
String url = "https://resources.djl.ai/demo/pytorch/traced_resnet18.zip";
Map<String, String> params = Map.of("url", url, "engine", "PyTorch");
HttpUtils.postRequest("http://localhost:8080/models", params, null, null, null);

// Run inference
String response =
        HttpUtils.postRequest(
                "http://localhost:8080/predictions/traced_resnet18",
                null,
                "application/octet-stream",
                null,
                Path.of("kitten.jpg"));
System.out.println(response);
```

Run the example:

```
# start djl-serving locally
djl-serving

./gradlew run -Dmain=ai.djl.examples.serving.javaclient.DJLServingClientExample1
```

This should return the following result:

```json
[
  {
    "className": "n02123045 tabby, tabby cat",
    "probability": 0.4021684527397156
  },
  {
    "className": "n02123159 tiger cat",
    "probability": 0.2915370762348175
  },
  {
    "className": "n02124075 Egyptian cat",
    "probability": 0.27031460404396057
  },
  {
    "className": "n02123394 Persian cat",
    "probability": 0.007626926526427269
  },
  {
    "className": "n02127052 lynx, catamount",
    "probability": 0.004957367666065693
  }
]
```

## Example 2

In the second example, we load a [HuggingFace Bert QA model](https://mlrepo.djl.ai/model/nlp/question_answer/ai/djl/huggingface/pytorch/deepset/bert-base-cased-squad2/0.0.1/bert-base-cased-squad2.zip) and make predictions.

```java
String url = "djl://ai.djl.huggingface.pytorch/deepset/bert-base-cased-squad2";
Map<String, String> params = Map.of("url", url, "engine", "PyTorch");
HttpUtils.postRequest("http://localhost:8080/models", params, null, null, null);

// Run inference
Map<String, String> input =
        Map.of(
                "question",
                "How is the weather",
                "paragraph",
                "The weather is nice, it is beautiful day");
String json = new Gson().toJson(input);
String response =
        HttpUtils.postRequest(
                "http://localhost:8080/predictions/bert_base_cased_squad2",
                null,
                "application/json",
                json,
                null);
System.out.println(response);
```

Run the example:

```
# start djl-serving locally
djl-serving

./gradlew run -Dmain=ai.djl.examples.serving.javaclient.DJLServingClientExample2
```

This should return the following result:

```
nice
```

## Example 3

In the third example, we can try a [HuggingFace Fill Mask model](https://mlrepo.djl.ai/model/nlp/fill_mask/ai/djl/huggingface/pytorch/bert-base-uncased/0.0.1/bert-base-uncased.zip). Masked model inputs masked words in a sentence and predicts which words should replace those masks.

```java
String url = "djl://ai.djl.huggingface.pytorch/bert-base-uncased";
Map<String, String> params = Map.of("url", url, "engine", "PyTorch");
HttpUtils.postRequest("http://localhost:8080/models", params, null, null, null);

// Run inference
String data = "The man worked as a [MASK].";
String response =
        HttpUtils.postRequest(
                "http://localhost:8080/predictions/bert_base_uncased",
                null,
                "text/plain",
                data,
                null);
System.out.println(response);
```

Run the example:

```
# start djl-serving locally
djl-serving

./gradlew run -Dmain=ai.djl.examples.serving.javaclient.DJLServingClientExample3
```

This should return the following result:

```json
[
  {
    "className": "carpenter",
    "probability": 0.05010193586349487
  },
  {
    "className": "salesman",
    "probability": 0.027945348992943764
  },
  {
    "className": "mechanic",
    "probability": 0.02747158892452717
  },
  {
    "className": "cop",
    "probability": 0.02429874986410141
  },
  {
    "className": "contractor",
    "probability": 0.024287723004817963
  }
]
```
