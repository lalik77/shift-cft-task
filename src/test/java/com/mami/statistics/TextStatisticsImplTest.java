package com.mami.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextStatisticsImplTest {
  private TextStatisticsImpl stats;

  @BeforeEach
  void setUp() {
    stats = new TextStatisticsImpl();
  }

  @Test
  void testInitialState() {
    stats.printShortStats();
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithSingleCyrilLicString() {
    stats.updateStats("Привет Алекс");
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithSingleArabicString() {
    stats.updateStats("المكرّفاس");
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithMultipleStrings() {
    stats.updateStats("hi");
    stats.updateStats("hello world");
    stats.updateStats("Java");
    assertEquals(3, stats.getCount());
    assertEquals(2, stats.getMin());
    assertEquals(11, stats.getMax());
    stats.printFullStats();
  }

  @Test
  void testEmptyStringHandling() {
    stats.updateStats("");
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithMultipleStringsWithAnEmptyString() {
    stats.updateStats("");
    stats.updateStats("hello world");
    stats.updateStats("Java");
    assertEquals(2, stats.getCount());
    assertEquals(4, stats.getMin());
    assertEquals(11, stats.getMax());
    stats.printFullStats();
  }

  @Test
  void testUpdateStatsWithMultipleStringsAndAreEquals() {
    stats.updateStats("hello world");
    stats.updateStats("hello world");
    stats.updateStats("hello world");
    assertEquals(3, stats.getCount());
    assertEquals(11, stats.getMin());
    assertEquals(11, stats.getMax());
    stats.printFullStats();
  }
}
