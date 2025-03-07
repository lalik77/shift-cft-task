package com.mami.statistics;

public class TextStatisticsImpl implements Stats<String> {
  private long count;
  private int min = Integer.MAX_VALUE;
  private int max = Integer.MIN_VALUE;

  @Override
  public void updateStats(String str) {
    if (str.isEmpty()) {
      return;
    }
    count++;
    min = Math.min(min, str.length());
    max = Math.max(max, str.length());
  }

  @Override
  public void printShortStats() {
    System.out.println("Количество строк: " + count);
  }

  @Override
  public void printFullStats() {
    if (count == 0) {
      printShortStats();
    } else {
      if (count == 1) {
        printShortStats();
        System.out.println("  Всего одна строка, длина : " + min);
      } else {
        printShortStats();

        if (min == max) {
          System.out.println("  Все строки одинаковы, длина : " + min);
        } else {
          System.out.println("  Самая короткая строка: " + min);
          System.out.println("  Самая длинная строка: " + max);
        }
      }
    }
  }

  public long getCount() {
    return count;
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }
}
