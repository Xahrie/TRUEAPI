package de.xahrie.trues.api.discord.builder.embed;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.discord.builder.queryCustomizer.Enumeration;
import de.xahrie.trues.api.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

@RequiredArgsConstructor
@ExtensionMethod(StringUtils.class)
public class AbstractSimpleEmbedCreator {
  protected final Enumeration enumeration;
  protected final String title;
  protected String description = "";
  @Getter
  protected int index;
  protected Color color;
  protected final List<EmbedColumn> data = new ArrayList<>();

  public AbstractSimpleEmbedCreator(Enumeration enumeration, String title, String description, int index) {
    this.enumeration = enumeration;
    this.title = title;
    this.description = description;
    this.index = index;
  }

  public AbstractSimpleEmbedCreator(Enumeration enumeration, String title, String description, Color color, int index) {
    this.enumeration = enumeration;
    this.title = title;
    this.description = description;
    this.color = color;
    this.index = index;
  }

  public void add(String name, String value, boolean inline) {
    this.data.add(new EmbedColumn(name, value, inline));
  }
}
