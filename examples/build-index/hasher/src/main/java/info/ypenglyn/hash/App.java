package info.ypenglyn.hash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import info.ypenglyn.lsh.SuperBitHash;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
