package org.pratikpharma.disambiguation.similarity;


@FunctionalInterface
public interface SimilarityMeasure {
    double compute(String first, String second);
}
