package de.xahrie.trues.api.database.connector;

import de.xahrie.trues.api.discord.command.slash.Column;
import de.xahrie.trues.api.util.StringUtils;

public record CellEntry(Object entry, Column column) {

  @Override
  public String toString() {
    return String.valueOf(this.entry);
  }

  public String round() {
    final String rawString = round(column.getRound());
    if (column.getMaxLength() == null) return rawString;
    final int length = column.getMaxLength() - (column.isSigned() ? 1 : 0);
    return StringUtils.keep(rawString, length);
  }

  private String round(Integer amount) {
    if (!(entry instanceof Number number) || amount == null) return this.toString();

    final double d = number.doubleValue();
    if (d != 0) return String.valueOf(Math.round(d * Math.pow(10, amount)) / Math.pow(10, amount));
    return this.toString();
  }

  public int getSignedLength() {
    final int length = round().length();
    return length + (isSigned() ? 1 : 0);
  }

  public boolean isSigned() {
    return (entry instanceof Number number && column.isSigned() && number.doubleValue() > 0);
  }
}
