package com.mami.config;

import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.IHelpFactory;
import picocli.CommandLine.Model.CommandSpec;

public class CustomHelpFactory implements IHelpFactory {
  @Override
  public Help create(CommandSpec commandSpec, CommandLine.Help.ColorScheme colorScheme) {
    return new Help(commandSpec, colorScheme) {
      @Override
      public String optionList() {

        String originalOutput = super.optionList();
        return originalOutput
            .replace("Show this help message and exit", "Вывести справку и выйти")
            .replace("Print version information and exit", "Показать версию утилиты")
            .replace("The output directory", "Каталог для сохранения выходных файлов")
            .replace("The prefix for output files", "Префикс выходных файлов")
            .replace("-o=<outputDir>", "-o            ")
            .replace("-p=<prefix>", "-p         ");
      }

      @Override
      public String parameterList() {
        return "";
      }
    };
  }
}
