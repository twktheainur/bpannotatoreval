package org.pratikpharma.io.ehealt2017.corpus.enumerations;

public enum DocumentLocationOfDeath implements CodedEnumeration {
    HOME(1, "Home"), HOSPITAL(2, "Hospital"), PRIVATE_CLINIC(3, "Private Clinic"),
    HOSPICE(4, "Hospice, Retirement home"), PUBLIC_PLACE(5, "Public place"), OTHER_LOCATION(6,"Other location");
    private final int code;
    private final String description;


    DocumentLocationOfDeath(final int code, final String description) {
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

    public static DocumentLocationOfDeath getEnumValueFromCode(final Integer code) {
        DocumentLocationOfDeath returnedLocation = null;
        for(final DocumentLocationOfDeath documentLocationOfDeath: values()){
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
