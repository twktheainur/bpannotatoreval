package org.pratikpharma.io.ehealth2017.corpus;


@SuppressWarnings("SerializableHasSerializationMethods")
public class InvalidCodeException extends RuntimeException {
    private static final long serialVersionUID = 3667695360651507654L;

    public InvalidCodeException(final int code) {
        super(String.format("The code (%d) does not correspond to " +
                "a valid value of the CodedEnumeration", code));
    }


}
