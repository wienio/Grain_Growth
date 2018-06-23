package sample.app;

/**
 * Created by Wienio on 2018-05-24.
 */
public enum NeighboursType {

    VONNEUMANN("Von Neumann"), MOORE("Moore"), HEXAGONAL_LEFT("Hexagonal left"), HEXAGONAL_RIGHT("Hexagonal right"), PENTAGONAL_RANDOM("Pentagonal random");

    private final String neighbourName;

    NeighboursType(String name) {
        this.neighbourName = name;
    }

    public String getNeighbourName() {
        return neighbourName;
    }

    public static NeighboursType getByName(String name) {
        for (NeighboursType type : values()) {
            if (type.getNeighbourName().equals(name)) {
                return type;
            }
        }
        return null;
    }

}
