package org.pratikpharma.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QuaeroRawReader implements Iterable<Map.Entry<String,String>> {

    private static final Logger logger = LoggerFactory.getLogger(QuaeroRawReader.class);

    private final String pathString;
    private final Map<String,String> texts;


    public QuaeroRawReader(final String path) {
        pathString = path;
        texts = new HashMap<>();
    }

    public QuaeroRawReader load() {
        Path corpusDirectory = Paths.get(pathString);
        if(Files.exists(corpusDirectory) && Files.isDirectory(corpusDirectory)){
            try {
                Files.list(corpusDirectory).filter(path -> path.getFileName().toString().endsWith("txt")).forEach(f->{
                    logger.info(f.toString());
                    try {
                        final StringBuilder stringBuilder = new StringBuilder();
                        for(final String line: Files.readAllLines(f)){
                            stringBuilder.append(line).append(System.lineSeparator());
                        }
                        final String result = stringBuilder.toString();
                        texts.put(f.getFileName().toString().split("\\.")[0], result);
                    } catch (final IOException e) {
                        logger.error("Cannot read file {}",e.getLocalizedMessage());
                    }
                });
            } catch (final IOException e) {
                logger.error("Cannot list corpus files in provided directory: {}",e.getLocalizedMessage());
            }
        } else {
            logger.error("The specified directory does not exist!");
        }
        return this;
    }



    @Override
    public Iterator<Map.Entry<String,String>> iterator() {
        return texts.entrySet().iterator();
    }
}
