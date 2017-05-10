package org.pratikpharma.io.ehealth2017.corpus;


import org.pratikpharma.disambiguation.ontology.ClassSemanticInformation;
import redis.clients.jedis.Jedis;

public interface ICD10Annotation extends Comparable<ICD10Annotation> {
    String getCauseRank();

    String getStandardText();

    String getIcd10Code();

    Integer getCauseRankFirst();

    Integer getCauseRankSecond();

    void setCauseRankFirst(Integer causeRank);

    void setCauseRankSecond(Integer causeRankSecond);

    void cache(final Jedis jedis, final String annotationKey);

    String getUri();

    void setClassSemanticInformation(ClassSemanticInformation classSemanticInformation);

    ClassSemanticInformation getClassSemanticInformation();

    double getScore();

    void setScore(double score);
}
