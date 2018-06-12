package me.realized.duels.data;

import lombok.Getter;
import lombok.Setter;

public class AttributeData {

    @Getter
    private final String name;
    @Getter
    private final String attrName;
    @Getter
    private final int operation;
    @Getter
    private final double amount;
    @Getter
    @Setter
    private String slot;

    public AttributeData(String name, String attrName, int operation, double amount) {
        this.name = name;
        this.attrName = attrName;
        this.operation = operation;
        this.amount = amount;
    }
}
