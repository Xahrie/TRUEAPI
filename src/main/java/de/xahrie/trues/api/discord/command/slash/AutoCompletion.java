package de.xahrie.trues.api.discord.command.slash;

import java.util.List;

import de.xahrie.trues.api.database.query.Query;
import net.dv8tion.jda.api.interactions.commands.Command;

public record AutoCompletion (String optionName, String query) {
  public List<Command.Choice> getData() {
    return getObjects().stream().map(o -> (String) o[0]).map(o -> new Command.Choice(o, o)).toList();
  }

  public List<Object[]> getObjects() {
    return new Query<>(query).list(25);
  }
}
