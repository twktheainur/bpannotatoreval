package org.pratikpharma.io.ehealth2017.corpus;

import org.pratikpharma.io.ehealth2017.corpus.enumerations.LineIntervalType;
import redis.clients.jedis.Jedis;


public interface DocumentLine extends Iterable<ICD10Annotation> {
    int getLineId();

    String getRawText();

    LineIntervalType getIntervalType();

    void setIntervalType(LineIntervalType intervalType);

    int getIntervalValue();

    void setIntervalValue(int intervalValue);

    void addAnnotation(ICD10Annotation icd10Annotation);

    Document getDocument();

    boolean hasAnnotation();

    void cacheAnnotations(final Jedis jedis, final String cacheKeyPrefix);
    @SuppressWarnings("all")
    boolean fetchFromCache(final Jedis jedis, final String cacheKeyPrefix);

    boolean isMarkedEmpty(final Jedis jedis, final String cacheKeyPrefix);


}
