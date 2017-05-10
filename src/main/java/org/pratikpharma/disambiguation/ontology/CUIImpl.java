package org.pratikpharma.disambiguation.ontology;

public class CUIImpl implements CUI {

    private final String code;
    private final String description;

    CUIImpl(final String code, final String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
