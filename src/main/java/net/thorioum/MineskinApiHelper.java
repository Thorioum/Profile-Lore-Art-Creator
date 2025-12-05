package net.thorioum;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class MineskinApiHelper {

    public static String requestSkin(String b64ImageData, String skinName, String apiKey) throws IOException {
        String urlStr = "https://api.mineskin.org/v2/generate";
        String json = "{\n" +
                "  \"variant\": \"classic\",\n" +
                "  \"name\": \""+ skinName + "\",\n" +
                "  \"visibility\": \"unlisted\",\n" +
                "  \"url\": \"" + b64ImageData + "\"\n" +
                "}";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        InputStream inputStream;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
            inputStream = conn.getInputStream();
        } else {
            inputStream = conn.getErrorStream();
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        return response.toString();
    }

    private static String getRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = new Random().nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
