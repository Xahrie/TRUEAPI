package de.xahrie.trues.api.util.io.log;

import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.util.StringUtils;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@NoArgsConstructor
public class DevInfo extends AbstractLog<DevInfo> {
  private static final TextChannel LOGGING_CHANNEL = Jinx.instance.getClient().getTextChannelById(
          Const.Channels.DEV_LOGGING_CHANNEL);

  public DevInfo(String message) {
    super(message);
  }

  public DevInfo doCommand(DiscordUser user, String command, String full) {
    this.level = Level.COMMAND;
    this.message = user.getNickname() + " -> " + command + full;
    if (LOGGING_CHANNEL == null) throw new NullPointerException("Dev-Log Channel wurde gelöscht");
    LOGGING_CHANNEL.sendMessage(StringUtils.keep(toString(), 2000)).queue();
    return this;
  }

  @Override
  protected DevInfo doLog() {
    if (Level.DISCORD_LOG.contains(level)) {
      if (LOGGING_CHANNEL == null) throw new NullPointerException("Dev-Log Channel wurde gelöscht");
      LOGGING_CHANNEL.sendMessage(StringUtils.keep(toString(), 2000)).queue();
    }
    return this;
  }
}
