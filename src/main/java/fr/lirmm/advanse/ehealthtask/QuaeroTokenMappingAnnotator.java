package fr.lirmm.advanse.ehealthtask;


import annotations.AnnotationWriter;
import annotations.BRATAnnotationWriter;
import fr.lirmm.advanse.textprocessor.api.TextProcessor;
import fr.lirmm.advanse.textprocessor.factories.DefaultTextProcessorFactory;
import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.similarity.Text;
import org.getalp.lexsema.util.Language;
import org.sifrproject.annotations.api.umls.PropertyRetriever;
import org.sifrproject.annotations.model.BioPortalAnnotationFactory;
import org.sifrproject.annotations.model.BioPortalAnnotationParser;
import org.sifrproject.annotations.umls.groups.UMLSGroup;
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
import java.util.Map;

public class QuaeroTokenMappingAnnotator implements TaskAnnotator {
    private static final Logger logger = LoggerFactory.getLogger(QuaeroTokenMappingAnnotator.class);
    private final BioPortalAnnotationParser bioPortalAnnotations;
    private final BioPortalAnnotator annotator;

    private Map<String, UMLSGroup> groups;

    public QuaeroTokenMappingAnnotator(Map<String, UMLSGroup> groups, BioPortalAnnotator annotator, BioPortalAnnotationFactory factory, PropertyRetriever cuiRetrieval, PropertyRetriever umlsTypeRetrieval, UMLSGroupIndex groupIndex) {
        this.groups = groups;
        this.bioPortalAnnotations = new BioPortalAnnotationParser(factory,cuiRetrieval, umlsTypeRetrieval, groupIndex);
        this.annotator = annotator;
    }

    @Override
    public void annotateText(Text text, Path resultsDirectory) throws IOException {
        if (!Files.exists(resultsDirectory)) {
            Files.createDirectory(resultsDirectory);
        }
        Path outputFile = Paths.get(resultsDirectory.toString(), text.getId() + ".ann");
        String textString = text.asString();

        processAnnotation(textString);

        //BioPortalAnnotationTextMapper.transferAnnotations(bioPortalAnnotations.annotations(), text);
        AnnotationWriter writer = new BRATAnnotationWriter();
        writer.writeAnnotations(outputFile, text);

    }

    @Override
    public void annotateText(String text, String textId, Language language, Path resultsDirectory) throws IOException {
        TextProcessor textProcessor = new DefaultTextProcessorFactory().createProcessor(language);
        Text processedText = textProcessor.process(text,textId);
        annotateText(processedText,resultsDirectory);
    }

    private void processAnnotation(String text) throws IOException {

        for (String groupType : groups.keySet()) {
            logger.info("\n ************{}************** \n", groupType);
            //Crafting query
            BioPortalAnnotatorQuery query = BioportalAnnotatorQueryBuilder.DEFAULT
                    .text(text).semantic_types(groups.get(groupType).types()).expand_mappings(false).build();

            String response = annotator.runQuery(query);
            bioPortalAnnotations.parseAnnotations(response);
            logger.info(bioPortalAnnotations.annotations().toString());
        }

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
