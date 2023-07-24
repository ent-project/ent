package org.ent.hyper;

public class AbstractHyperDefinition implements HyperDefinition {

    protected final String name;

    protected final String type;

    public AbstractHyperDefinition(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
