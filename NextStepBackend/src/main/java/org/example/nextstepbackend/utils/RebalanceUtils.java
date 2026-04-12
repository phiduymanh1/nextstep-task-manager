package org.example.nextstepbackend.utils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Utility to rebalance positions on a list of items.
 *
 * <p>- Provides a simple rebalance that starts at 1 and increments by 1. - Provides an overload to
 * customize start and step values. - Null-safe for the items list (no-op) and validates the setter.
 */
public final class RebalanceUtils {

  private static final BigDecimal DEFAULT_STEP = new BigDecimal("1000");

  private RebalanceUtils() {
    // prevent instantiation
    throw new AssertionError("No instances");
  }

  /** Rebalance items starting at 1 and incrementing by 1. */
  public static <T> void rebalance(List<T> items, BiConsumer<T, BigDecimal> setPosition) {
    rebalance(items, setPosition, BigDecimal.ONE, DEFAULT_STEP);
  }

  /**
   * Rebalance items starting at {@code start} and incrementing by {@code step} for each item. If
   * {@code items} is null or empty this is a no-op. {@code setPosition} must not be null.
   */
  public static <T> void rebalance(
      List<T> items, BiConsumer<T, BigDecimal> setPosition, BigDecimal start, BigDecimal step) {

    Objects.requireNonNull(setPosition, "setPosition must not be null");

    if (items == null || items.isEmpty()) return;

    BigDecimal current = (start == null) ? BigDecimal.ONE : start;
    BigDecimal increment = (step == null) ? DEFAULT_STEP : step;

    for (T item : items) {
      setPosition.accept(item, current);
      current = current.add(increment);
    }
  }
}
