package org.pratikpharma.io.quaero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class QuaeroReader implements Iterable<Map.Entry<String,String>> {

    private static final Logger logger = LoggerFactory.getLogger(QuaeroReader.class);

    private final String pathString;
    private final Map<String,String> texts;


    public QuaeroReader(final String path) {
        pathString = path;
        texts = new HashMap<>();
    }

    @SuppressWarnings({"MethodReturnOfConcreteClass", "PublicMethodNotExposedInInterface"})
    public QuaeroReader load() {
        final Path corpusDirectory = Paths.get(pathString);
        if(Files.exists(corpusDirectory) && Files.isDirectory(corpusDirectory)){
            try {
                final Stream<Path> list = Files.list(corpusDirectory);
                final Stream<Path> txt = list.filter(path -> {
                    final Path fileName = path.getFileName();
                    final String s = fileName.toString();
                    return s.endsWith("txt");
                });
                txt.forEach(new PathConsumer(texts));
            } catch (final IOException e) {
                logger.error("Cannot list corpus files in provided directory: {}",e.getLocalizedMessage());
            }
        } else {
            logger.error("The specified directory does not exist!");
        }
        return this;

    }

    private static class PathConsumer implements Consumer<Path> {
        private final Map<String,String> texts;

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        PathConsumer(final Map<String, String> texts) {
            this.texts = texts;
        }

        @Override
        public void accept(final Path file) {
            logger.info(file.toString());
            try {
                final StringBuilder stringBuilder = new StringBuilder();
                for (final String line : Files.readAllLines(file)) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                }
                final String result = stringBuilder.toString();
                final Path filePath = file.getFileName();
                final String fileName = filePath.toString();
                texts.put(fileName.split("\\.")[0], result);
            } catch (final IOException e) {
                logger.error("Cannot read file {}", e.getLocalizedMessage());
            }
        }
    }



    @Override
    public Iterator<Map.Entry<String,String>> iterator() {
        final Set<Map.Entry<String, String>> entries = texts.entrySet();
        return entries.iterator();
    }
}
