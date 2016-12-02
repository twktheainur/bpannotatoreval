package fr.lirmm.advanse.io;

import fr.lirmm.advanse.textprocessor.api.TextProcessor;
import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.io.document.loader.CorpusLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class QuaeroCorpusLoader extends CorpusLoaderImpl{

    private static final Logger logger = LoggerFactory.getLogger(QuaeroCorpusLoader.class);

    private final String pathString;
    private final TextProcessor textProcessor;

    public QuaeroCorpusLoader(String path, TextProcessor textProcessor) {
        this.pathString = path;
        this.textProcessor = textProcessor;
    }

    @Override
    public void load() {
        Path corpusDirectory = Paths.get(pathString);
        if(Files.exists(corpusDirectory) && Files.isDirectory(corpusDirectory)){
            try {
                Files.list(corpusDirectory).filter(path -> path.getFileName().toString().endsWith("txt")).forEach(f->{
                    logger.info(f.toString());
                    try {
                        Optional result = Files.readAllLines(f).stream().reduce(String::concat);
                        if(result.isPresent()){
                            addText(textProcessor.process(result.get().toString(),f.getFileName().toString().split("\\.")[0]));
                        }
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

    }

    @Override
    public CorpusLoader loadNonInstances(boolean b) {
        return this;
    }
}
