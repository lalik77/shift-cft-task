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
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class NumberProcessorImplTest {
  @TempDir
  Path tempDir;

  private Stats<BigInteger> stats;
  private NumberProcessorImpl numberProcessor;
  private File inputFile;
  private File outputFile;


  @BeforeEach
  void setUp() {
    stats = Mockito.mock(Stats.class);
    numberProcessor = new NumberProcessorImpl(stats);
    inputFile = tempDir.resolve("input.txt").toFile();
    outputFile = tempDir.resolve("output.txt").toFile();
  }

  @Test
  void testProcessFile_WithValidNumbers() throws IOException {
    Files.writeString(inputFile.toPath(), "123\n456\n789\n", StandardOpenOption.CREATE);

    boolean isFileCreated = numberProcessor.processFile(inputFile, outputFile, false);

    String expected = "123\n456\n789\n".replace("\r\n", "\n");
    String actual = Files.readString(outputFile.toPath());

    assertTrue(isFileCreated);
    assertTrue(outputFile.exists());
    assertEquals(expected, actual);

    verify(stats, times(1)).updateStats(new BigInteger("123"));
    verify(stats, times(1)).updateStats(new BigInteger("456"));
    verify(stats, times(1)).updateStats(new BigInteger("789"));
  }

  @Test
  void testProcessFile_WithInvalidAndValidNumbers() throws IOException {
    Files.writeString(inputFile.toPath(), "abc\n123\n!@#\n456\n", StandardOpenOption.CREATE);

    boolean isFileCreated = numberProcessor.processFile(inputFile, outputFile, false);

    String expected = "123\n456\n".replace("\r\n", "\n");
    String actual = Files.readString(outputFile.toPath());

    assertTrue(isFileCreated);
    assertEquals(expected, actual);

    verify(stats, times(1)).updateStats(new BigInteger("123"));
    verify(stats, times(1)).updateStats(new BigInteger("456"));

    verify(stats, times(2)).updateStats(any(BigInteger.class));
  }

  @Test
  void testProcessFile_WithInvalidNumbers() throws IOException {
    Files.writeString(inputFile.toPath(), "abc\n!@#\ndef\n12.34\n", StandardOpenOption.CREATE);

    boolean isFileCreated = numberProcessor.processFile(inputFile, outputFile, false);

    assertFalse(isFileCreated);
    assertFalse(outputFile.exists());
    verify(stats, never()).updateStats(any(BigInteger.class));
  }

  @Test
  void testProcessFile_WithEmptyFile() throws IOException {
    Files.writeString(inputFile.toPath(), "", StandardOpenOption.CREATE);

    boolean isFileCreated = numberProcessor.processFile(inputFile, outputFile, false);

    assertFalse(isFileCreated);
    assertFalse(outputFile.exists());
    verify(stats, never()).updateStats(any());
  }

  @Test
  void testProcessFile_AppendsToExistingFile() throws IOException {
    Files.writeString(outputFile.toPath(), "100\n", StandardOpenOption.CREATE);
    Files.writeString(inputFile.toPath(), "200\n300\n", StandardOpenOption.CREATE);

    boolean isFileCreated = numberProcessor.processFile(inputFile, outputFile, true);

    String expected = "100\n200\n300\n".replace("\r\n", "\n");
    String actual = Files.readString(outputFile.toPath());

    assertTrue(isFileCreated);
    assertEquals(expected, actual);

    verify(stats, times(1)).updateStats(new BigInteger("200"));
    verify(stats, times(1)).updateStats(new BigInteger("300"));
  }
}
