package com.mami.statistics;

import java.math.BigInteger;

public class NumberStatisticsImpl implements Stats<BigInteger> {
  private long count;
  private BigInteger sum = BigInteger.ZERO;
  private BigInteger min = null;
  private BigInteger max = null;
  private BigInteger average = null;

  @Override
  public void updateStats(BigInteger value) {

    if (value == null) {
      return;
    }
    count++;
    sum = sum.add(value);
    if (min == null || value.compareTo(min) < 0) {
      min = value;
    }
    if (max == null || value.compareTo(max) > 0) {
      max = value;
    }
    average = (count == 0) ? null : sum.divide(BigInteger.valueOf(count));
  }

  @Override
  public void printShortStats() {
    System.out.println("Целые числа: " + count);
  }

  @Override
  public void printFullStats() {
    if (count == 0) {
      printShortStats();
    } else {
      if (count == 1) {
        printShortStats();
        System.out.println("  Всего однo целое число: " + min);
      } else {
        printShortStats();
        if (min.equals(max)) {
          System.out.println("  Все целые числа одинаковы: " + min);
          System.out.println("  Сумма: " + sum);
        } else {
          System.out.println("  Минимальное: " + min);
          System.out.println("  Максимальное: " + max);
          System.out.println("  Сумма: " + sum);
          System.out.println("  Среднее: " + average);
        }
      }
    }
  }

  public long getCount() {
    return count;
  }

  public BigInteger getAverage() {
    return average;
  }

  public BigInteger getSum() {
    return sum;
  }

  public BigInteger getMin() {
    return min;
  }

  public BigInteger getMax() {
    return max;
  }
}
