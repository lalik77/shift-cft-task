package com.mami;

import com.mami.config.CustomHelpFactory;
import com.mami.exceptions.InvalidOutputDirectoryException;
import com.mami.exceptions.MissingOutputDirectoryException;
import com.mami.processors.DecimalProcessorImpl;
import com.mami.processors.FileProcessor;
import com.mami.processors.NumberProcessorImpl;
import com.mami.processors.TextProcessorImpl;
import com.mami.statistics.DecimalStatisticsImpl;
import com.mami.statistics.NumberStatisticsImpl;
import com.mami.statistics.Stats;
import com.mami.statistics.TextStatisticsImpl;
import java.math.BigDecimal;
import java.math.BigInteger;
import picocli.CommandLine;

public class App {
  public static void main(String[] args) {

    CommandLine commandLine = getCommandLine();
    commandLine.setHelpFactory(new CustomHelpFactory());

    commandLine.setParameterExceptionHandler((ex, args1) -> {
      if (ex instanceof CommandLine.MissingParameterException) {
        CommandLine.MissingParameterException mpe = (CommandLine.MissingParameterException) ex;
        String missingOptions = mpe.getMissing().stream()
            .map(arg -> {
              String optionName =
                  (arg instanceof CommandLine.Model.OptionSpec) ? ((CommandLine.Model.OptionSpec) arg).longestName() :
                      "неизвестный параметр";
              return String.format("Ошибка: не указаны обязательные параметры для %s", optionName);
            })
            .reduce((a, b) -> a + "\n" + b)
            .orElse("Ошибка: отсутствуют параметры");
        System.out.println("Воспользуйтесь опцией -h");
        System.err.println(missingOptions);

        return 2;
      }
      return commandLine.getCommandSpec().exitCodeOnInvalidInput();
    });

    commandLine.setExecutionExceptionHandler((ex, cmd, parseResult) -> {

      if (ex instanceof MissingOutputDirectoryException) {
        System.err.println("Ошибка: " + ex.getMessage());
        return 3;
      } else if (ex instanceof InvalidOutputDirectoryException) {
        System.err.println("Ошибка: " + ex.getMessage());
        return 4;
      }
      return 1;
    });

    int exitCode = commandLine.execute(args);
    System.exit(exitCode);
  }

  private static CommandLine getCommandLine() {
    Stats<BigInteger> numberStatistics = new NumberStatisticsImpl();
    Stats<BigDecimal> decimalStatistics = new DecimalStatisticsImpl();
    Stats<String> textStatistics = new TextStatisticsImpl();
    FileProcessor intProcessor = new NumberProcessorImpl(numberStatistics);
    FileProcessor floatProcessor = new DecimalProcessorImpl(decimalStatistics);
    FileProcessor stringProcessor = new TextProcessorImpl(textStatistics);
    FileFilterUtil app =
        new FileFilterUtil(numberStatistics, decimalStatistics, textStatistics, intProcessor, floatProcessor,
            stringProcessor);
    return new CommandLine(app);
  }
}
