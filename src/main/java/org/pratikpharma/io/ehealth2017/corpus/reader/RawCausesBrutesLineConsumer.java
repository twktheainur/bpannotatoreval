package org.pratikpharma.io.ehealth2017.corpus.reader;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.pratikpharma.io.ehealth2017.corpus.Document;
import org.pratikpharma.io.ehealth2017.corpus.DocumentImpl;
import org.pratikpharma.io.ehealth2017.corpus.DocumentLine;
import org.pratikpharma.io.ehealth2017.corpus.DocumentLineImpl;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentGender;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentLocationOfDeath;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.LineIntervalType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
public class RawCausesBrutesLineConsumer implements Consumer<String> {
    private final Map<String, Document> documentIndex;
    private final Map<Document, Map<Integer,DocumentLine>> documentLineIndex;
    private final List<Document> corpus;

    RawCausesBrutesLineConsumer(final Map<String, Document> documentIndex, final Map<Document, Map<Integer,DocumentLine>> documentLineIndex, final List<Document> corpus) {
        this.documentIndex = documentIndex;
        this.documentLineIndex = documentLineIndex;
        this.corpus = corpus;
    }

    @SuppressWarnings({"MethodWithMoreThanThreeNegations", "FeatureEnvy"})
    @Override
    public void accept(final String line) {
        final String[] fields = line.split(";");
        if(!line.contains("DocID")) {
            try (final CSVParser parser = CSVParser.parse(line, CSVFormat.RFC4180.withDelimiter(';').withQuoteMode(QuoteMode.ALL))){
                final CSVRecord csvRecord = parser.getRecords().get(0);
                final String documentId = csvRecord.get(0);
                final int yearCoded = Integer.valueOf(csvRecord.get(1));
                final int lineId = csvRecord.get(2).isEmpty() ? 0 : Integer.valueOf(csvRecord.get(2));
                final String rawText = fields[3];

                if (!documentIndex.containsKey(documentId)) {
                    final Document document = new DocumentImpl(documentId, yearCoded, 0, DocumentGender.UNKNOWN, DocumentLocationOfDeath.UNKNOWN);
                    documentIndex.put(documentId, document);
                    corpus.add(document);

                }
                final Document document = documentIndex.get(documentId);

                if (!documentLineIndex.containsKey(document)) {
                    documentLineIndex.put(document, new HashMap<>());
                }

                final Map<Integer, DocumentLine> currentLineMap = documentLineIndex.get(document);

                addDocumentLine(document, lineId, rawText, currentLineMap);

                final DocumentLine documentLine = currentLineMap.get(lineId);

                if(fields.length>5) {
                    final String intTypeString = csvRecord.get(4);
                    final String intValueString = csvRecord.get(5);

                    if (!intTypeString.equals("NULL") && !intValueString.equals("NULL") && !intTypeString.isEmpty() && !intValueString.isEmpty()) {
                        documentLine.setIntervalType(
                                LineIntervalType.getEnumValueFromCode(Integer.valueOf(intTypeString)));
                        documentLine.setIntervalValue(Integer.valueOf(intValueString));
                    }
                }
            } catch (IOException ignored) {
            }

        }
    }


    private void addDocumentLine(final Document document, final int lineId, final String rawText, final Map<Integer,DocumentLine> currentLineMap){
        if(!currentLineMap.containsKey(lineId)){
            final DocumentLine documentLine = new DocumentLineImpl(lineId, document, rawText);
            currentLineMap.put(lineId, documentLine);
            document.addLine(documentLine);
        }
    }
}
