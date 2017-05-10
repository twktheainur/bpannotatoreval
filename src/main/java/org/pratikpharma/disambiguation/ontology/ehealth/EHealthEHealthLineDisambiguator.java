package org.pratikpharma.disambiguation.ontology.ehealth;


import com.wcohen.ss.ScaledLevenstein;
import org.pratikpharma.disambiguation.ontology.ClassSemanticInformation;
import org.pratikpharma.disambiguation.ontology.ClassSemanticInformationFactory;
import org.pratikpharma.disambiguation.ontology.DefaultClassSemanticInformationFactory;
import org.pratikpharma.disambiguation.similarity.SimilarityMeasure;
import org.pratikpharma.disambiguation.similarity.TverskiIndex;
import org.pratikpharma.io.ehealth2017.corpus.Document;
import org.pratikpharma.io.ehealth2017.corpus.DocumentLine;
import org.pratikpharma.io.ehealth2017.corpus.ICD10Annotation;
import org.pratikpharma.io.ehealth2017.corpus.ICD10AnnotationImpl;
import org.pratikpharma.util.EmptyResultsCache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("FeatureEnvy")
public class EHealthEHealthLineDisambiguator implements EHealthLineDisambiguator {

    private final JedisPool jedisPool;
    private final ClassSemanticInformationFactory semanticInformationFactory;
    private final double threshold;
    private final SimilarityMeasure similarityMeasure;
    private final String keyCachePrefix;


    public EHealthEHealthLineDisambiguator(final JedisPool jedisPool, final double threshold, final String keyCachePrefix) {
        this.jedisPool = jedisPool;
        semanticInformationFactory = DefaultClassSemanticInformationFactory.DEFAULT;
        this.threshold = threshold;
        similarityMeasure = new TverskiIndex(1, .5, .5, false, false, new ScaledLevenstein());
        this.keyCachePrefix = keyCachePrefix;
    }

    @SuppressWarnings({"LocalVariableOfConcreteClass", "OverlyLongLambda"})
    @Override
    public List<ICD10Annotation> disambiguate(final Document document, final DocumentLine documentLine) {
        final String key = keyCachePrefix + "_disamb3_D" + document.getId() + "_L" + documentLine.getLineId();
        final List<ICD10Annotation> finalResult = new ArrayList<>();
        final Jedis jedis = jedisPool.getResource();
        final List<String> cachedAnnotations = jedis.lrange(key, 0, -1);
        cachedAnnotations.forEach(annotation -> finalResult.add(new ICD10AnnotationImpl(annotation)));

        if (finalResult.isEmpty() && !EmptyResultsCache.isEmpty(key, jedis)) {
            final List<ICD10Annotation> documentAnnotations = getDocumentAnnotations(document, documentLine);
            final List<ICD10Annotation> initialAnnotations = new ArrayList<>(getLineAnnotations(documentLine));

            final AtomicDouble sum = new AtomicDouble(0d);
            final Stream<ICD10Annotation> annotationStream = initialAnnotations.parallelStream();
            annotationStream.forEach(lineAnnotation -> {
                final ClassSemanticInformation lineAnnotInfo = lineAnnotation.getClassSemanticInformation();
                double score = 0d;

//                for (final ICD10Annotation documentAnnotation : documentAnnotations) {
//                    final ClassSemanticInformation docAnnotInfo = documentAnnotation.getClassSemanticInformation();
//                    score += calculateClassSimilarity(docAnnotInfo, lineAnnotInfo);
//                }

                score += similarityMeasure.compute(lineAnnotInfo.getTextualSignature(),document.getDocumentText());

                lineAnnotation.setScore(score);
                sum.addValue(score);
            });

            final double totalScore = sum.getValue();
            initialAnnotations.forEach(icd10Annotation -> icd10Annotation.setScore(icd10Annotation.getScore() / totalScore));
            initialAnnotations.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
            final Iterator<ICD10Annotation> annotationIterator = initialAnnotations.iterator();
            double cumulativeScore = 0d;
            while ((cumulativeScore < threshold) && annotationIterator.hasNext()) {
                final ICD10Annotation icd10Annotation = annotationIterator.next();
                cumulativeScore += icd10Annotation.getScore();
                finalResult.add(icd10Annotation);
            }


            if (finalResult.isEmpty()) {
                EmptyResultsCache.markEmpty(key, jedis);
            } else {
                finalResult.forEach(icd10Annotation -> icd10Annotation.cache(jedis, key));
            }
        }

        jedis.close();

        return finalResult;
    }

    private double calculateClassSimilarity(final ClassSemanticInformation first, final ClassSemanticInformation second) {
        double score = 0d;

//        //score += similarityMeasure.compute(first.getTextualSignature(), second.getTextualSignature());
//
//        double cuiTotalCount = 0d;
//        double cuiSimilarity = 0d;
//        for (final CUI cuiFirst : first.getCUIs()) {
//            for (final CUI cuiSecond : second.getCUIs()) {
//                final String firstDescription = cuiFirst.getDescription();
//                final String secondDescription = cuiSecond.getDescription();
//                if ((firstDescription != null) && (secondDescription != null) && !firstDescription.isEmpty() && !secondDescription.isEmpty()) {
//                    cuiSimilarity += similarityMeasure.compute(firstDescription, secondDescription);
//                    cuiTotalCount++;
//                }
//            }
//        }
//        score += cuiSimilarity / cuiTotalCount;

        double totalCount = 0d;
        double tuiSimilarity = 0d;
        for (final String tuiFirst : first.getTUIs()) {
            for (final String tuiSecond : second.getTUIs()) {
                if (tuiFirst.equals(tuiSecond)) {
                    tuiSimilarity += 1;
                }
                totalCount++;
            }
        }
        score += tuiSimilarity / totalCount;

        return score;
    }

    private List<ICD10Annotation> getDocumentAnnotations(final Iterable<DocumentLine> document, final DocumentLine documentLine) {
        final List<ICD10Annotation> documentAnnotations = new ArrayList<>();

        document.forEach(line -> {
            if (documentLine != line)
                line.forEach(icd10Annotation -> {
                    icd10Annotation.setClassSemanticInformation(
                            semanticInformationFactory.getSemanticsForClass(icd10Annotation.getUri(), jedisPool, keyCachePrefix)
                    );
                    documentAnnotations.add(icd10Annotation);
                });
        });
        return documentAnnotations;
    }

    private Collection<ICD10Annotation> getLineAnnotations(final Iterable<ICD10Annotation> documentLine) {
        final Collection<ICD10Annotation> localAnnotations = new ArrayList<>();

        documentLine.forEach(icd10Annotation -> {
            icd10Annotation.setClassSemanticInformation(
                    semanticInformationFactory.getSemanticsForClass(icd10Annotation.getUri(), jedisPool, keyCachePrefix)
            );
            localAnnotations.add(icd10Annotation);
        });

        return localAnnotations;
    }

}
