package com.mami;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mami.exceptions.InvalidOutputDirectoryException;
import com.mami.exceptions.MissingOutputDirectoryException;
import com.mami.processors.FileProcessor;
import com.mami.statistics.Stats;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FileFilterUtilTest {
  @Mock
  private Stats<BigInteger> numberStatistics;
  @Mock
  private Stats<BigDecimal> decimalStatistics;
  @Mock
  private Stats<String> textStatistics;

  @Mock
  private FileProcessor numberProcessor;
  @Mock
  private FileProcessor decimalProcessor;
  @Mock
  private FileProcessor textProcessor;

  private FileFilterUtil fileFilterUtil;

  @BeforeEach
  public void setup() throws IOException {
    MockitoAnnotations.openMocks(this);
    fileFilterUtil =
        new FileFilterUtil(numberStatistics, decimalStatistics, textStatistics, numberProcessor, decimalProcessor,
            textProcessor);
    fileFilterUtil.setInputFiles(Arrays.asList(new File("in1.txt"), new File("in2.txt")));
  }

  @Test
  public void testProcessFilesSuccessfully() throws Exception {
    when(numberProcessor.processFile(any(), any(), anyBoolean())).thenReturn(true);
    when(decimalProcessor.processFile(any(), any(), anyBoolean())).thenReturn(true);
    when(textProcessor.processFile(any(), any(), anyBoolean())).thenReturn(true);

    fileFilterUtil.setOutputDir(new File("."));

    int result = fileFilterUtil.call();

    assertEquals(0, result);
    verify(numberProcessor, times(2)).processFile(any(), any(), anyBoolean());
    verify(decimalProcessor, times(2)).processFile(any(), any(), anyBoolean());
    verify(textProcessor, times(2)).processFile(any(), any(), anyBoolean());
  }

  @Test
  public void testMissingOutputDirectory() {
    fileFilterUtil.setOutputDir(null);
    assertThrows(MissingOutputDirectoryException.class, () -> {
      fileFilterUtil.call();
    });
  }

  @Test
  public void testInvalidOutputDirectory() {
    fileFilterUtil.setOutputDir(new File("invalid_dir"));
    assertThrows(InvalidOutputDirectoryException.class, () -> {
      fileFilterUtil.call();
    });
  }

  @Test
  public void testEmptyInputFiles() throws Exception {
    fileFilterUtil.setInputFiles(Collections.emptyList());

    int result = fileFilterUtil.call();

    assertEquals(1, result);
  }

  @Test
  public void testFilterValidAndInvalidFiles()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
    File validFile = new File("in1.txt");
    File invalidFile = new File("invalid.txt");

    Path invalidFilePath = invalidFile.toPath();

    if (Files.exists(invalidFilePath)) {
      Files.delete(invalidFilePath);
    }
    Files.createFile(invalidFilePath);

    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      Files.getFileAttributeView(invalidFilePath, DosFileAttributeView.class)
          .setReadOnly(true);
    } else {
      Files.setPosixFilePermissions(invalidFilePath, PosixFilePermissions.fromString("---------"));
    }

    fileFilterUtil.setInputFiles(Arrays.asList(validFile, invalidFile));

    Method method = FileFilterUtil.class.getDeclaredMethod("filterValidFiles", List.class);
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<File> validFiles = (List<File>) method.invoke(fileFilterUtil,
        Arrays.asList(fileFilterUtil.getInputFiles().get(0), fileFilterUtil.getInputFiles().get(1)));

    assertEquals(1, validFiles.size());
    assertEquals("in1.txt", validFiles.get(0).getName());

    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      Files.getFileAttributeView(invalidFilePath, DosFileAttributeView.class)
          .setReadOnly(false);
    } else {
      Files.setPosixFilePermissions(invalidFilePath, PosixFilePermissions.fromString("rw-r--r--"));
    }
    invalidFile.delete();
  }

  @Test
  public void testFilterValidAndNonExistentFiles()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    File validFile = new File("in1.txt");
    File nonExistantFile = new File("in44.txt");

    fileFilterUtil.setInputFiles(Arrays.asList(validFile, nonExistantFile));

    Method method = FileFilterUtil.class.getDeclaredMethod("filterValidFiles", List.class);
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<File> validFiles = (List<File>) method.invoke(fileFilterUtil,
        Arrays.asList(fileFilterUtil.getInputFiles().get(0), fileFilterUtil.getInputFiles().get(1)));
    assertEquals(1, validFiles.size());
    assertEquals("in1.txt", validFiles.get(0).getName());
  }

  @Test
  public void testStatisticsPrinted() throws Exception {
    fileFilterUtil.setShortStats(true);
    fileFilterUtil.setFullStats(true);

    fileFilterUtil.setOutputDir(new File("."));

    fileFilterUtil.call();

    verify(numberStatistics).printShortStats();
    verify(decimalStatistics).printShortStats();
    verify(textStatistics).printShortStats();

    verify(numberStatistics).printFullStats();
    verify(decimalStatistics).printFullStats();
    verify(textStatistics).printFullStats();
  }

  @Test
  public void testAppendModeEnabledAndAllOutputFilesDoesNotExist() throws Exception {
    fileFilterUtil.setAppendMode(true);
    fileFilterUtil.setOutputDir(new File("."));

    File intFile = new File("./integers.txt");
    File floatFile = new File("./floats.txt");
    File stringFile = new File("./strings.txt");

    when(numberProcessor.processFile(any(), eq(intFile), eq(false))).thenReturn(true);
    when(decimalProcessor.processFile(any(), eq(floatFile), eq(false))).thenReturn(true);
    when(textProcessor.processFile(any(), eq(stringFile), eq(false))).thenReturn(true);

    if (intFile.exists()) {
      intFile.delete();
    }
    if (floatFile.exists()) {
      floatFile.delete();
    }
    if (stringFile.exists()) {
      stringFile.delete();
    }

    int result = fileFilterUtil.call();
    assertEquals(6, result);

    verify(numberProcessor, times(0)).processFile(any(), eq(intFile), eq(true));
    verify(decimalProcessor, times(0)).processFile(any(), eq(floatFile), eq(true));
    verify(textProcessor, times(0)).processFile(any(), eq(stringFile), eq(true));
  }

  @Test
  public void testAppendModeEnabledAndAtLeastOneOutputFileExist() throws Exception {

    fileFilterUtil.setAppendMode(true);
    fileFilterUtil.setOutputDir(new File("."));

    File intFile = new File("./integers.txt");
    File floatFile = new File("./floats.txt");
    File stringFile = new File("./strings.txt");

    intFile.createNewFile();
    if (floatFile.exists()) {
      floatFile.delete();
    }
    if (stringFile.exists()) {
      stringFile.delete();
    }

    when(numberProcessor.processFile(any(), eq(intFile), eq(true))).thenReturn(true);
    when(decimalProcessor.processFile(any(), eq(floatFile), eq(false))).thenReturn(false);
    when(textProcessor.processFile(any(), eq(stringFile), eq(false))).thenReturn(false);

    int result = fileFilterUtil.call();
    assertEquals(0, result);

    verify(numberProcessor, times(2)).processFile(any(), eq(intFile), eq(true));
    verify(decimalProcessor, times(2)).processFile(any(), eq(floatFile), anyBoolean());
    verify(textProcessor, times(2)).processFile(any(), eq(stringFile), anyBoolean());

    intFile.delete();
    floatFile.delete();
    stringFile.delete();
  }

  @Test
  public void testAppendModeEnabledAndTwoOutputFileExist() throws Exception {

    fileFilterUtil.setAppendMode(true);
    fileFilterUtil.setOutputDir(new File("."));

    File intFile = new File("./integers.txt");
    File floatFile = new File("./floats.txt");
    File stringFile = new File("./strings.txt");

    intFile.createNewFile();
    floatFile.createNewFile();

    if (stringFile.exists()) {
      stringFile.delete();
    }

    when(numberProcessor.processFile(any(), eq(intFile), eq(true))).thenReturn(true);
    when(decimalProcessor.processFile(any(), eq(floatFile), eq(true))).thenReturn(true);
    when(textProcessor.processFile(any(), eq(stringFile), eq(false))).thenReturn(false);

    int result = fileFilterUtil.call();
    assertEquals(0, result);

    verify(numberProcessor, times(2)).processFile(any(), eq(intFile), eq(true));
    verify(decimalProcessor, times(2)).processFile(any(), eq(floatFile), eq(true));
    verify(textProcessor, times(2)).processFile(any(), eq(stringFile), anyBoolean());

    intFile.delete();
    floatFile.delete();
    stringFile.delete();
  }

  @Test
  public void testAppendModeDisabledAndFilesExist() throws Exception {
    fileFilterUtil.setAppendMode(false);
    fileFilterUtil.setOutputDir(new File("."));

    File intFile = new File("./integers.txt");
    File floatFile = new File("./floats.txt");
    File stringFile = new File("./strings.txt");

    when(numberProcessor.processFile(any(), eq(intFile), eq(true))).thenReturn(true);
    when(decimalProcessor.processFile(any(), eq(floatFile), eq(true))).thenReturn(true);
    when(textProcessor.processFile(any(), eq(stringFile), eq(true))).thenReturn(true);

    int result = fileFilterUtil.call();
    assertEquals(0, result);

    verify(numberProcessor, times(2)).processFile(any(), eq(intFile), eq(false));
    verify(decimalProcessor, times(2)).processFile(any(), eq(floatFile), eq(false));
    verify(textProcessor, times(2)).processFile(any(), eq(stringFile), eq(false));
  }

  @Test
  public void testAppendModeDisabledAndFilesDoesNotExist() throws Exception {
    fileFilterUtil.setAppendMode(false);
    fileFilterUtil.setOutputDir(new File("."));

    File intFile = new File("./integers.txt");
    File floatFile = new File("./floats.txt");
    File stringFile = new File("./strings.txt");

    when(numberProcessor.processFile(any(), eq(intFile), eq(false))).thenReturn(false);
    when(decimalProcessor.processFile(any(), eq(floatFile), eq(false))).thenReturn(false);
    when(textProcessor.processFile(any(), eq(stringFile), eq(false))).thenReturn(false);

    int result = fileFilterUtil.call();
    assertEquals(0, result);

    verify(numberProcessor, times(2)).processFile(any(), eq(intFile), eq(false));
    verify(decimalProcessor, times(2)).processFile(any(), eq(floatFile), eq(false));
    verify(textProcessor, times(2)).processFile(any(), eq(stringFile), eq(false));
  }

  @Test
  public void testOutputFilesWithPrefix() throws Exception {
    fileFilterUtil.setPrefix("test_");
    fileFilterUtil.setOutputDir(new File("."));

    File intFile = new File("./test_integers.txt");
    File floatFile = new File("./test_floats.txt");
    File stringFile = new File("./test_strings.txt");

    when(numberProcessor.processFile(any(), eq(intFile), anyBoolean())).thenReturn(true);
    when(decimalProcessor.processFile(any(), eq(floatFile), anyBoolean())).thenReturn(true);
    when(textProcessor.processFile(any(), eq(stringFile), anyBoolean())).thenReturn(true);

    int result = fileFilterUtil.call();
    assertEquals(0, result);

    verify(numberProcessor, times(2)).processFile(any(), eq(intFile), anyBoolean());
    verify(decimalProcessor, times(2)).processFile(any(), eq(floatFile), anyBoolean());
    verify(textProcessor, times(2)).processFile(any(), eq(stringFile), anyBoolean());
  }

  @Test
  public void testReadOnlyOutputDirectory() throws IOException, InterruptedException {
    Path readOnlyDirPath = Paths.get("./readonly_dir");

    if (!Files.exists(readOnlyDirPath)) {
      Files.createDirectory(readOnlyDirPath);
    }

    if (isWindows()) {
      setReadOnlyWindows(readOnlyDirPath);
      assertTrue(isDirectoryReadOnlyWindows(readOnlyDirPath), "Directory should be read-only on Windows!");
    } else {
      Files.setPosixFilePermissions(readOnlyDirPath, PosixFilePermissions.fromString("r-xr-xr-x"));
      assertFalse(Files.isWritable(readOnlyDirPath), "Directory should not be writable on Unix!");
    }

    fileFilterUtil.setOutputDir(readOnlyDirPath.toFile());

    assertThrows(InvalidOutputDirectoryException.class, () -> {
      fileFilterUtil.call();
    });

    if (isWindows()) {
      resetWritableWindows(readOnlyDirPath);
    } else {
      Files.setPosixFilePermissions(readOnlyDirPath, PosixFilePermissions.fromString("rwxrwxrwx"));
    }
    Files.deleteIfExists(readOnlyDirPath);
  }

  @Test
  public void testProcessingLargeFiles() throws Exception {
    File largeFile = new File("large_file.txt");

    if (!largeFile.exists() || largeFile.length() < 100_000_000) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(largeFile))) {
        for (int i = 0; i < 10_000_000; i++) {
          writer.write("Line " + i + ": Random text 1234567890\n");
        }
      }
    }

    fileFilterUtil.setInputFiles(Collections.singletonList(largeFile));
    fileFilterUtil.setOutputDir(new File("."));

    when(numberProcessor.processFile(any(), any(), anyBoolean())).thenReturn(true);
    when(decimalProcessor.processFile(any(), any(), anyBoolean())).thenReturn(true);
    when(textProcessor.processFile(any(), any(), anyBoolean())).thenReturn(true);

    int result = fileFilterUtil.call();
    assertEquals(0, result);

    verify(numberProcessor, times(1)).processFile(any(), any(), anyBoolean());
    verify(decimalProcessor, times(1)).processFile(any(), any(), anyBoolean());
    verify(textProcessor, times(1)).processFile(any(), any(), anyBoolean());

    largeFile.delete();
  }

  @Test
  public void testProcessingMixOfValidAndCorruptFiles() throws Exception {
    File validFile = new File("in11.txt");
    File corruptEmptyFile = new File("corrupt.txt");
    corruptEmptyFile.createNewFile();

    fileFilterUtil.setInputFiles(Arrays.asList(validFile, corruptEmptyFile));
    fileFilterUtil.setOutputDir(new File("."));

    when(numberProcessor.processFile(any(), eq(validFile), anyBoolean())).thenReturn(true);
    when(numberProcessor.processFile(any(), eq(corruptEmptyFile), anyBoolean())).thenReturn(false);

    int result = fileFilterUtil.call();
    File intFile = new File("./integers.txt");

    assertEquals(0, result);
    verify(numberProcessor, times(1)).processFile(eq(validFile), eq(intFile), anyBoolean());
    verify(numberProcessor, times(1)).processFile(eq(corruptEmptyFile), eq(intFile), anyBoolean());

    Method method = FileFilterUtil.class.getDeclaredMethod("filterValidFiles", List.class);
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<File> validFiles = (List<File>) method.invoke(fileFilterUtil,
        Arrays.asList(fileFilterUtil.getInputFiles().get(0), fileFilterUtil.getInputFiles().get(1)));

    assertEquals(1, validFiles.size());
    assertEquals("in11.txt", validFiles.get(0).getName());

    corruptEmptyFile.delete();
  }

  private boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("win");
  }

  private void setReadOnlyWindows(Path path) throws IOException {
    Files.getFileAttributeView(path, DosFileAttributeView.class)
        .setReadOnly(true);
  }

  private void resetWritableWindows(Path path) throws IOException {
    Files.getFileAttributeView(path, DosFileAttributeView.class)
        .setReadOnly(false);
  }

  private boolean isDirectoryReadOnlyWindows(Path path) throws IOException {
    DosFileAttributes attr = Files.readAttributes(path, DosFileAttributes.class);
    return attr.isReadOnly();
  }
}
