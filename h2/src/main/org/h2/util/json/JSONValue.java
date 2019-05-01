/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.util.json;

/**
 * JSON value.
 */
public abstract class JSONValue {
    
    public static final int OBJECT = 0;
    public static final int ARRAY = 1;
    public static final int STRING = 2;
    public static final int NUMBER = 3;
    public static final int BOOLEAN = 4;
    public static final int NULL = 5;

    JSONValue() {
    }

    /**
     * Appends this value to the specified target.
     *
     * @param target
     *            the target
     */
    public abstract void addTo(JSONTarget target);
    
    public abstract int getType();

    @Override
    public final String toString() {
        JSONStringTarget target = new JSONStringTarget();
        addTo(target);
        return target.getResult();
    }

    @Override
    public boolean equals(Object v) {
        return this.toString().equals(v.toString());
    }

}
