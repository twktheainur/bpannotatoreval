package org.pratikpharma.annotatorapi.annotationmodel;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pratikpharma.annotatorapi.api.BioPortalAnnotation;
import org.pratikpharma.annotatorapi.api.BioPortalAnnotationFactory;
import org.pratikpharma.umls.cui.CUIRetrieval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BioPortalAnnotations {

    private final static Logger logger = LoggerFactory.getLogger(BioPortalAnnotation.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private final List<BioPortalAnnotation> annotations;
    private final BioPortalAnnotationFactory annotationFactory;
    private final CUIRetrieval cuiRetrieval;

    public BioPortalAnnotations(BioPortalAnnotationFactory annotationFactory, CUIRetrieval cuiRetrieval) {
        annotations = new ArrayList<>();
        this.annotationFactory = annotationFactory;
        this.cuiRetrieval = cuiRetrieval;
    }

    public void parseJSONAnnotations(String queryResponse, String type) throws IOException {
        JsonNode rootNode = mapper.readTree(queryResponse);
        if (rootNode != null) {
            for (JsonNode child : rootNode) {
                JsonNode annotatedClass = child.get("annotatedClass");
                String classId = annotatedClass.get("@id").asText();
                String ontology = annotatedClass.get("links").get("ontology").asText();
                JsonNode annotationNode = child.get("annotations");
                double score = 0d;
                if (child.has("score")) {
                    score = child.get("score").asDouble();
                }

                Set<String> mappings = Collections.emptySet();
                if(child.has("mappings")){
                    mappings = handleMappings(child.get("mappings"));
                }

                handleAnnotations(annotationNode, classId, ontology, type, score, mappings);
            }
        } else {
            logger.error("Output empty");
        }
    }

    private String cuiString(Iterable<String> cuis) {
        StringBuilder builder = new StringBuilder();

        for (String cui : cuis) {
            builder.append(cui).append(" ");
        }
        return builder.toString().trim();
    }

    private Set<String> handleMappings(JsonNode mappingsNode){
        Set<String> mappings = new TreeSet<>();
        mappingsNode.forEach(mapping -> mappings.add(mapping.get("annotatedClass").get("@id").asText()));
        return mappings;
    }

    private void handleAnnotations(JsonNode annotationNode, String classId, String ontology, String type, double score, Set<String> mappings) {
        for (JsonNode annotation : annotationNode) {
            int end = annotation.findValue("to").asInt();
            int begin = annotation.findValue("from").asInt() -1;
            String text = annotation.findValue("text").asText();
            Set<String> cuiset = new HashSet<>();
            cuiset.addAll(cuiRetrieval.getCUIsForURI(classId));
            mappings.forEach(mapping -> cuiset.addAll(cuiRetrieval.getCUIsForURI(mapping)));
            String cuis = cuiString(cuiset);

            List<BioPortalAnnotation> filtered = annotations.stream().filter(
                    a -> a.getEnd() == end && a.getBegin() == begin && a.getType().equals(type)
            ).collect(Collectors.toList());

            if (filtered.isEmpty()) {
                annotations.add(annotationFactory.createAnnotation(text, classId, ontology, type, cuis, score, begin, end));
            } else {
                List<BioPortalAnnotation> cuifiltered = filtered.stream().filter(a->!a.getCuis().isEmpty()).collect(Collectors.toList());
                if(cuifiltered.isEmpty()){
                    for(BioPortalAnnotation fAnnot: filtered) {
                        annotations.remove(fAnnot);
                    }
                    annotations.add(annotationFactory.createAnnotation(text, classId, ontology, type, cuis, score, begin, end));
                }
            }
        }
    }

    public List<BioPortalAnnotation> annotations() {
        return annotations;
    }
}
