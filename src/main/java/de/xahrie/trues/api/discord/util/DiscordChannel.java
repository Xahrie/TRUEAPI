package de.xahrie.trues.api.discord.util;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

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

  public TextChannel getTextChannel(DefinedTextChannel channel) {
    return willump.client.getTextChannelById(channel.getId());
  }

  public AudioChannel getVoiceChannel(Member member) {
    final GuildVoiceState voiceState = member.getVoiceState();
    return voiceState == null ? null : voiceState.getChannel();
  }

  public GuildChannel getChannel(long id) {
    return willump.guild.getGuildChannelById(id);
  }

  public void move(Member member, AudioChannel channel) {
    willump.guild.moveVoiceMember(member, channel).queue();
  }
}
