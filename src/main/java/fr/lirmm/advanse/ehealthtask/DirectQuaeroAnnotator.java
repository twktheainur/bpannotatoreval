package fr.lirmm.advanse.ehealthtask;


import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.similarity.Text;
import org.getalp.lexsema.util.Language;
import org.sifrproject.annotations.api.model.Annotation;
import org.sifrproject.annotations.api.umls.PropertyRetriever;
import org.sifrproject.annotations.model.BioPortalAnnotationFactory;
import org.sifrproject.annotations.model.BioPortalAnnotationParser;
import org.sifrproject.annotations.umls.groups.UMLSGroupIndex;
import org.sifrproject.annotatorclient.BioportalAnnotatorQueryBuilder;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.sifrproject.annotatorclient.api.BioPortalAnnotatorQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DirectQuaeroAnnotator implements TaskAnnotator {
    private static final Logger logger = LoggerFactory.getLogger(QuaeroTokenMappingAnnotator.class);
    private final BioPortalAnnotationParser bioPortalAnnotations;
    private final BioPortalAnnotator annotator;

    public DirectQuaeroAnnotator(UMLSGroupIndex groupIndex, BioPortalAnnotator annotator, BioPortalAnnotationFactory factory, PropertyRetriever cuiRetrieval, PropertyRetriever typeRetrieval) {
        this.bioPortalAnnotations = new BioPortalAnnotationParser(factory, cuiRetrieval, typeRetrieval, groupIndex);
        this.annotator = annotator;
    }

    @Override
    public void annotateText(Text text, Path resultsDirectory) throws IOException {
        String textString = text.asString();
        annotateText(textString, text.getId(), text.getLanguage(), resultsDirectory);
    }

    @Override
    public void annotateText(String text, String textId, Language language, Path resultsDirectory) throws IOException {
        if (!Files.exists(resultsDirectory)) {
            Files.createDirectory(resultsDirectory);
        }
        Path outputFile = Paths.get(resultsDirectory.toString(), textId + ".ann");


        BioPortalAnnotatorQuery query = BioportalAnnotatorQueryBuilder.DEFAULT
                .text(text).expand_mappings(true).build();

        String output = annotator.runQuery(query);
        //logger.info(output);
        bioPortalAnnotations.parseAnnotations(output);
        //logger.info(bioPortalAnnotations.annotations().toString());

        writeBRAT(bioPortalAnnotations.annotations(), outputFile);

    }


    private void writeBRAT(List<Annotation> annotations, Path file) {
        //annotations.sort(Comparator.comparingInt(BioPortalAnnotation::getBegin));
        /*int termCounter = 1;
        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(file))) {
            for (BioPortalAnnotation annotation : annotations) {
                printWriter.println(String.format("T%d\t%s %d %d\t%s", termCounter, annotation.getSemanticGroup(), annotation.getBegin(), annotation.getEnd(), annotation.getText().toLowerCase()));
                printWriter.println(String.format("#%d\tAnnotatorNotes T%d\t%s", termCounter, termCounter, annotation.getCuis()));
                termCounter++;
            }
        } catch (IOException e) {
            logger.error("Cannot open file for writing: {}", e.getLocalizedMessage());
        }*/
    }

    @Override
    public void annotateCorpus(CorpusLoader corpusLoader, Path resultsDirectory) {
        for (Text text : corpusLoader) {
            try {
                annotateText(text, resultsDirectory);
            } catch (IOException e) {
                logger.error("Cannot create results directory");
            }
        }
    }
}
