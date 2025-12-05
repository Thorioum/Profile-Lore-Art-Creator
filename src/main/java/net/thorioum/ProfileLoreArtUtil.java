package net.thorioum;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import static net.thorioum.TiledImage.hashImage;
import static net.thorioum.TiledImage.imageTextureCache;

public class ProfileLoreArtUtil {

    private static Thread processingThread = null;
    public static void process(File imageFile, String apiKey, Consumer<String> outputWindow) throws Exception {
        BufferedImage fullImage = ImageIO.read(imageFile);

        //convert the full image into tiles of 8x8, as each player head is this resolution
        TiledImage tiledImage = new TiledImage(fullImage);
        int rows = tiledImage.getNumberOfRows();
        int cols = tiledImage.getNumberOfCols();

        if(processingThread != null) {
            outputWindow.accept("Busy processing your last request! Either restart the program or wait for the last task to complete.");
            return;
        }

        //ugly code i know
        processingThread = new Thread(() -> {
            List<String> textures = new ArrayList<>();

            try {
                outputWindow.accept("[--------------------]\nDue to API Ratelimits, this will take some time to process.\nGiven the size of your image, you can expect this to take up to ~" + 3.1 * tiledImage.images.size() + " seconds");
                tiledImage.forEachRow((rowImages) -> {
                    for (BufferedImage image : rowImages) {

                        try {
                            String imageHash = hashImage(image);

                            String encodedImage = asEncodedUrl(image);
                            if(imageTextureCache.containsKey(imageHash)) {

                                textures.add(imageTextureCache.get(imageHash));
                                outputWindow.accept(textures.size() + "/" + tiledImage.images.size());

                            } else {
                                String skin = MineskinApiHelper.requestSkin(encodedImage, "LoreArt" + imageHash.substring(0, 10), apiKey);
                                JsonObject o;

                                try {
                                    o = JsonParser.parseString(skin).getAsJsonObject();
                                } catch (Exception e) {
                                    System.out.println(skin);
                                    e.printStackTrace();
                                    continue;
                                }

                                if (o.get("success").getAsString().equals("true")) {
                                    String textureValue = o.getAsJsonObject("skin")
                                            .getAsJsonObject("texture")
                                            .getAsJsonObject("data")
                                            .get("value").getAsString();

                                    textures.add(textureValue);
                                    outputWindow.accept(textures.size() + "/" + tiledImage.images.size());

                                    imageTextureCache.put(imageHash, textureValue);

                                    Thread.sleep(3000);
                                } else {
                                    outputWindow.accept(skin);
                                }

                                //outputWindow.accept(skin);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                LoreArtBuilder loreArtBuilder = new LoreArtBuilder(textures, rows, cols);
                String textComponent = loreArtBuilder.build();
                outputWindow.accept(textComponent);
            } finally {
                processingThread = null;
            }
        });
        processingThread.start();

    }

    public static String asEncodedUrl(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ImageIO.write(image, "png", baos);
        baos.flush();

        byte[] bytes = baos.toByteArray();
        baos.close();

        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:image/png;base64," + base64;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Window::new);
    }
}