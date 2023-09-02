package org.ent.dev.randnet;

public class DefaultValueDrawing extends AbstractValueDrawing {
    public DefaultValueDrawing() {
        super();
        defaultValueInitialization();
    }

    @Override
    protected void initializeValues(ValueCollector collector) {
        defaultInitializeValues(collector);
    }
}
