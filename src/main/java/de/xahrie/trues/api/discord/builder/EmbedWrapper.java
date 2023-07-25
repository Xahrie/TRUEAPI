package de.xahrie.trues.api.discord.builder;

import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.util.Const;
import lombok.Data;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Data
public class EmbedWrapper {
  private final List<MessageEmbed> embeds = new ArrayList<>();
  private final List<List<String>> content = new ArrayList<>();

  /**
   * Füge {@link MessageEmbed} zur Gesamtnachricht hinzu
   * @param embed Liste von Einbettungen innerhalb einer Nachricht
   * @return instance für Chaining
   */
  public EmbedWrapper embed(List<MessageEmbed> embed) {
    this.embeds.addAll(embed);
    return this;
  }

  /**
   * Füge {@link String} zur Gesamtnachricht hinzu
   * @param content Liste von Texten innerhalb einer Nachricht
   * @return instance für Chaining
   */
  public EmbedWrapper content(List<String> content) {
    this.content.add(content);
    return this;
  }

  /**
   * Verlinke Contents miteinander
   * @return Messages, die auszugeben sind
   */
  public List<String> merge() {
    if (this.content.isEmpty()) return List.of("");

    final List<String> data = new ArrayList<>();
    StringBuilder out = new StringBuilder();
    for (List<String> texts : this.content) {
      for (String text : texts) {
        if (out.length() + text.length() > Const.DISCORD_MESSAGE_MAX_CHARACTERS) {
          data.add(out.toString());
          out = new StringBuilder(text);
        } else {
          out.append(text);
        }
      }
      out.append("\n\n");
    }
    data.add(out.append("```zuletzt aktualisiert ").append(TimeFormat.AUTO.now()).toString());
    return data;
  }
}
