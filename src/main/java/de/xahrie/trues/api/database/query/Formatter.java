package de.xahrie.trues.api.database.query;

public record Formatter(String columnName, CellFormat format, int maxLength) {
  public static Formatter of(String columnName, CellFormat format, int maxLength) {
    return new Formatter(columnName, format, maxLength);
  }
  public static Formatter of(String columnName, CellFormat format) {
    return new Formatter(columnName, format, 32);
  }

  public static Formatter of(String columnName, int maxLength) {
    return new Formatter(columnName, CellFormat.OTHER, maxLength);
  }

  public static Formatter of(String columnName) {
    return new Formatter(columnName, CellFormat.OTHER, 32);
  }

  public String toString(String alias) {
    final String columnName = (columnName().contains(".") || columnName().startsWith("IF")) ? columnName() : alias + "." + columnName();
    return switch (format) {
      default -> "SUBSTRING(" + columnName + ", 1, " + maxLength + ")";
      case NUMBER -> "TRIM(LEADING '0' FROM IF(LENGTH(ROUND(" + columnName + ")) > " + maxLength + ", IF(LENGTH(ROUND(" + columnName + ")) - 3 > " + maxLength + ", CONCAT(TRIM(TRAILING '.' FROM SUBSTRING(ROUND(" + columnName + " / 1000000, " + maxLength + "), 1, " + maxLength + ")), 'M'), CONCAT(TRIM(TRAILING '.' FROM SUBSTRING(ROUND(" + columnName + " / 1000, " + maxLength + "), 1, " + maxLength + ")), 'k')), ROUND(" + columnName + ")))";
      case DECIMAL -> "TRIM(LEADING '0' FROM IF(LENGTH(ROUND(" + columnName + ")) > " + maxLength + ", IF(LENGTH(ROUND(" + columnName + ")) - 3 > " + maxLength + ", CONCAT(TRIM(TRAILING '.' FROM SUBSTRING(ROUND(" + columnName + " / 1000000, " + maxLength + "), 1, " + maxLength + ")), 'M'), CONCAT(TRIM(TRAILING '.' FROM SUBSTRING(ROUND(" + columnName + " / 1000, " + maxLength + "), 1, " + maxLength + ")), 'k')), ROUND(" + columnName + ", " + maxLength + " - LENGTH(ROUND(" + columnName + ")))))";
      case TIME -> "DATE_FORMAT(" + columnName + ", '%H:%i')";
      case DATE -> "IF(YEAR(" + columnName + ") = YEAR(NOW()), DATE_FORMAT(" + columnName + ", '%d %b'), DATE_FORMAT(" + columnName + ", '%d %b %Y'))";
      case WEEKDAY -> "DATE_FORMAT(" + columnName + ", '%" + (maxLength >= 10 ? "W" : "a.") + "')";
      case DATETIME -> "IF(YEAR(" + columnName + ") = YEAR(NOW()), DATE_FORMAT(" + columnName + ", '%d %b %H:%i'), DATE_FORMAT(" + columnName + ", '%d %b %Y %H:%i'))";
      case WEEKTIME -> "DATE_FORMAT(" + columnName + ", '%a, %H:%i')";
      case WEEKDAYTIME -> "IF(YEAR(" + columnName + ") = YEAR(NOW()), DATE_FORMAT(" + columnName + ", '%a, %d %b %H:%i'), DATE_FORMAT(" + columnName + ", '%a, %d %b %Y %H:%i'))";
      case DEFAULT -> "CONCAT('<t:', ROUND(UNIX_TIMESTAMP(" + columnName + ")), ':R>')";
      case AUTO -> "IF(TIMESTAMPDIFF(MINUTE, " + columnName + ", NOW()) < 45, CONCAT('<t:', ROUND(UNIX_TIMESTAMP(" + columnName + ")), ':R>'), IF(YEAR(" + columnName + ") = YEAR(NOW()) AND DAYOFYEAR(" + columnName + ") = DAYOFYEAR(NOW()), DATE_FORMAT(" + columnName + ", '%H:%i'), IF(TIMESTAMPDIFF(HOUR, " + columnName + ", NOW()) < 24, CONCAT('<t:', ROUND(UNIX_TIMESTAMP(" + columnName + ")), ':R>'), IF(TIMESTAMPDIFF(DAY, " + columnName + ", NOW()) < 7, DATE_FORMAT(" + columnName + ", '%a, %H:%i'), IF(TIMESTAMPDIFF(DAY, " + columnName + ", NOW()) < 25, CONCAT('<t:', ROUND(UNIX_TIMESTAMP(" + columnName + ")), ':R>'), DATE_FORMAT(" + columnName + ", '%a, %d %b %H:%i'))))))";
    };
  }

  public enum CellFormat {
    OTHER,
    NUMBER,
    DECIMAL,
    TIME,
    DATE,
    WEEKDAY,
    DATETIME,
    WEEKTIME,
    WEEKDAYTIME,
    DEFAULT,
    AUTO
  }
}
