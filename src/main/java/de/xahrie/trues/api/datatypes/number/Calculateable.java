package de.xahrie.trues.api.datatypes.number;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

public abstract class Calculateable<T extends Number> extends AbstractCalculateable<T> {
  @Serial
  private static final long serialVersionUID = 449609028569927397L;
  private final Class<T> genericType;

  public Calculateable(Class<T> type, Double value) {
    super(value);
    this.genericType = type;
  }

  private T newInstance(Double value) {
    try {
      return genericType.getDeclaredConstructor(Double.class).newInstance(value);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public T add(double summand) {
    return newInstance(value + summand);
  }

  @Override
  public T subtract(double subtrahend) {
    return newInstance(value - subtrahend);
  }

  @Override
  public T multiply(double factor) {
    return newInstance(value * factor);
  }

  @Override
  public T divide(double divisor) {
    return divide(divisor, false);
  }

  public T divide(double divisor, boolean re) {
    return divide(divisor, re ? value : 0);
  }

  public T divide(double divisor, double result) {
    return divisor == 0 ? newInstance(result) : newInstance(value / divisor);
  }

  @Override
  public T modulo(double rest) {
    return newInstance(value % rest);
  }

  @Override
  public T power(double exponent) {
    return newInstance(Math.pow(value, exponent));
  }

  @Override
  public void increment() {
    value++;
  }

  @Override
  public void decrement() {
    value--;
  }

}
