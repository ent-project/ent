package org.ent.dev.hyper;

public abstract class HyperDefinition<T> {

    protected final String name;

    protected final String type;

    public HyperDefinition(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public abstract HyperDefinition<T> cloneWithName(String otherName);

}
