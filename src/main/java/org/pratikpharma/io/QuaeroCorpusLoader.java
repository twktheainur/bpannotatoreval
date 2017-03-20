package org.pratikpharma.io;

import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.io.document.loader.CorpusLoaderImpl;
import org.pratikpharma.textprocessor.api.TextProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class QuaeroCorpusLoader extends CorpusLoaderImpl{

    private static final Logger logger = LoggerFactory.getLogger(QuaeroCorpusLoader.class);
    static final String CANNOT_LOAD_FILE_CONTENTS = "Cannot load file contents: {}";
    static final String ERROR_WHILE_LISTING_CORPUS_FILES = "Error while listing corpus files: {}";
    static final String PATH_MUST_POINT_TO_AN_EXISTING_DIRECTORY = "Path must point to an existing directory.";

    private final String pathString;
    private final TextProcessor textProcessor;

    public QuaeroCorpusLoader(final String path, final TextProcessor textProcessor) {
        this.pathString = path;
        this.textProcessor = textProcessor;
    }

    @Override
    public void load() {
        final Path corpusDirectory = Paths.get(pathString);
        if(Files.exists(corpusDirectory) && Files.isDirectory(corpusDirectory)){
            try {
                Files.list(corpusDirectory).filter(path -> path.getFileName().toString().endsWith("txt")).forEach(f->{
                    logger.info(f.toString());
                    try {
                        final Optional result = Files.readAllLines(f).stream().reduce(String::concat);
                        if(result.isPresent()){
                            addText(textProcessor.process(result.get().toString(),f.getFileName().toString().split("\\.")[0]));
                        }
                    } catch (final IOException e) {
                        logger.error(CANNOT_LOAD_FILE_CONTENTS,e.getLocalizedMessage());
                    }
                });
            } catch (final IOException e) {
                logger.error(ERROR_WHILE_LISTING_CORPUS_FILES,e.getLocalizedMessage());
            }
        } else {
            logger.error(PATH_MUST_POINT_TO_AN_EXISTING_DIRECTORY);
        }

    }

    @Override
    public CorpusLoader loadNonInstances(final boolean b) {
        return this;
    }
}
