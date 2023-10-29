package org.ent.net.node.cmd.split;

import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Splits {

    private static final Map<Integer, Split> splitMap = new HashMap<>();

    static {
        initializeSplits();
    }

    private static final Map<String, Split> splitsByName = buildSplitsByName();

    private Splits() {
    }

    private static Map<String, Split> buildSplitsByName() {
        Map<String, Split> result = new HashMap<>();
        for (Split split : splitMap.values()) {
            String name = split.getShortName();
            if (result.containsKey(name)) {
                throw new AssertionError("Duplicate split name: " + name);
            }
            result.put(name, split);
        }
        return result;
    }

    private static void initializeSplits() {
        List<BiCondition> biConditions = List.of(
                Conditions.IDENTICAL_CONDITION,
                Conditions.SAME_VALUE_CONDITION,
                Conditions.GREATER_THAN_CONDITION
        );
        for (Accessor accessor1 : Accessors.ALL_ACCESSORS) {
            for (Accessor accessor2 : Accessors.ALL_ACCESSORS) {
                for (BiCondition condition : biConditions) {
                    for (boolean not : List.of(false, true)) {
                        Split split = new BiSplit(accessor1, accessor2, condition, not);
                        initializeSplit(split);
                    }
                }
            }
        }
    }

    private static void initializeSplit(Split split) {
        int value = split.getValue();
        if (splitMap.containsKey(value)) {
            throw new AssertionError("duplicate value: " + Integer.toBinaryString(value));
        }
        splitMap.put(value, split);
    }

    public static Split getByValue(int value) {
        return splitMap.get(value);
    }

    public static Split getByName(String name) {
        return splitsByName.get(name);
    }

    public static Collection<String> getAllSplitNames() {
        return splitsByName.keySet();
    }

    public static Split get(BiCondition condition) {
        return get(condition, false);
    }

    public static Split get(BiCondition condition, boolean not) {
        BiSplit split = new BiSplit(Accessors.L, Accessors.R, condition, not);
        return Splits.getByValue(split.getValue());
    }

    public static Split get(BiCondition condition, boolean not, Accessor accessor1, Accessor accessor2) {
        BiSplit split = new BiSplit(accessor1, accessor2, condition, not);
        return Splits.getByValue(split.getValue());
    }

    public static Split get(BiCondition condition, Accessor accessor1, Accessor accessor2) {
        BiSplit split = new BiSplit(accessor1, accessor2, condition, false);
        return Splits.getByValue(split.getValue());
    }

}
