package com.mami.statistics;

public interface Stats<T> {
  void updateStats(T value);

  void printShortStats();

  void printFullStats();
}
