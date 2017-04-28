package org.pratikpharma.ehealthtask.task12017;

public enum MFCTypes {
    NONE("None"),FIRST("First"), CUTOFF("Cutoff");

    private final String name;

    MFCTypes(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
