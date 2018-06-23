package sample.app;

import javafx.scene.paint.Color;

import java.util.Random;

/**
 * Created by Wienio on 2018-06-05.
 */
public class Grain {

    private Random random = new Random();
    private int state;
    private Color color;

    public Grain(int state) {
        this.state = state;
        this.color = Color.rgb(random.nextInt(255 + 1 - 10) + 10, random.nextInt(255 + 1 - 10) + 10, random.nextInt(255 + 1 - 10) + 10);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Grain() {
        
    }
}
