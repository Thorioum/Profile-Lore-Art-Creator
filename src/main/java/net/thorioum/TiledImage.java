package net.thorioum;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TiledImage {

    //key: image hash, value: uploaded image texture
    public static Map<String,String> imageTextureCache = new HashMap<>();

    List<BufferedImage> images;
    public final int tilesX, tilesY;

    public TiledImage(BufferedImage input) {
        //even though we just need a player head, mineskin api takes in whole skin images
        //so we create a skin image, then just draw over the 8x8 that the player head represents
        int tileSize = 8;
        int outTileSize = 64;
        int offset = 8;

        int width = input.getWidth();
        int height = input.getHeight();

        tilesX = (width  + tileSize - 1) / tileSize;
        tilesY = (height + tileSize - 1) / tileSize;
        images = new ArrayList<>(tilesX * tilesY);

        for (int ty = 0; ty < tilesY; ty++) {
            for (int tx = 0; tx < tilesX; tx++) {

                BufferedImage outTile = new BufferedImage(
                        outTileSize, outTileSize, BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g = outTile.createGraphics();

                try {
                    g.setComposite(AlphaComposite.Clear);
                    g.fillRect(0, 0, tileSize, tileSize);

                    g.setComposite(AlphaComposite.Src);
                    g.setColor(Color.BLACK);
                    g.fillRect(8, 8, 8, 8);

                    g.setComposite(AlphaComposite.SrcOver);

                    int srcX = tx * tileSize;
                    int srcY = ty * tileSize;

                    int srcW = Math.min(tileSize, width  - srcX);
                    int srcH = Math.min(tileSize, height - srcY);

                    if (srcW > 0 && srcH > 0) {
                        g.drawImage(
                                input,
                                offset,
                                offset,
                                offset + srcW,
                                offset + srcH,
                                srcX,
                                srcY,
                                srcX + srcW,
                                srcY + srcH,
                                null
                        );
                    }
                } finally {
                    g.dispose();
                }


                images.add(outTile);
            }
        }
    }

    public int getNumberOfRows() {
        int total = images.size();
        return (total + tilesX - 1) / tilesX;
    }
    public int getNumberOfCols() {
        int total = images.size();
        return (total + tilesY - 1) / tilesY;
    }

    public void forEachRow(Consumer<List<BufferedImage>> consumer) {
        int total = images.size();
        for (int i = 0; i < total; i += tilesX) {
            int end = Math.min(i + tilesX, total);
            List<BufferedImage> row = images.subList(i, end);
            consumer.accept(row);
        }
    }

    //for the name on mineskin
    public static String hashImage(BufferedImage image) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            int width = image.getWidth();
            int height = image.getHeight();

            digest.update(intToBytes(width));
            digest.update(intToBytes(height));

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);
                    digest.update(intToBytes(argb));
                }
            }

            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static byte[] intToBytes(int value) {
        return new byte[] {
                (byte) ((value >>> 24) & 0xFF),
                (byte) ((value >>> 16) & 0xFF),
                (byte) ((value >>> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }
}
