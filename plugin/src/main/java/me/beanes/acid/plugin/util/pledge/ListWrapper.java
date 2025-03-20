package me.beanes.acid.plugin.util.pledge;

import java.util.List;

// Source: https://github.com/ThomasOM/Pledge/blob/master-1.0/src/main/java/dev/thomazz/pledge/util/collection/ListWrapper.java
// Licensed under MIT
public abstract class ListWrapper<T> implements List<T> {
    protected final List<T> base;

    public ListWrapper(List<T> base) {
        this.base = base;
    }

    public List<T> getBase() {
        return this.base;
    }
}