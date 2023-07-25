package de.xahrie.trues.api.discord.command.slash;

import lombok.Getter;
import lombok.Setter;

@Getter
public final class Column {
  @Setter
  private String name;
  private final Integer maxLength;
  private String delimiter = null;
  private boolean inline = true;
  private Integer round = null;
  private Align align = Align.AUTO;
  private boolean ignore = false;
  private boolean signed = false;

  public Column(String name, Integer maxLength) {
    this.name = name;
    this.maxLength = maxLength;
  }

  public Column(String name) {
    this(name, null);
  }

  public Column withPrevious() {
    return withPrevious(" - ");
  }

  public Column withPrevious(String delimiter) {
    this.delimiter = delimiter;
    return this;
  }

  public Column outline() {
    this.inline = false;
    return this;
  }

  public Column round() {
    return round(0);
  }

  public Column round(int digits) {
    this.round = digits;
    return this;
  }

  public Column alignLeft() {
    this.align = Align.LEFT;
    return this;
  }

  public Column alignRight() {
    this.align = Align.RIGHT;
    return this;
  }

  public Column ignore() {
    this.ignore = true;
    return this;
  }

  public Column sign() {
    this.signed = true;
    return this;
  }
}
