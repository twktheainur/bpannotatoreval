package fr.lirmm.advanse.textprocessor;


import org.getalp.lexsema.similarity.Text;
import org.sifrproject.annotations.api.model.Annotation;

import java.util.Collection;


public class BioPortalAnnotationTextMapper {

    private BioPortalAnnotationTextMapper() {
    }

    public static void transferAnnotations(Collection<Annotation> annotations, Text text) {
        /*for (Annotation bioPortalAnnotation : annotations) {
            List<Word> matchingWords = text.words().stream()
                    .filter(word -> word.getBegin() == bioPortalAnnotation.getBegin() &&
                            word.getEnd() == bioPortalAnnotation.getEnd())
                    .collect(Collectors.toList());
            matchingWords.forEach(word -> word.addAnnotation(
                    Annotations.createAnnotation(
                            bioPortalAnnotation.getClassId(),
                            bioPortalAnnotation.getSemanticGroup(),
                            bioPortalAnnotation.getOntology()
                    )
            ));
        }*/
    }
}
