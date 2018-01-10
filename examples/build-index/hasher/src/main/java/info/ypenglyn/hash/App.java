package info.ypenglyn.hash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import info.ypenglyn.lsh.SuperBitHash;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.BitSet;
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
    private static SuperBitHash quantizer1 = SuperBitHash.getNewInstance().init(784, 8, 64, 100L);
    private static SuperBitHash quantizer2 = SuperBitHash.getNewInstance().init(784, 16, 64, 100L);

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

    @RequestMapping(value = "/bits", params = "value", method = RequestMethod.POST)
    @ResponseBody
    String bits(@RequestParam("value") String value) {
        double[][] bits = gson.fromJson(value, double[][].class);
        List<String> hashed = new ArrayList<>();
        for (double[] v : bits) {
            BitSet current = hasher.hash(v);
            StringBuilder currentRawAsString = new StringBuilder();
            for (int i = 0; i < 128; i++) {
                currentRawAsString.append(current.get(i) ? '1' : '0');
            }
            hashed.add(currentRawAsString.toString());
        }
        return gson.toJson(hashed);
    }

    @RequestMapping(value = "/hash_bits", params = "value", method = RequestMethod.POST)
    @ResponseBody
    String hashbits(@RequestParam("value") String value) {
        double[][] bits = gson.fromJson(value, double[][].class);
        List<Pair> hashed = new ArrayList<>();
        for (double[] v : bits) {
            BitSet current = hasher.hash(v);
            StringBuilder currentRawAsString = new StringBuilder();
            for (int i = 0; i < 128; i++) {
                currentRawAsString.append(current.get(i) ? '1' : '0');
            }
            String encodedHash = new String(encoder.encode(current.toByteArray()));
            Pair currentPair = new Pair();
            currentPair.lcode = currentRawAsString.toString();
            currentPair.scode = encodedHash;
            hashed.add(currentPair);
        }
        return gson.toJson(hashed);
    }

    @RequestMapping(value = "/quantize", params = "value", method = RequestMethod.POST)
    @ResponseBody
    String quantize(@RequestParam("value") String value) {
        double[][] bits = gson.fromJson(value, double[][].class);
        List<Pair> hashed = new ArrayList<>();
        for (double[] v : bits) {
            BitSet current = hasher.hash(v);
            BitSet currentQ1 = quantizer1.hash(v);
            BitSet currentQ2 = quantizer2.hash(v);
            Pair currentPair = new Pair();
            currentPair.scode = new String(encoder.encode(current.toByteArray()));
            currentPair.mcode = new String(encoder.encode(currentQ1.toByteArray()));
            currentPair.lcode = new String(encoder.encode(currentQ2.toByteArray()));
            hashed.add(currentPair);
        }
        return gson.toJson(hashed);
    }

    class Pair {

        String scode;
        String mcode;
        String lcode;
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
