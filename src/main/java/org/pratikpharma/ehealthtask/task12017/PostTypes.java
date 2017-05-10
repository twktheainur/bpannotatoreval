package org.pratikpharma.ehealthtask.task12017;

public enum PostTypes {
    NONE("None"),FIRST("First"), CUTOFF("Cutoff"), DISAMBIGUATE("Disambiguate");

    private final String name;

    PostTypes(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
