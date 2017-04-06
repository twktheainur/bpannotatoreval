package org.pratikpharma.io.ehealt2017.corpus;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.pratikpharma.io.ehealt2017.corpus.enumerations.LineIntervalType;

import java.util.*;
import java.util.function.Consumer;

public class DocumentLineImpl implements DocumentLine {
    private final int lineId;
    private final Document document;
    private final String rawText;

    private LineIntervalType intervalType;
    private int intervalValue;

    private final Set<ICD10Annotation> annotations;

    public DocumentLineImpl(final int lineId, final Document document, final String rawText) {
        this.lineId = lineId;
        this.rawText = rawText;
        this.document = document;
        annotations = new TreeSet<>();
    }


    @Override
    public int getLineId() {
        return lineId;
    }

    @Override
    public String getRawText() {
        return rawText;
    }

    @Override
    public LineIntervalType getIntervalType() {
        return intervalType;
    }

    @Override
    public void setIntervalType(final LineIntervalType intervalType) {
        this.intervalType = intervalType;
    }

    @Override
    public int getIntervalValue() {
        return intervalValue;
    }

    @Override
    public void setIntervalValue(final int intervalValue) {
        this.intervalValue = intervalValue;
    }

    @Override
    public Iterator<ICD10Annotation> iterator() {
        return annotations.iterator();
    }

    @Override
    public void forEach(final Consumer<? super ICD10Annotation> action) {
        annotations.forEach(action);
    }

    @Override
    public Spliterator<ICD10Annotation> spliterator() {
        return annotations.spliterator();
    }

    @Override
    public void addAnnotation(final ICD10Annotation icd10Annotation) {
        annotations.add(icd10Annotation);
    }

    @Override
    public Document getDocument() {
        return document;
    }

    @Override
    public boolean hasAnnotation() {
        return !annotations.isEmpty();
    }

    @SuppressWarnings({"LawOfDemeter", "MethodWithMultipleReturnPoints"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if ((o == null) || (getClass() != o.getClass())) return false;

        final DocumentLine documentLine = (DocumentLine) o;

        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(getLineId(), documentLine.getLineId());
        final Document document = getDocument();
        final Document otherDocument = documentLine.getDocument();
        equalsBuilder.append(document.getId(), otherDocument.getId());
        return equalsBuilder.isEquals();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 37);
        hashCodeBuilder.append(getLineId());
        hashCodeBuilder.append(getDocument());
        return hashCodeBuilder.toHashCode();
    }
}
