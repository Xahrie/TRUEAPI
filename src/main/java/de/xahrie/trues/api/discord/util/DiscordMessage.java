package de.xahrie.trues.api.discord.util;

import de.xahrie.trues.api.util.io.log.DevInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;

public record DiscordMessage(Willump willump, TextChannel channel) {
  @Nullable
  public Message getMessage(long messageId) {
    try {
      return channel.retrieveMessageById(messageId).complete();
    } catch (RuntimeException exception) {
      new DevInfo("Message nicht gefunden").warn(exception);
    }
    return null;
  }

  @Nullable
  public Message getLastMessage() {
    try {
      return channel.retrieveMessageById(channel.getLatestMessageIdLong()).complete();
    } catch (RuntimeException exception) {
      new DevInfo("Message nicht gefunden").warn(exception);
    }
    return null;
  }
}
