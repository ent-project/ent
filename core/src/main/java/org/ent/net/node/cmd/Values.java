package org.ent.net.node.cmd;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.ent.net.node.cmd.split.Splits;

public class Values {

    private Values() {
    }

    private static final BiMap<String, Integer> valuesByName = buildValuesByName();

    private static BiMap<String, Integer> buildValuesByName() {
        BiMap<String, Integer> result = HashBiMap.create();
        Commands.getAllCommandNames().forEach(name -> {
            if (result.containsKey(name)) {
                throw new AssertionError("duplicate name: " + name);
            }
            result.put(name, Commands.getByName(name).getValue());
        });
        Splits.getAllSplitNames().forEach(name -> {
            if (result.containsKey(name)) {
                throw new AssertionError("duplicate name: " + name);
            }
            result.put(name, Splits.getByName(name).getValue());
        });
        return result;
    }

    public static Integer getByName(String name) {
        return valuesByName.get(name);
    }

    public static String getName(int value) {
        return valuesByName.inverse().get(value);
    }
}
