package sample.app;

/**
 * Created by Wienio on 2018-06-05.
 */
public class Cell {

    private int state = 0;
    private Grain grain;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Grain getGrain() {
        return grain;
    }

    public void setGrain(Grain grain) {
        this.grain = new Grain();
        this.grain.setState(grain.getState());
        this.grain.setColor(grain.getColor());
    }

    public Cell() {

    }

    public Cell(int state) {
        this.state = state;
        grain = new Grain(state);
    }

}
