package org.pratikpharma.io.ehealth2017.corpus.reader;


import org.pratikpharma.io.ehealth2017.corpus.*;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentGender;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentLocationOfDeath;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.LineIntervalType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
public class AlignedLineConsumer implements Consumer<String> {
    private static final int ICD_COLUMN_INDEX = 11;
    private final Map<String, Document> documentIndex;
    private final Map<Document, Map<Integer,DocumentLine>> documentLineIndex;
    private final List<Document> corpus;

    AlignedLineConsumer(final Map<String, Document> documentIndex, final Map<Document, Map<Integer,DocumentLine>> documentLineIndex, final List<Document> corpus) {
        this.documentIndex = documentIndex;
        this.documentLineIndex = documentLineIndex;
        this.corpus = corpus;
    }

    @SuppressWarnings({"MethodWithMoreThanThreeNegations", "FeatureEnvy"})
    @Override
    public void accept(final String line) {

        final String[] fields = line.split(";");
        if(!fields[0].equals("DocID")) {
            final String documentId = fields[0];
            final int yearCoded = Integer.valueOf(fields[1]);
            final DocumentGender gender = DocumentGender.getEnumValueFromCode(Integer.valueOf(fields[2]));
            final int age = Integer.valueOf(fields[3]);
            final DocumentLocationOfDeath documentLocationOfDeath = DocumentLocationOfDeath.getEnumValueFromCode(Integer.valueOf(fields[4]));
            final int lineId = Integer.valueOf(fields[5]);
            final String rawText = fields[6];

            if (!documentIndex.containsKey(documentId)) {
                final Document document = new DocumentImpl(documentId, yearCoded, age, gender, documentLocationOfDeath);
                documentIndex.put(documentId, document);
                corpus.add(document);

            }
            final Document document = documentIndex.get(documentId);

            if (!documentLineIndex.containsKey(document)) {
                documentLineIndex.put(document, new HashMap<>());
            }

            final Map<Integer, DocumentLine> currentLineMap = documentLineIndex.get(document);

            if (!currentLineMap.containsKey(lineId)) {
                final DocumentLine documentLine = new DocumentLineImpl(lineId, document, rawText);
                currentLineMap.put(lineId, documentLine);
                document.addLine(documentLine);
            }

            final DocumentLine documentLine = currentLineMap.get(lineId);

            if(fields.length>8) {
                final String intTypeString = fields[7];
                final String intValueString = fields[8];

                if (!intTypeString.equals("NULL") && !intValueString.equals("NULL") && !intTypeString.isEmpty() && !intValueString.isEmpty()) {
                    documentLine.setIntervalType(
                            LineIntervalType.getEnumValueFromCode(Integer.valueOf(intTypeString)));
                    documentLine.setIntervalValue(Integer.valueOf(intValueString));
                }
            }

            if (fields.length > ICD_COLUMN_INDEX) {
                final ICD10Annotation icd10Annotation = new ICD10AnnotationImpl(fields[10], fields[ICD_COLUMN_INDEX]);
                if (!fields[9].isEmpty()) {
                    final String[] causeRanks = fields[9].split("-");
                    icd10Annotation.setCauseRankFirst(Integer.valueOf(causeRanks[0]));
                    icd10Annotation.setCauseRankSecond(Integer.valueOf(causeRanks[1]));
                }
                documentLine.addAnnotation(icd10Annotation);
            }
        }
    }
}
