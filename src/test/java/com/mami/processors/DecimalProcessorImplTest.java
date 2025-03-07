package com.mami.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mami.statistics.Stats;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class DecimalProcessorImplTest {
  @TempDir
  Path tempDir;

  private Stats<BigDecimal> stats;
  private DecimalProcessorImpl decimalProcessor;
  private File inputFile;
  private File outputFile;

  @BeforeEach
  void setup() {
    stats = Mockito.mock(Stats.class);
    decimalProcessor = new DecimalProcessorImpl(stats);
    inputFile = tempDir.resolve("input.txt").toFile();
    outputFile = tempDir.resolve("output.txt").toFile();
  }

  @Test
  void testProcessFile_WithValidDecimals() throws IOException {
    Files.writeString(inputFile.toPath(), "3.1415\n-0.001\n1.528535047E-25\n", StandardOpenOption.CREATE);

    boolean isFileCreated = decimalProcessor.processFile(inputFile, outputFile, false);

    String expected = "3.1415\n-0.001\n1.528535047E-25\n".replace("\r\n", "\n");
    String actual = Files.readString(outputFile.toPath());

    assertTrue(isFileCreated);
    assertTrue(outputFile.exists());
    assertEquals(expected, actual);

    verify(stats, times(1)).updateStats(new BigDecimal("3.1415"));
    verify(stats, times(1)).updateStats(new BigDecimal("-0.001"));
    verify(stats, times(1)).updateStats(new BigDecimal("1.528535047E-25"));
  }

  @Test
  void testProcessFile_WithInvalidAndValidDecimals() throws IOException {
    Files.writeString(inputFile.toPath(), "abc\n-0.001\n!@#\n1.528535047E-25\n", StandardOpenOption.CREATE);

    boolean isFileCreated = decimalProcessor.processFile(inputFile, outputFile, false);

    String expected = "-0.001\n1.528535047E-25\n".replace("\r\n", "\n");
    String actual = Files.readString(outputFile.toPath());

    assertTrue(isFileCreated);
    assertEquals(expected, actual);

    verify(stats, times(1)).updateStats(new BigDecimal("-0.001"));
    verify(stats, times(1)).updateStats(new BigDecimal("1.528535047E-25"));

    verify(stats, times(2)).updateStats(any(BigDecimal.class));
  }

  @Test
  void testProcessFile_WithInvalidDecimals() throws IOException {
    Files.writeString(inputFile.toPath(), "abc\n!@#\ndef\n12\n-45\n", StandardOpenOption.CREATE);

    boolean isFileCreated = decimalProcessor.processFile(inputFile, outputFile, false);

    assertFalse(isFileCreated);
    assertFalse(outputFile.exists());
    verify(stats, never()).updateStats(any(BigDecimal.class));
  }

  @Test
  void testProcessFile_WithEmptyFile() throws IOException {
    Files.writeString(inputFile.toPath(), "", StandardOpenOption.CREATE);

    boolean isFileCreated = decimalProcessor.processFile(inputFile, outputFile, false);

    assertFalse(isFileCreated);
    assertFalse(outputFile.exists());
    verify(stats, never()).updateStats(any());
  }

  @Test
  void testProcessFile_AppendsToExistingFile() throws IOException {
    Files.writeString(outputFile.toPath(), "-0.001\n", StandardOpenOption.CREATE);
    Files.writeString(inputFile.toPath(), "1.528535047E-25\n3.1415\n", StandardOpenOption.CREATE);

    boolean isFileCreated = decimalProcessor.processFile(inputFile, outputFile, true);

    String expected = "-0.001\n1.528535047E-25\n3.1415\n".replace("\r\n", "\n");
    String actual = Files.readString(outputFile.toPath());

    assertTrue(isFileCreated);
    assertEquals(expected, actual);

    verify(stats, times(1)).updateStats(new BigDecimal("1.528535047E-25"));
    verify(stats, times(1)).updateStats(new BigDecimal("3.1415"));
  }
}
