package org.ent.dev.game.forwardarithmetic.readinfo;

class Holder<T> {
    T item;

    T put(T item) {
        this.item = item;
        return item;
    }

    public T get() {
        return item;
    }
}
