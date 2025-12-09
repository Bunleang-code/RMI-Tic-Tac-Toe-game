package model;

import java.io.Serializable;
import java.util.UUID;

public class Player implements Serializable {

    private final String id;
    private final String name;
    private final char symbol;

    public Player(String name, char symbol) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.symbol = symbol;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public char getSymbol() {
        return symbol;
    }
}
