package com.mami.processors;

import java.io.File;
import java.io.IOException;

public interface FileProcessor {
  boolean processFile(File inputFile, File outputFile, boolean isFileCreated) throws IOException;
}
