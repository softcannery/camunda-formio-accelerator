package cucumber.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Payload {

    public String readPayloadFromFile(String fileName) {
        String result = null;
        String pathToPayload = "./src/test/resources/payloads/" + fileName;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(pathToPayload));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            reader.close();
            result = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
