package org.pratikpharma.io.ehealth2017.corpus.reader;

import org.pratikpharma.io.ehealth2017.corpus.Document;
import org.pratikpharma.io.ehealth2017.corpus.DocumentLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EHealth2017Task1AlignedReader implements EHealth2017Task1Reader {

    private static final Logger logger = LoggerFactory.getLogger(EHealth2017Task1AlignedReader.class);

    private final String pathString;
    private final List<Document> corpus;


    public EHealth2017Task1AlignedReader(final String path) {
        pathString = path;
        corpus = new ArrayList<>();
    }

    @Override
    @SuppressWarnings("PublicMethodNotExposedInInterface")
    public EHealth2017Task1Reader load() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathString))) {
            final Map<String, Document> documentIndex = new HashMap<>();
            final Map<Document, Map<Integer,DocumentLine>> documentLineIndex = new HashMap<>();

            final Stream<String> lines = bufferedReader.lines();
            lines.forEach(new AlignedLineConsumer(documentIndex, documentLineIndex, corpus));

        } catch (final FileNotFoundException e) {
            logger.error(EHealth2017Task1RawReader.CANNOT_FIND_CORPUS_FILE, e.getLocalizedMessage());
        } catch (final IOException e) {
            logger.error(EHealth2017Task1RawReader.CANNOT_READ_CORPUS_FILE, e.getLocalizedMessage());
        }
        return this;
    }


    @Override
    public void forEach(final Consumer<? super Document> action) {
        corpus.forEach(action);
    }

    @Override
    public Spliterator<Document> spliterator() {
        return corpus.spliterator();
    }

    @Override
    public Iterator<Document> iterator() {
        return corpus.iterator();
    }
}

