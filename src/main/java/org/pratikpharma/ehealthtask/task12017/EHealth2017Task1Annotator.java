package org.pratikpharma.ehealthtask.task12017;


import org.json.simple.parser.ParseException;
import org.pratikpharma.disambiguation.ontology.ehealth.EHealthEHealthLineDisambiguator;
import org.pratikpharma.disambiguation.ontology.ehealth.EHealthLineDisambiguator;
import org.pratikpharma.io.ehealth2017.corpus.Document;
import org.pratikpharma.io.ehealth2017.corpus.DocumentLine;
import org.pratikpharma.io.ehealth2017.corpus.ICD10Annotation;
import org.pratikpharma.io.ehealth2017.corpus.ICD10AnnotationImpl;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentGender;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.DocumentLocationOfDeath;
import org.pratikpharma.io.ehealth2017.corpus.enumerations.LineIntervalType;
import org.sifrproject.annotations.api.input.AnnotationParser;
import org.sifrproject.annotations.api.model.AnnotatedClass;
import org.sifrproject.annotations.api.model.Annotation;
import org.sifrproject.annotations.api.model.AnnotationToken;
import org.sifrproject.annotations.api.model.AnnotationTokens;
import org.sifrproject.annotations.exceptions.InvalidFormatException;
import org.sifrproject.annotations.exceptions.NCBOAnnotatorErrorException;
import org.sifrproject.annotations.input.BioPortalJSONAnnotationParser;
import org.sifrproject.annotations.model.BioPortalLazyAnnotationFactory;
import org.sifrproject.annotatorclient.BioportalAnnotatorQueryBuilder;
import org.sifrproject.annotatorclient.api.BioPortalAnnotator;
import org.sifrproject.annotatorclient.api.BioPortalAnnotatorQuery;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("HardcodedLineSeparator")
public class EHealth2017Task1Annotator {


    private static final double PROGRESS_THRESHOLD = 0.01;
    private final double freqCutoffThreshold;
    private final BioPortalAnnotator annotator;
    private final List<String> alCode;
    private final List<Integer> alFreq;
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[\t\n\r]");

    private final AnnotationParser annotationParser;
    private EHealthLineDisambiguator disambiguator;

    private final Jedis jedis;

    private final String cacheKeyPrefix;

    private final PostTypes mfc;

    private final boolean rawCorpus;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    public EHealth2017Task1Annotator(final BioPortalAnnotator annotator, final JedisPool jedisPool, final String cacheKeyPrefix, final PostTypes mfc, final double freqCutoffThreshold, final boolean rawFormat) throws IOException {
        this.annotator = annotator;
        annotationParser = new BioPortalJSONAnnotationParser(new BioPortalLazyAnnotationFactory());
        jedis = jedisPool.getResource();
        this.cacheKeyPrefix = cacheKeyPrefix;
        this.mfc = mfc;
        this.freqCutoffThreshold = freqCutoffThreshold;
        // charger les fréquences des codes
        alCode = new ArrayList<>();
        alFreq = new ArrayList<>();
        rawCorpus = rawFormat;

        try (InputStream inputStream = EHealth2017Task1Annotator.class.getResourceAsStream("/codefreq_train.csv")) {
            try (final BufferedReader r = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = r.readLine();
                while (line != null) {
                    alCode.add(line.split(";")[1]);
                    alFreq.add(Integer.parseInt(line.split(";")[0]));
                    line = r.readLine();
                }
            }
        }
        if(mfc == PostTypes.DISAMBIGUATE) {
            disambiguator = new EHealthEHealthLineDisambiguator(jedisPool, freqCutoffThreshold,cacheKeyPrefix);
        }
    }

