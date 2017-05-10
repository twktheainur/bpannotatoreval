package org.pratikpharma.io.ehealth2017.corpus;

import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentGender;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentLocationOfDeath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class DocumentImpl implements Document {
    private final String id;
    private final Integer yearCoded;
    private final Integer age;

    private final DocumentGender gender;
    private final DocumentLocationOfDeath locationOfDeath;

    private final Collection<DocumentLine> documentLines;

    private String documentText = "";

    public DocumentImpl(final String id, final Integer yearCoded, final Integer age, final DocumentGender gender, final DocumentLocationOfDeath locationOfDeath) {
        this.id = id;
        this.yearCoded = yearCoded;
        this.age = age;
        this.gender = gender;
        this.locationOfDeath = locationOfDeath;

        documentLines = new ArrayList<>();
    }

    @Override
    public void addLine(final DocumentLine documentLine){
        documentLines.add(documentLine);
        documentText+=documentLine.getRawText()+ System.lineSeparator();
    }


    @Override
    public String getId() {
        return id;
    }


    @Override
    public Integer getYearCoded() {
        return yearCoded;
    }


    @Override
    public Integer getAge() {
        return age;
    }


    @Override
    public DocumentGender getGender() {
        return gender;
    }


    @Override
    public DocumentLocationOfDeath getLocationOfDeath() {
        return locationOfDeath;
    }

    @Override
    public Iterator<DocumentLine> iterator() {
        return documentLines.iterator();
    }

    @Override
    public void forEach(final Consumer<? super DocumentLine> action) {
        documentLines.forEach(action);
    }

    @Override
    public Spliterator<DocumentLine> spliterator() {
        return documentLines.spliterator();
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        final Document theDocument = (Document) o;

        final String id = getId();
        return id.equals(theDocument.getId());
    }

    @Override
    public int hashCode() {
        final String id = getId();
        return id.hashCode();
    }

    @Override
    public int size(){
        return documentLines.size();
    }

    @Override
    public String getDocumentText() {
        return documentText;
    }
}
