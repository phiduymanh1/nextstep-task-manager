package org.example.nextstepbackend.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

public class PositionUtils {
  private static final BigDecimal STEP = BigDecimal.ONE;
  private static final int SCALE = 10;
  private static final BigDecimal MIN_GAP = new BigDecimal("0.00001");

  private PositionUtils() {
    // prevent instantiation
  }

  public record MoveResult<T>(T prev, T next, BigDecimal position, boolean needRebalance) {}

  /** Calculate position by prev and next */
  public static BigDecimal calculate(BigDecimal prev, BigDecimal next) {
    // First insert
    if (prev == null && next == null) {
      return STEP;
    }

    // On top insert
    if (prev == null) {
      return next.subtract(STEP);
    }

    // Down insert
    if (next == null) {
      return prev.add(STEP);
    }

    // Insert in the middle
    return prev.add(next).divide(BigDecimal.valueOf(2), SCALE, RoundingMode.HALF_UP);
  }

  /** Check if a rebalance is needed. */
  public static boolean shouldRebalance(BigDecimal prev, BigDecimal next) {
    if (prev == null || next == null) return false;

    return next.subtract(prev).compareTo(MIN_GAP) < 0;
  }

  /** Resolve full move logic (generic) */
  public static <T> MoveResult<T> resolve(T prev, T next, Function<T, BigDecimal> getPosition) {
    BigDecimal prevPos = (prev != null) ? getPosition.apply(prev) : null;
    BigDecimal nextPos = (next != null) ? getPosition.apply(next) : null;

    BigDecimal position = calculate(prevPos, nextPos);

    boolean needRebalance = shouldRebalance(prevPos, nextPos);

    return new MoveResult<>(prev, next, position, needRebalance);
  }
}
