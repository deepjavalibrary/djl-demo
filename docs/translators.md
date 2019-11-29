## Writing Translators

Translator classes provide a way to include methods that pre-process input and post-process output during inference. It wraps around this logic. Passing a translator to a model predictor object enables automatic invocation of this logic.


## Writing the CSV Translator

The comma separated values (CSV) Translator needs to handle the following:

   1. Pre-process input - Convert the input text to a two-dimensional NDArray. Use one-hot encoding to a shape of (AllPossibleCharacters, MaxTextlength). In this example, it's (69, 1014). For more infomation, see the [character level CNN research paper](https://arxiv.org/abs/1509.01626).
   
   2. Post-process output - Convert the output to a Classification object.
   
   
 If you extend the base Translator class where input type and output type are provided as template parameters, then the pre-process logic is very similar to the ```encodeData()``` logic in [CSVDataset](dataset_creation.md)
 
 ```java
// Get String text input and a List of Classification objects as output, for the URL translator
public class URLTranslator implements Translator<String, List<Classifications>> {
    private static final int FEATURE_LENGTH = 1014;
    private List<Character> alphabets;
    private Map<Character, Integer> alphabetsIndex;

    /**
     * URLTranslator, like the Dataset defines encoding, to pre-process incoming inference requests
     */
    URLTranslator() {
        String allChars = "abcdefghijklmnopqrstuvwxyz0123456789-,;.!?:'\"/\\|_@#$%^&*~`+ =<>()[]{}";
        // Create an empty List of character
        alphabets = allChars.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        alphabetsIndex =
                IntStream.range(0, alphabets.size()).boxed().collect(toMap(alphabets::get, i -> i));
    }

    /**
     * processInput encodes the input URL string to a 69,1014 NDArray, works like Training data
     * encoder.
     *
     * @param ctx context of the translator.
     * @param url The input url sent to the FilterProxy.
     * @return NDList of encoded NDArray
     */
    @Override
    public NDList processInput(TranslatorContext ctx, String url) {
        //one-hot encode the text to an array initialized to zeros.
        NDArray encoded = ctx.getNDManager().zeros(new Shape(alphabets.size(), FEATURE_LENGTH));
        char[] arrayText = url.toCharArray();
        for (int i = 0; i < url.length(); i++) {
            if (i > FEATURE_LENGTH) {
                break;
            }
            if (alphabetsIndex.containsKey(arrayText[i])) {
                encoded.set(new NDIndex(alphabetsIndex.get(arrayText[i]), i), 1);
            }
        }
        NDList ndList = new NDList();
        ndList.add(encoded);
        return ndList;
    }
```

The post-process method takes the result of the neural network and adds labels. These labels can be more descriptive than the ones in the dataset. The softmax on the output and labels are used to create a Classification probability list.

```java

    /**
     * Converts the Output NDArray (classification labels) to Classification objects for easy
     * formatting.
     *
     * @param ctx context of the translator.
     * @param list NDlist of prediction output
     * @return returns a list of Classification objects
     */
    @Override
    public List<Classifications> processOutput(TranslatorContext ctx, NDList list) {
        NDArray array = list.get(0);
        List<Classifications> ret = new ArrayList<>(2);
        NDArray pred = array.softmax(-1);
        List<String> labels = new ArrayList<>();
        labels.add("benign");
        labels.add("malicious");
        Classifications out = new Classifications(labels, pred);
        ret.add(out);
        return ret;
    }
```

Translators are typically attached to model predictors in order to integrate them easily with the inference call flow.

```java
URLTranslator urlTranslator = new URLTranslator(); 
// Creating a predictor with URLTranslator.
Predictor<String, List<Classifications>> predictor = model.newPredictor(urlTranslator);
```
