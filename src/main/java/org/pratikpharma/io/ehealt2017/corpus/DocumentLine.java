package org.pratikpharma.io.ehealt2017.corpus;

import org.pratikpharma.io.ehealt2017.corpus.enumerations.LineIntervalType;


public interface DocumentLine extends Iterable<ICD10Annotation> {
    int getLineId();

    String getRawText();

    LineIntervalType getIntervalType();

    void setIntervalType(LineIntervalType intervalType);

    int getIntervalValue();

    void setIntervalValue(int intervalValue);

    void addAnnotation(ICD10Annotation icd10Annotation);

    Document getDocument();

    boolean hasAnnotation();
}
