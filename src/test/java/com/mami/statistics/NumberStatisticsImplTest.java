package com.mami.statistics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NumberStatisticsImplTest {
  private NumberStatisticsImpl stats;

  @BeforeEach
  void setup() {
    stats = new NumberStatisticsImpl();
  }

  @Test
  void testInitialState() {
    stats.printShortStats();
    stats.printFullStats();
    assertDoesNotThrow(() -> stats.printFullStats());
  }

  @Test
  void testUpdateStatsWithSingleValue() {
    stats.updateStats(BigInteger.valueOf(10));
    assertEquals(1, stats.getCount());
    assertEquals(BigInteger.valueOf(10), stats.getSum());
    assertEquals(BigInteger.valueOf(10), stats.getMin());
    assertEquals(BigInteger.valueOf(10), stats.getMax());
    assertEquals(BigInteger.valueOf(10), stats.getAverage());
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithMultipleValues() {
    stats.updateStats(BigInteger.valueOf(5));
    stats.updateStats(BigInteger.valueOf(15));
    stats.updateStats(BigInteger.valueOf(10));

    assertEquals(3, stats.getCount());
    assertEquals(BigInteger.valueOf(30), stats.getSum());
    assertEquals(BigInteger.valueOf(5), stats.getMin());
    assertEquals(BigInteger.valueOf(15), stats.getMax());
    assertEquals(BigInteger.valueOf(10), stats.getAverage());
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithNegativeValues() {
    stats.updateStats(BigInteger.valueOf(-5));
    stats.updateStats(BigInteger.valueOf(-10));
    stats.updateStats(BigInteger.valueOf(0));

    assertEquals(3, stats.getCount());
    assertEquals(BigInteger.valueOf(-15), stats.getSum());
    assertEquals(BigInteger.valueOf(-10), stats.getMin());
    assertEquals(BigInteger.ZERO, stats.getMax());
    assertEquals(BigInteger.valueOf(-5), stats.getAverage());
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithMultipleValuesAndAreEquals() {
    stats.updateStats(BigInteger.valueOf(5));
    stats.updateStats(BigInteger.valueOf(5));
    stats.updateStats(BigInteger.valueOf(5));

    assertEquals(3, stats.getCount());
    assertEquals(BigInteger.valueOf(15), stats.getSum());
    assertEquals(BigInteger.valueOf(5), stats.getMin());
    assertEquals(BigInteger.valueOf(5), stats.getMax());
    assertEquals(BigInteger.valueOf(5), stats.getAverage());
    stats.printFullStats();
  }
}
