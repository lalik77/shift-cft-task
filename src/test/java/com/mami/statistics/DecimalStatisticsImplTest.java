package com.mami.statistics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DecimalStatisticsImplTest {
  private DecimalStatisticsImpl stats;

  @BeforeEach
  void setUp() {
    stats = new DecimalStatisticsImpl();
  }

  @Test
  void testInitialState() {
    stats.printShortStats();
    stats.printFullStats();
    assertDoesNotThrow(() -> stats.printFullStats());
  }

  @Test
  void testUpdateStatsWithSingleValue() {
    BigDecimal decimalToBeTested = new BigDecimal("10.50");
    stats.updateStats(decimalToBeTested);
    assertEquals(1, stats.getCount());
    assertEquals(decimalToBeTested, stats.getSum());
    assertEquals(decimalToBeTested, stats.getMin());
    assertEquals(decimalToBeTested, stats.getMax());
    assertEquals(decimalToBeTested, stats.getAverage());
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithMultipleValues() {
    BigDecimal decimalToBeTested1 = new BigDecimal("3.2");
    BigDecimal decimalToBeTested2 = new BigDecimal("7.8");
    BigDecimal decimalToBeTested3 = new BigDecimal("5.0");

    stats.updateStats(decimalToBeTested1);
    stats.updateStats(decimalToBeTested2);
    stats.updateStats(decimalToBeTested3);

    assertEquals(3, stats.getCount());
    assertEquals(BigDecimal.valueOf(16.0), stats.getSum());
    assertEquals(BigDecimal.valueOf(3.2), stats.getMin());
    assertEquals(BigDecimal.valueOf(7.8), stats.getMax());
    assertEquals(BigDecimal.valueOf(5.33), stats.getAverage());
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithNegativeValues() {
    BigDecimal decimalToBeTested1 = new BigDecimal("-1.1");
    BigDecimal decimalToBeTested2 = new BigDecimal("-5.3");
    BigDecimal decimalToBeTested3 = new BigDecimal("2.4");

    stats.updateStats(decimalToBeTested1);
    stats.updateStats(decimalToBeTested2);
    stats.updateStats(decimalToBeTested3);

    assertEquals(3, stats.getCount());
    assertEquals(BigDecimal.valueOf(-4.0), stats.getSum());
    assertEquals(BigDecimal.valueOf(-5.3), stats.getMin());
    assertEquals(BigDecimal.valueOf(2.4), stats.getMax());
    assertEquals(BigDecimal.valueOf(-1.33), stats.getAverage());
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithMultipleValuesAndAreEquals() {
    BigDecimal decimalToBeTested1 = new BigDecimal("1.528535047E-25");
    BigDecimal decimalToBeTested2 = new BigDecimal("1.528535047E-25");
    BigDecimal decimalToBeTested3 = new BigDecimal("1.528535047E-25");

    stats.updateStats(decimalToBeTested1);
    stats.updateStats(decimalToBeTested2);
    stats.updateStats(decimalToBeTested3);

    assertEquals(3, stats.getCount());
    assertEquals(new BigDecimal("4.585605141E-25"), stats.getSum());
    assertEquals(new BigDecimal("1.528535047E-25"), stats.getMin());
    assertEquals(new BigDecimal("1.528535047E-25"), stats.getMax());
    assertEquals(new BigDecimal("0.00"), stats.getAverage());
    stats.printFullStats();
  }
}
