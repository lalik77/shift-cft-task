package com.mami.processors;

import com.mami.statistics.Stats;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class TextProcessorImpl implements FileProcessor {
  private final Stats<String> statistics;

  public TextProcessorImpl(Stats<String> statistics) {
    this.statistics = statistics;
  }

  @Override
  public boolean processFile(File inputFile, File outputFile, boolean isFileCreated) throws IOException {

    if (inputFile == null || outputFile == null) {
      return false;
    }

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }

        if (isOnlyText(line)) {
          if (!isFileCreated) {
            Files.write(outputFile.toPath(), new byte[0], StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
            isFileCreated = true;
          }
          Files.writeString(outputFile.toPath(), line + "\n", StandardOpenOption.APPEND);
          statistics.updateStats(line);
        }
      }
    }
    return isFileCreated;
  }

  private boolean isOnlyText(String str) {
    return !str.isEmpty()
        && !str.matches("^-?\\d+(\\.\\d+)?(E-?\\d+)?$");
  }
}
