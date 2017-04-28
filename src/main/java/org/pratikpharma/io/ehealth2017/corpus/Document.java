package org.pratikpharma.io.ehealth2017.corpus;

import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentGender;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentLocationOfDeath;


public interface Document extends Iterable<DocumentLine> {
    void addLine(DocumentLine documentLine);

    Integer getId();

    Integer getYearCoded();

    Integer getAge();

    DocumentGender getGender();

    DocumentLocationOfDeath getLocationOfDeath();

    int size();
}
