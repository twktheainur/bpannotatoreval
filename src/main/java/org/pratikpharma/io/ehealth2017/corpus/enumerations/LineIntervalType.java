package org.pratikpharma.io.ehealth2017.corpus.enumerations;

public enum LineIntervalType implements CodedEnumeration {
    MINUTES(1, "Minutes"), HOURS(2, "Hours"), DAYS(3, "Days"),
    MONTHS(4, "Months"), YEARS(5, "YEARS");
    private final int code;
    private final String description;


    LineIntervalType(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public static LineIntervalType getEnumValueFromCode(final Integer code) {
        LineIntervalType returnedLocation = null;
        for(final LineIntervalType documentLocationOfDeath: values()){
            final Integer documentLocationOfDeathCode = documentLocationOfDeath.getCode();
            if(documentLocationOfDeathCode.equals(code)){
                returnedLocation = documentLocationOfDeath;
            }
        }
        return returnedLocation;
    }


    @Override
    public String toString() {
        return name() +
                "c=" + code;
    }
}
