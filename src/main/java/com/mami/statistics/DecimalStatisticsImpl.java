package com.mami.statistics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalStatisticsImpl implements Stats<BigDecimal> {
  private long count;
  private BigDecimal sum = BigDecimal.ZERO;
  private BigDecimal min = null;
  private BigDecimal max = null;
  private BigDecimal average = null;

  @Override
  public void updateStats(BigDecimal value) {
    count++;
    sum = sum.add(value);
    if (min == null || value.compareTo(min) < 0) {
      min = value;
    }
    if (max == null || value.compareTo(max) > 0) {
      max = value;
    }
    average = (count == 0) ? null : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
  }

  @Override
  public void printShortStats() {
    System.out.println("Вещественные числа: " + count);
  }

  @Override
  public void printFullStats() {

    if (count == 0) {
      printShortStats();
    } else {
      if (count == 1) {
        printShortStats();
        System.out.println("  Всего однo вещественное число: " + min);
      } else {
        printShortStats();
        if (min.equals(max)) {
          System.out.println("  Все вещественные числа одинаковы: " + min);
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

  public BigDecimal getSum() {
    return sum;
  }

  public BigDecimal getMin() {
    return min;
  }

  public BigDecimal getMax() {
    return max;
  }

  public BigDecimal getAverage() {
    return average;
  }
}
