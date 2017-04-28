package org.pratikpharma.io.ehealth2017.corpus;

import org.pratikpharma.io.ehealth2017.corpus.enumerations.LineIntervalType;

import java.util.Set;


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

    Set<ICD10Annotation> getAnnotations();
}
