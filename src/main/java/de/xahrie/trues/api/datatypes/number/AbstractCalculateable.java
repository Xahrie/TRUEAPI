package de.xahrie.trues.api.datatypes.number;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractCalculateable<E extends Number> extends Numeric<E> implements Comparable<E> {
  @Serial
  private static final long serialVersionUID = 2719280032288969714L;


  public AbstractCalculateable(Double value) {
    super(value);
  }

  public E add(E summand) {
    return add(summand.doubleValue());
  }

  public E subtract(E subtrahend) {
    return subtract(subtrahend.doubleValue());
  }

  public E multiply(E factor) {
    return multiply(factor.doubleValue());
  }

  public E divide(E divisor) {
    return divide(divisor.doubleValue());
  }

  public E modulo(E rest) {
    return modulo(rest.doubleValue());
  }
  public E power(E exponent) {
    return power(exponent.doubleValue());
  }

  public abstract E add(double summand);
  public abstract E subtract(double subtrahend);
  public abstract E multiply(double factor);
  public abstract E divide(double divisor);
  public abstract E modulo(double rest);
  public abstract E power(double exponent);
  public abstract void increment();
  public abstract void decrement();

  @Override
  public int compareTo(@NotNull E o) {
    return Double.compare(doubleValue(), o.doubleValue());
  }

}
