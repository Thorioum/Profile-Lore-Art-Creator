package net.thorioum;

import java.util.ArrayList;
import java.util.List;

public class LoreArtBuilder {

    private final int rows, cols;
    private final List<String> textureValues;
    public LoreArtBuilder(List<String> textureValues, int rows, int cols) {
        this.textureValues = textureValues;
        this.rows = rows;
        this.cols = cols;
    }

    public String build() {
        StringBuilder text = new StringBuilder("[");

        for(int row = 0; row < rows; row++) {
            text.append("[");
            for(int col = 0; col < cols; col++) {
                String texture = textureValues.get((row*cols)+col);
                text.append("{color:\"white\",");
                if(row != rows - 1) text.append("shadow_color:-1,");
                text.append("player:{properties:[{name:\"textures\",value:\"");
                text.append(texture);
                text.append("\"}]}}");
                if(col != cols - 1) text.append(",");
            }
            text.append("]");
            if(row != rows - 1) text.append(",");
        }


        text.append("]");
        return text.toString();
    }
}
