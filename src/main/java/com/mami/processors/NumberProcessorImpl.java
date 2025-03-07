package com.mami.processors;

import com.mami.statistics.Stats;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class NumberProcessorImpl implements FileProcessor {
  private final Stats<BigInteger> statistics;

  public NumberProcessorImpl(Stats<BigInteger> statistics) {
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

        if (isOnlyDigits(line)) {
          if (!isFileCreated) {
            Files.write(outputFile.toPath(), new byte[0], StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
            isFileCreated = true;
          }
          Files.writeString(outputFile.toPath(), line + "\n", StandardOpenOption.APPEND);
          statistics.updateStats(new BigInteger(line));
        }
      }
    }
    return isFileCreated;
  }

  private boolean isOnlyDigits(String str) {
    return str.matches("^\\d+$");
  }
}
