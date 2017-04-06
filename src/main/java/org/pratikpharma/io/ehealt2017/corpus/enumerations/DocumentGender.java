package org.pratikpharma.io.ehealt2017.corpus.enumerations;


public enum DocumentGender implements CodedEnumeration {
    MALE(1, "Male subject"), FEMALE(2, "Female subject");

    private final int code;
    private final String description;

    DocumentGender(final int code, final String description) {
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


    public static DocumentGender getEnumValueFromCode(final Integer code) {
        DocumentGender returnedGender = null;
        for(final DocumentGender documentGender: values()){
            final Integer documentGenderCode = documentGender.getCode();
            if(documentGenderCode.equals(code)){
                returnedGender = documentGender;
            }
        }
        return returnedGender;
    }

    public static String valuesString() {
        final StringBuilder stringBuilder = new StringBuilder();
        for(final DocumentGender documentLocationOfDeath: values()){
            stringBuilder.append(documentLocationOfDeath);
            stringBuilder.append(" ");
        }

        final String valueString = stringBuilder.toString();
        return valueString.trim();
    }

    @Override
    public String toString() {
        return name() +
                "c=" + code;
    }


}
