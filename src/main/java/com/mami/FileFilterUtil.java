package com.mami;

import com.mami.exceptions.InvalidOutputDirectoryException;
import com.mami.exceptions.MissingOutputDirectoryException;
import com.mami.processors.FileProcessor;
import com.mami.statistics.Stats;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "Number and Text Utility",
    mixinStandardHelpOptions = true,
    version = "1.0",
    abbreviateSynopsis = true,
    synopsisHeading = "",
    customSynopsis = {""},
    description = "Утилита которая фильтрует содержимое файлов на целые числа, строки и вещественные числа.\n")
public class FileFilterUtil implements Callable<Integer> {

  @Option(names = {"-s"}, description = "Вывести краткую статистику по количеству элементов")
  private boolean shortStats = false;

  @Option(names = {"-f"}, description = "Вывести полную статистику по данным")
  private boolean fullStats = false;

  @Option(names = {"-a"}, description = "Добавлять данные в существующие файлы")
  private boolean appendMode = false;

  @Option(names = {"-o"}, description = "Каталог для сохранения выходных файлов", defaultValue = "./")
  private File outputDir;

  @Option(names = {"-p"}, description = "Префикс выходных файлов", defaultValue = "")
  private String prefix = "";

  @Parameters(description = "Список входных файлов")
  private List<File> inputFiles = new ArrayList<>();

  private final FileProcessor numberProcessor;
  private final FileProcessor decimalProcessor;
  private final FileProcessor textProcessor;

  private final Stats<BigInteger> numberStatistics;
  private final Stats<BigDecimal> decimalStatistics;
  private final Stats<String> textStatistics;

  public FileFilterUtil(Stats<BigInteger> numberStatistics, Stats<BigDecimal> decimalStatistics,
                        Stats<String> textStatistics, FileProcessor numberProcessor, FileProcessor decimalProcessor,
                        FileProcessor textProcessor) {
    this.numberStatistics = numberStatistics;
    this.decimalStatistics = decimalStatistics;
    this.textStatistics = textStatistics;
    this.numberProcessor = numberProcessor;
    this.decimalProcessor = decimalProcessor;
    this.textProcessor = textProcessor;
  }

  @Override
  public Integer call() throws Exception {

    if (isInputFilesEmpty()) {
      return 1;
    }
    if (filterValidFiles(inputFiles).isEmpty()) {
      return 1;
    }

    if (outputDir == null) {
      throw new MissingOutputDirectoryException();
    }

    if (!outputDir.exists() || !outputDir.isDirectory()) {
      throw new InvalidOutputDirectoryException("Указанный путь не является директорией: " + outputDir);
    }

    if (!canWriteToDirectory(outputDir)) {
      throw new InvalidOutputDirectoryException("Указанный путь недоступен для записи: " + outputDir);
    }

    boolean isIntFileCreated = false;
    boolean isFloatFileCreated = false;
    boolean isStringFileCreated = false;

    File intFile = new File(outputDir, prefix + "integers.txt");
    File floatFile = new File(outputDir, prefix + "floats.txt");
    File stringFile = new File(outputDir, prefix + "strings.txt");

    try {
      if (!appendMode) {
        for (File file : inputFiles) {

          if (file.exists() && file.canRead()) {
            isIntFileCreated = numberProcessor.processFile(file, intFile, isIntFileCreated);
            isFloatFileCreated = decimalProcessor.processFile(file, floatFile, isFloatFileCreated);
            isStringFileCreated = textProcessor.processFile(file, stringFile, isStringFileCreated);
          }
        }
      } else {

        isIntFileCreated = intFile.exists();
        isFloatFileCreated = floatFile.exists();
        isStringFileCreated = stringFile.exists();

        if (!isIntFileCreated && !isFloatFileCreated && !isStringFileCreated) {
          System.out.println("""
              Выходные файлы не существуют.
              Ошибка: Опция -a (режим добавления) может быть использована только в том случае, если хотя бы один 
              из выходных файлов уже существует..
              """);
          return 6;
        } else {

          for (File file : inputFiles) {

            if (file.exists() && file.canRead()) {
              isIntFileCreated = numberProcessor.processFile(file, intFile, isIntFileCreated);
              isFloatFileCreated = decimalProcessor.processFile(file, floatFile, isFloatFileCreated);
              isStringFileCreated = textProcessor.processFile(file, stringFile, isStringFileCreated);
            }
          }
        }
      }
      System.out.println(
          "Фильтрация завершена.\nФайлы сохранены в " + (outputDir.getPath().equals(".") ? "./" : outputDir.getPath()));

      if (shortStats) {
        System.out.println("Краткая статистика:");
        numberStatistics.printShortStats();
        decimalStatistics.printShortStats();
        textStatistics.printShortStats();
      }
      if (fullStats) {
        System.out.println("Полная статистика:");
        numberStatistics.printFullStats();
        decimalStatistics.printFullStats();
        textStatistics.printFullStats();
      }

    } catch (IOException e) {
      System.out.println(outputDir.canWrite());
      System.err.println("Ошибка чтения файла(ов): " + e.getMessage());
      return 5;
    }
    return 0;
  }

  private boolean isInputFilesEmpty() {
    if (inputFiles.isEmpty()) {
      System.out.println("Воспользуйтесь опцией -h");
      System.err.println("Ошибка: Не переданы входные файлы!");
      return true;
    }
    return false;
  }

  private List<File> filterValidFiles(List<File> inputFiles) {
    List<File> validFiles = new ArrayList<>();

    for (File file : inputFiles) {
      if (!file.exists()) {
        System.err.println("Ошибка: Файл " + file.getAbsolutePath() + " не существует. Пропускаем.");
      } else if (!file.canRead()) {
        System.err.println("Ошибка: Файл " + file.getAbsolutePath() + " недоступен для чтения. Пропускаем.");
      } else if (file.length() == 0) {
        System.err.println("Ошибка: Файл " + file.getAbsolutePath() + " пустой. Пропускаем.");
      } else {
        validFiles.add(file);
      }
    }
    return validFiles;
  }

  private boolean canWriteToDirectory(File dir) throws IOException {
    Path path = dir.toPath();

    if (isWindows()) {
      DosFileAttributes attrs = Files.readAttributes(path, DosFileAttributes.class);
      return !attrs.isReadOnly();
    } else {
      return dir.canWrite();
    }
  }

  private boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("win");
  }

  public List<File> getInputFiles() {
    return inputFiles;
  }

  public void setInputFiles(List<File> inputFiles) {
    this.inputFiles = inputFiles;
  }

  public void setOutputDir(File outputDir) {
    this.outputDir = outputDir;
  }

  public void setShortStats(boolean shortStats) {
    this.shortStats = shortStats;
  }

  public void setFullStats(boolean fullStats) {
    this.fullStats = fullStats;
  }

  public void setAppendMode(boolean appendMode) {
    this.appendMode = appendMode;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

}
