package org.ent.net.node.cmd;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.ent.net.node.cmd.veto.Vetos;

public class Values {

    private Values() {
    }

    private static final BiMap<String, Integer> valuesByName = buildValuesByName();

    private static BiMap<String, Integer> buildValuesByName() {
        BiMap<String, Integer> result = HashBiMap.create();
        CommandFactory.getAllCommandNames().forEach(name -> {
            if (result.containsKey(name)) {
                throw new AssertionError("duplicate name: " + name);
            }
            result.put(name, CommandFactory.getByName(name).getValue());
        });
        Vetos.getAllVetoNames().forEach(name -> {
            if (result.containsKey(name)) {
                throw new AssertionError("duplicate name: " + name);
            }
            result.put(name, Vetos.getByName(name).getValue());
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
