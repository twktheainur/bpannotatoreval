package org.pratikpharma.disambiguation.ontology.ehealth;


import org.pratikpharma.io.ehealth2017.corpus.Document;
import org.pratikpharma.io.ehealth2017.corpus.DocumentLine;
import org.pratikpharma.io.ehealth2017.corpus.ICD10Annotation;

import java.util.List;

@FunctionalInterface
public interface EHealthLineDisambiguator {
    List<ICD10Annotation> disambiguate(Document document, DocumentLine documentLine);
}
