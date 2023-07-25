package de.xahrie.trues.api.community.orgateam.teamchannel;

import de.xahrie.trues.api.database.query.Query;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.Nullable;

public class TeamChannelRepository {
  @Nullable
  public static TeamChannel getTeamChannelFromChannel(@NonNull GuildChannel channel) {
    return getTeamChannelFromChannelId(channel.getIdLong());
  }

  @Nullable
  public static TeamChannel getTeamChannelFromChannelId(long channelId) {
    return new Query<>(TeamChannel.class).where("discord_id", channelId).entity();
  }

}
