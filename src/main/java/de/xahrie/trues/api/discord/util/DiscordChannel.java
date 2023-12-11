package de.xahrie.trues.api.discord.util;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;

public class DiscordChannel {
  private final Willump willump;

  public DiscordChannel(Willump willump) {
    this.willump = willump;
  }

  public TextChannel getAdminChannel() {
    return getTextChannel(DefinedTextChannel.ADMIN_CHANNEL);
  }

  public TextChannel getBewerberChannel() {
    return getTextChannel(DefinedTextChannel.BEWERBER);
  }

  public TextChannel getTextChannel(@NotNull DefinedTextChannel channel) {
    return getTextChannel(channel.getId());
  }

  public TextChannel getTextChannel(long channelId) {
    return willump.client.getTextChannelById(channelId);
  }

  public AudioChannel getVoiceChannel(@NotNull Member member) {
    final GuildVoiceState voiceState = member.getVoiceState();
    return voiceState == null ? null : voiceState.getChannel();
  }

  public GuildChannel getChannel(long id) {
    return willump.guild.getGuildChannelById(id);
  }

  public void move(Member member, AudioChannel channel) {
    willump.guild.moveVoiceMember(member, channel).queue();
  }

  public DiscordMessage getMessages(@NotNull DefinedTextChannel channel) {
    return new DiscordMessage(willump, getTextChannel(channel.getId()));
  }

  public DiscordMessage getMessages(long channelId) {
    return new DiscordMessage(willump, getTextChannel(channelId));
  }
}
