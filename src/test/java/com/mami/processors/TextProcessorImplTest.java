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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class TextProcessorImplTest {
  @TempDir
  Path tempDir;

  private Stats<String> stats;
  private TextProcessorImpl textProcessor;
  private File inputFile;
  private File outputFile;

  @BeforeEach
  void setUp() {
    stats = Mockito.mock(Stats.class);
    textProcessor = new TextProcessorImpl(stats);
    inputFile = tempDir.resolve("input.txt").toFile();
    outputFile = tempDir.resolve("output.txt").toFile();
  }

  @Test
  void testProcessFile_WithValidText() throws IOException {
    Files.writeString(inputFile.toPath(), "Hello\nnull\n-white34\n", StandardOpenOption.CREATE);

    boolean isFileCreated = textProcessor.processFile(inputFile, outputFile, false);

    String expected = "Hello\nnull\n-white34\n".replace("\r\n", "\n");
    String actual = Files.readString(outputFile.toPath());

    assertTrue(isFileCreated);
    assertTrue(outputFile.exists());
    assertEquals(expected, actual);

    verify(stats, times(1)).updateStats("Hello");
    verify(stats, times(1)).updateStats("null");
    verify(stats, times(1)).updateStats("-white34");
  }

  @Test
  void testProcessFile_WithInvalidAndValidText() throws IOException {
    Files.writeString(inputFile.toPath(), "abc\n12.3\n!@#\n-456\n", StandardOpenOption.CREATE);

    boolean isFileCreated = textProcessor.processFile(inputFile, outputFile, false);

    assertTrue(isFileCreated);

    String expected = "abc\n!@#\n".replace("\r\n", "\n");
    String actual = Files.readString(outputFile.toPath());

    assertEquals(expected, actual);

    verify(stats, times(1)).updateStats("abc");
    verify(stats, times(1)).updateStats("!@#");

    verify(stats, times(2)).updateStats(any(String.class));
  }

  @Test
  void testProcessFile_WithInvalidText() throws IOException {
    Files.writeString(inputFile.toPath(), "-0.0034505\n4\n-2\n12.34\n", StandardOpenOption.CREATE);

    boolean isFileCreated = textProcessor.processFile(inputFile, outputFile, false);

    assertFalse(isFileCreated);
    assertFalse(outputFile.exists());
    verify(stats, never()).updateStats(any(String.class));
  }

  @Test
  void testProcessFile_WithEmptyFile() throws IOException {
    Files.writeString(inputFile.toPath(), "", StandardOpenOption.CREATE);

    boolean isFileCreated = textProcessor.processFile(inputFile, outputFile, false);

    assertFalse(isFileCreated);
    assertFalse(outputFile.exists());
    verify(stats, never()).updateStats(any());
  }

  @Test
  void testProcessFile_AppendsToExistingFile() throws IOException {
    Files.writeString(outputFile.toPath(), "Hello\n", StandardOpenOption.CREATE);
    Files.writeString(inputFile.toPath(), "Shift\nAnd\nAlex\n", StandardOpenOption.CREATE);

    boolean isFileCreated = textProcessor.processFile(inputFile, outputFile, true);

    String expected = "Hello\nShift\nAnd\nAlex\n";
    String actual = Files.readString(outputFile.toPath()).replace("\r\n", "n");

    assertTrue(isFileCreated);
    assertEquals(expected, actual);

    verify(stats, times(1)).updateStats("Shift");
    verify(stats, times(1)).updateStats("And");
    verify(stats, times(1)).updateStats("Alex");
  }
}
