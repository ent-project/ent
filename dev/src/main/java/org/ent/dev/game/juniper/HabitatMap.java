package org.ent.dev.game.juniper;

class HabitatMap {
    private final int width;
    private final int height;

    private final Field[][] fields;

    public HabitatMap(String mapDefinition) {
        this.width = mapDefinition.lines().findFirst().orElseThrow().length() - 2;
        this.height = (int) (mapDefinition.lines().count());
        this.fields = new Field[width][height];
        readMap(mapDefinition);
    }

    private void readMap(String mapDefinition) {
        int x = 0;
        int y = 0;
        int idx = 0;
        while (idx < mapDefinition.length()) {
            char ch = mapDefinition.charAt(idx);
            if (ch == '\n') {
                x = 0;
                y++;
            } else if (ch == '#') {
                // skip
            } else {
                fields[x][y] = new Field(ch);
                x++;
            }
            idx++;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Field[][] getFields() {
        return fields;
    }

    public Field getField(int x, int y) {
        return fields[Math.floorMod(x, width)][Math.floorMod(y, height)];
    }

    public boolean isXOutOfBounds(int x) {
        return x < 0 || x >= width;
    }

    public boolean isYOutOfBounds(int y) {
        return y < 0 || y >= height;
    }

    public void dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("#".repeat(width + 2)).append('\n');
        for (int y = 0; y < height; y++) {
            sb.append('#');
            for (int x = 0; x < width; x++) {
                Field f = fields[x][y];
                sb.append(f.type);
            }
            sb.append("#\n");
        }
        sb.append("#".repeat(width + 2)).append('\n');
        System.err.println(sb);
    }
}
