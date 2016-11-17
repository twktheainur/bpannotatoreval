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


    public QuaeroRawReader(String path) {
        this.pathString = path;
        texts = new HashMap<>();
    }

    public QuaeroRawReader load() {
        Path corpusDirectory = Paths.get(pathString);
        if(Files.exists(corpusDirectory) && Files.isDirectory(corpusDirectory)){
            try {
                Files.list(corpusDirectory).filter(path -> path.getFileName().toString().endsWith("txt")).forEach(f->{
                    logger.info(f.toString());
                    try {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(String line: Files.readAllLines(f)){
                            stringBuilder.append(line).append("\n");
                        }
                        String result = stringBuilder.toString();
                        texts.put(f.getFileName().toString().split("\\.")[0], result);
                    } catch (IOException e) {
                        logger.error("Cannot load file contents: {}",e.getLocalizedMessage());
                    }
                });
            } catch (IOException e) {
                logger.error("Error while listing corpus files: {}",e.getLocalizedMessage());
            }
        } else {
            logger.error("Path must point to an existing directory.");
        }
        return this;
    }



    @Override
    public Iterator<Map.Entry<String,String>> iterator() {
        return texts.entrySet().iterator();
    }
}