    @SuppressWarnings({"FeatureEnvy", "PublicMethodNotExposedInInterface"})
    public void annotate(final Iterable<Document> corpus, final PrintWriter resultOutput, final String... ontologies) throws IOException, NCBOAnnotatorErrorException, ParseException, InvalidFormatException {

        //Counting the number of line in order to calculate the progress of the annotation
        int totalLineCount = 0;
        for (final Document document : corpus) {
            totalLineCount += document.size();
        }

        final AtomicInteger progressCount = new AtomicInteger(0);
        double previousProgress = 0d;

        for (final Document document : corpus) {
            for (final DocumentLine documentLine : document) {
                final String rawText = documentLine.getRawText();
                final String text = rawText.trim();
                if (!text.isEmpty()) {

                    //Fetching cached annotations
                    final boolean hasCached = documentLine.fetchFromCache(jedis, cacheKeyPrefix);
                    final boolean markedEmpty = documentLine.isMarkedEmpty(jedis, cacheKeyPrefix);

                    //If there are no annotations in the cache and if the annotation
                    // has not been previously marked empty (the bioportal annotations were cached as empty),
                    // we fetch the annotations from Bioportal and cache them
                    if (!hasCached && !markedEmpty) {
                        fetchBioportalAnnotations(text, ontologies, documentLine);
                    }
                }
            }

            final double progress = (progressCount.addAndGet(document.size()) / (double) totalLineCount) * 100;

            if (Math.abs(progress - previousProgress) > PROGRESS_THRESHOLD) {
                //noinspection UseOfSystemOutOrSystemErr,HardcodedLineSeparator
                System.out.print(String.format("\rAnnotating %.2f%%", progress));
                previousProgress = progress;
            }
        }

        processAndWriteResult(corpus, resultOutput, totalLineCount);

    }

    private void fetchBioportalAnnotations(final CharSequence text, final String[] ontologies, final DocumentLine documentLine) throws IOException, ParseException, InvalidFormatException, NCBOAnnotatorErrorException {
        final Matcher matcher = SPECIAL_CHARS.matcher(text);
        final BioportalAnnotatorQueryBuilder queryBuilder = BioportalAnnotatorQueryBuilder.DEFAULT;
        queryBuilder.text(matcher.replaceAll(" "));
        queryBuilder.lemmatize(false);
        queryBuilder.ontologies(ontologies);
        queryBuilder.longest_only(true);

        final BioPortalAnnotatorQuery query = queryBuilder.build();
        final String output = annotator.runQuery(query);
        final List<Annotation> annotations = annotationParser.parseAnnotations(output);

        annotations.forEach(new AnnotationConsumer(documentLine));
        documentLine.cacheAnnotations(jedis, cacheKeyPrefix);

    }

    private void processAndWriteResult(final Iterable<Document> corpus, final PrintWriter resultOutput, final int totalLineCount) {
        int linecount =totalLineCount;
        final AtomicInteger progressCount = new AtomicInteger(0);
        double previousProgress = 0d;
        for (final Document document : corpus) {
            for (final DocumentLine documentLine : document) {
                if (documentLine.hasAnnotation()) {
                    final List<CodeFreqPair> codeByFreq = new ArrayList<>();
                    final double sum = computeFrequencies(documentLine, codeByFreq);
                    //Normalizing code frequency distribution for current annotations
                    for (final CodeFreqPair codeFreqPair : codeByFreq) {
                        codeFreqPair.setFreq(codeFreqPair.getFreq() / sum);
                    }
                    if (codeByFreq.isEmpty() || (mfc == PostTypes.NONE) || (mfc == PostTypes.DISAMBIGUATE) ) {
                        final List<ICD10Annotation> annotations = new ArrayList<>();
                        if(mfc ==PostTypes.DISAMBIGUATE){
                            annotations.addAll(disambiguator.disambiguate(document,documentLine));

                            final double progress = (progressCount.addAndGet(1) / (double) linecount) * 100;

                            if (Math.abs(progress - previousProgress) > PROGRESS_THRESHOLD) {
                                //noinspection UseOfSystemOutOrSystemErr,HardcodedLineSeparator
                                System.out.print(String.format("\rDisambiguating %.2f%%", progress));
                                previousProgress = progress;
                            }
                        } else {
                            documentLine.forEach(annotations::add);
                        }
                        for (final ICD10Annotation annotation : annotations) {
                            resultOutput.println(formatResultLine(document, documentLine, annotation));
                        }
                    } else if (!codeByFreq.isEmpty()) {
                        handleMfcHeuristic(resultOutput, document, documentLine, codeByFreq);
                    }
                } else {
                    resultOutput.println(formatResultLine(document, documentLine, null));
                    linecount--;
                }
            }
        }
        resultOutput.flush();
    }

    private double computeFrequencies(final Iterable<ICD10Annotation> documentLine, final Collection<CodeFreqPair> frequencies) {
        double sum = 0d;
        for (final ICD10Annotation annotation : documentLine) {
            final int index = alCode.indexOf(annotation.getIcd10Code());
            if (index >= 0) {
                final double freq = alFreq.get(index);
                frequencies.add(new CodeFreqPair(annotation, freq));
                sum += freq;
            }
        }
        return sum;
    }

