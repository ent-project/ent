package org.ent.net.node.cmd.veto;

import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vetos {

    private static final Map<Integer, Veto> vetoMap = new HashMap<>();

    static {
        initializeVetos();
    }

    private static final Map<String, Veto> vetosByName = buildVetosByName();

    private Vetos() {
    }

    private static Map<String, Veto> buildVetosByName() {
        Map<String, Veto> result = new HashMap<>();
        for (Veto veto : vetoMap.values()) {
            String name = veto.getShortName();
            if (result.containsKey(name)) {
                throw new AssertionError("Duplicate veto name: " + name);
            }
            result.put(name, veto);
        }
        return result;
    }

    private static void initializeVetos() {
        List<BiCondition> biConditions = List.of(
                Conditions.IDENTICAL_CONDITION,
                Conditions.SAME_VALUE_CONDITION,
                Conditions.GREATER_THAN_CONDITION
        );
        for (Accessor accessor1 : Accessors.ALL_ACCESSORS) {
            for (Accessor accessor2 : Accessors.ALL_ACCESSORS) {
                for (BiCondition condition : biConditions) {
                    for (boolean not : List.of(false, true)) {
                        Veto veto = new BiVeto(accessor1, accessor2, condition, not);
                        initializeVeto(veto);
                    }
                }
            }
        }
    }

    private static void initializeVeto(Veto veto) {
        int value = veto.getValue();
        if (vetoMap.containsKey(value)) {
            throw new AssertionError("duplicate value: " + Integer.toBinaryString(value));
        }
        vetoMap.put(value, veto);
    }

    public static Veto getByValue(int value) {
        return vetoMap.get(value);
    }

    public static Veto getByName(String name) {
        return vetosByName.get(name);
    }

    public static Collection<String> getAllVetoNames() {
        return vetosByName.keySet();
    }

    public static Veto get(BiCondition condition) {
        return get(condition, false);
    }

    public static Veto get(BiCondition condition, boolean not) {
        BiVeto veto = new BiVeto(Accessors.LL, Accessors.LR, condition, not);
        return Vetos.getByValue(veto.getValue());
    }

    public static Veto get(BiCondition condition, boolean not, Accessor accessor1, Accessor accessor2) {
        BiVeto veto = new BiVeto(accessor1, accessor2, condition, not);
        return Vetos.getByValue(veto.getValue());
    }

    public static Veto get(BiCondition condition, Accessor accessor1, Accessor accessor2) {
        BiVeto veto = new BiVeto(accessor1, accessor2, condition, false);
        return Vetos.getByValue(veto.getValue());
    }

}
