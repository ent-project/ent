package org.ent.hyper;

public class PropertyNameResolver {

    private final String group;

    public static final PropertyNameResolver IDENTITY_RESOLVER = new PropertyNameResolver(null);

    public PropertyNameResolver(String group) {
        this.group = group;
    }

    public String resolve(String propertyName) {
        if (group == null) {
            return propertyName;
        } else {
            return group + "." + propertyName;
        }
    }
}
