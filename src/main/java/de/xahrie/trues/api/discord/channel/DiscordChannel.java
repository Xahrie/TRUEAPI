package de.xahrie.trues.api.discord.channel;

import java.io.Serial;
import java.util.List;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Getter
@Setter
@Table(value = "discord_channel", department = "other")
@Log
public class DiscordChannel extends AbstractDiscordChannel implements Entity<DiscordChannel> {
  @Serial
  private static final long serialVersionUID = -495599946883173951L;

  public DiscordChannel(long discordId, String name, ChannelType permissionType, DiscordChannelType channelType) {
    super(discordId, name, permissionType, channelType);
  }

  private DiscordChannel(int id, long discordId, DiscordChannelType channelType, String name, ChannelType permissionType) {
    super(id, discordId, channelType, name, permissionType);
  }

  public static DiscordChannel get(List<Object> objects) {
    return new DiscordChannel(
        (int) objects.get(0),
        (long) objects.get(2),
        new SQLEnum<>(DiscordChannelType.class).of(objects.get(3)),
        (String) objects.get(4),
        new SQLEnum<>(ChannelType.class).of(objects.get(5))
    );
  }

  @Override
  public DiscordChannel create() {
    return new Query<>(DiscordChannel.class).key("discord_id", discordId)
                                            .col("channel_type", channelType).col("channel_name", name).col("permission_type", permissionType)
                                            .insert(this);
  }
}
