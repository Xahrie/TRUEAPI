package de.xahrie.trues.api.database.connector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Listing {
  ListingType value() default ListingType.ORDINAL;
  int start() default 0;

  enum ListingType {
    ORDINAL,
    CAPITALIZE,
    LOWER,
    UPPER,
    CUSTOM
  }
}
