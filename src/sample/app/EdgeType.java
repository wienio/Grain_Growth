package sample.app;

/**
 * Created by Wienio on 2018-05-24.
 */
public enum EdgeType {

    CLOSED("Closed"), PERIODIC("Periodic");

    private final String name;

    EdgeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EdgeType getByName(String name) {
        for (EdgeType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

}
