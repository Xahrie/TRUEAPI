package de.xahrie.trues.api.database.query;

import java.util.Arrays;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.extern.java.Log;

@Log
public class SQLEnum<E extends Enum<E>> {
  protected final Class<E> targetId;

  public SQLEnum(Class<E> targetId) {
    this.targetId = targetId;
  }

  public E of(Object source) {
    if (source == null) return null;
    if (source instanceof String sourceString) return toEnum(targetId, sourceString);
    else if (source instanceof Number sourceInt) return toEnum(targetId, (int) sourceInt);

    final RuntimeException exception = new IllegalStateException("Formatierung nicht zulässig");
    new DevInfo().severe(exception);
    throw exception;
  }

  private static <E extends Enum<E>> E toEnum(Class<E> enumClazz, int index) {
    final E[] enumConstants = enumClazz.getEnumConstants();
    if (index >= enumConstants.length) throw new ArrayIndexOutOfBoundsException("Der Wert ist zu groß.");
    return enumConstants[index];
  }

  private static <E extends Enum<E>> E toEnum(Class<E> enumClazz, String source) {
    final Listing.ListingType value = enumClazz.getAnnotation(Listing.class).value();
    return Arrays.stream(enumClazz.getEnumConstants())
        .filter(e -> (switch (value) {
          case CUSTOM -> e.toString();
          case LOWER -> e.name().toLowerCase();
          case UPPER -> e.name().toUpperCase();
          case CAPITALIZE -> StringUtils.capitalizeEnum(e.name().toLowerCase());
          case ORDINAL -> "" + e.ordinal();
        }).equals(source))
        .findFirst().orElseThrow(() -> new IllegalArgumentException("Der Wert kann nicht vergeben werden. (" + value + ")"));
  }

}
