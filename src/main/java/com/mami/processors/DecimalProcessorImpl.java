package com.mami.processors;

import com.mami.statistics.Stats;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class DecimalProcessorImpl implements FileProcessor {
  private final Stats<BigDecimal> statistics;

  public DecimalProcessorImpl(Stats<BigDecimal> statistics) {
    this.statistics = statistics;
  }

  @Override
  public boolean processFile(File inputFile, File outputFile, boolean isFileCreated) throws IOException {

    if (inputFile == null || outputFile == null) {
      return false;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }

        if (isDecimal(line)) {
          if (!isFileCreated) {
            Files.write(outputFile.toPath(), new byte[0], StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
            isFileCreated = true;
          }
          Files.writeString(outputFile.toPath(), line + "\n", StandardOpenOption.APPEND);
          statistics.updateStats(new BigDecimal(line));
        }
      }
    }
    return isFileCreated;
  }

  private boolean isDecimal(String str) {
    return !str.isEmpty()
        && str.matches("^-?\\d+\\.\\d+(?:[eE][-+]?\\d+)?$");
  }
}