    @SuppressWarnings("LocalVariableOfConcreteClass")
    private void handleMfcHeuristic(final PrintWriter printWriter, final Document document, final DocumentLine documentLine, final List<CodeFreqPair> codeByFreq) {

        codeByFreq.sort((o1, o2) -> Double.compare(o2.getFreq(), o1.getFreq()));
        final Iterator<CodeFreqPair> iterator = codeByFreq.iterator();
        if (mfc == PostTypes.CUTOFF) {
            double cumulativeFreq = 0d;
            while ((cumulativeFreq < freqCutoffThreshold) && iterator.hasNext()) {
                final CodeFreqPair pair = iterator.next();
                cumulativeFreq += pair.getFreq();
                final ICD10Annotation annotation = pair.getAnnotation();
                printWriter.println(formatResultLine(document, documentLine, annotation));
            }
        } else if (mfc == PostTypes.FIRST) {
            final CodeFreqPair first = iterator.next();
            final ICD10Annotation annotation = first.getAnnotation();
            printWriter.println(formatResultLine(document, documentLine, annotation));
        }
    }

    @SuppressWarnings("all")
    private static class CodeFreqPair {
        private final ICD10Annotation annotation;
        private double freq;

        CodeFreqPair(final ICD10Annotation annotation, final double freq) {
            this.annotation = annotation;
            this.setFreq(freq);
        }

        public ICD10Annotation getAnnotation() {
            return annotation;
        }

        public double getFreq() {
            return freq;
        }

        public void setFreq(double freq) {
            this.freq = freq;
        }
    }


    private String formatResultLine(final Document document, final DocumentLine documentLine,
                                    final ICD10Annotation icd10Annotation) {
        return rawCorpus ? formatRawResultLine(document, documentLine, icd10Annotation) :
                formatAlignedResultLine(document, documentLine, icd10Annotation);

    }

    @SuppressWarnings("FeatureEnvy")
    private String formatRawResultLine(final Document document, final DocumentLine documentLine,
                                       final ICD10Annotation icd10Annotation) {

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s;%d;%d;",
                document.getId(),
                document.getYearCoded(),
                documentLine.getLineId()));

        if (icd10Annotation == null) {
            stringBuilder.append(";;");
        } else {
            stringBuilder.append(String.format("%s;%s;%s", icd10Annotation.getCauseRank(), icd10Annotation.getStandardText(), icd10Annotation.getIcd10Code()));
        }

        return stringBuilder.toString();
    }

    @SuppressWarnings("FeatureEnvy")
    private String formatAlignedResultLine(final Document document, final DocumentLine documentLine,
                                           final ICD10Annotation icd10Annotation) {
        final StringBuilder stringBuilder = new StringBuilder();
        final DocumentGender gender = document.getGender();
        final DocumentLocationOfDeath locationOfDeath = document.getLocationOfDeath();
        stringBuilder.append(String.format("%s;%d;%s;%d;%s;",
                document.getId(),
                document.getYearCoded(),
                gender,
                document.getAge(),
                locationOfDeath));

        String intTypeString = "NULL";
        String intValueString = "NULL";

        final LineIntervalType intervalType = documentLine.getIntervalType();
        if (intervalType != null) {
            intTypeString = String.valueOf(intervalType);
            intValueString = String.valueOf(documentLine.getIntervalValue());
        }

        stringBuilder.append(String.format("%d;%s;%s;%s;",
                documentLine.getLineId(),
                documentLine.getRawText(),
                intTypeString,
                intValueString
        ));

        if (icd10Annotation == null) {
            stringBuilder.append(";;");
        } else {
            stringBuilder.append(String.format("%s;%s;%s", icd10Annotation.getCauseRank(), icd10Annotation.getStandardText(), icd10Annotation.getIcd10Code()));
        }

        return stringBuilder.toString();

    }

    private static class AnnotationConsumer implements Consumer<Annotation> {
        private final DocumentLine documentLine;

        AnnotationConsumer(final DocumentLine documentLine) {
            this.documentLine = documentLine;
        }

        @Override
        public void accept(final Annotation annotation) {
            final AnnotatedClass annotatedClass = annotation.getAnnotatedClass();
            final String id = annotatedClass.getId();

            final AnnotationTokens annotations = annotation.getAnnotations();
            final Iterator<AnnotationToken> iterator = annotations.iterator();
            final AnnotationToken first = iterator.next();
            final String standardText = first.getText();
            documentLine.addAnnotation(new ICD10AnnotationImpl(id, standardText));
        }
    }
}
