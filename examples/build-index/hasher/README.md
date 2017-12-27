# Hasher

Returns SB-LSH in terms of base64 string using `solr-lsh`.

```java
@Controller
@EnableAutoConfiguration
public class App {

    // NOTE: Change the dimension to match your requirement
    // NOTE: Keep seed same from indexing and searching both
    private static SuperBitHash hasher = SuperBitHash.getInstance().init(784, 8, 16, 100L);
    private static Gson gson = new GsonBuilder().create();
    private static Encoder encoder = Base64.getEncoder();

    @RequestMapping(value = "/hash", params = "value", method = RequestMethod.POST)
    @ResponseBody
    String hash(@RequestParam("value") String value) {
        double[][] bits = gson.fromJson(value, double[][].class);
        List<String> hashed = new ArrayList<>();
        for (double[] v : bits) {
            hashed.add(new String(encoder.encode(hasher.hash(v).toByteArray())));
        }
        return gson.toJson(hashed);
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

Use `SuperBitHash.getInstance()` to fetch singleton instance, which can not be used for hashing without `init`.
As can be seen, there are 4 parameters for `init` function:

- input vector dimension, eg: 784
- super bit batch size, eg: 8
- super bit depth, eg: 16
- super bit seed

With a 8 * 16 sized super bit hash, the output will be 128 bits array.