package org.ent.dev.stat;

import java.awt.Color;

public class PlotRow {
    private BinnedStat stat;
    private Color color;
    private String label;

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
