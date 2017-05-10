package org.pratikpharma.io.ehealth2017.corpus.reader;

import org.pratikpharma.io.ehealth2017.corpus.Document;

public interface EHealth2017Task1Reader extends Iterable<Document> {
    @SuppressWarnings({"MethodReturnOfConcreteClass", "PublicMethodNotExposedInInterface"})
    EHealth2017Task1Reader load();
}
