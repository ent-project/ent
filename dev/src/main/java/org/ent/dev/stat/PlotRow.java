package org.ent.dev.stat;

import java.awt.Color;

public class PlotRow {

    public static final String GROUP_DEFAULT = "group-default";

    private BinnedStat stat;
    private RowType type = RowType.BAR;
    private String group;
    private Color color;
    private String label;

    public RowType getType() {
        return type;
    }

    public void setType(RowType type) {
        this.type = type;
    }

    public PlotRow withType(RowType type) {
        setType(type);
        return this;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public PlotRow withGroup(String group) {
        setGroup(group);
        return this;
    }

    public BinnedStat getStat() {
        return stat;
    }

    public void setStat(BinnedStat stat) {
        this.stat = stat;
    }

    public PlotRow withStat(BinnedStat stat) {
        setStat(stat);
        return this;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public PlotRow withColor(Color color) {
        setColor(color);
        return this;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PlotRow withLabel(String label) {
        setLabel(label);
        return this;
    }
}
