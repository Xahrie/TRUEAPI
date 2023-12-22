package de.xahrie.trues.api.discord.channel;

import de.xahrie.trues.api.database.connector.Database;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Id;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.util.Jinx;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@Table("discord_channel")
@Log
public abstract class AbstractDiscordChannel implements ADiscordChannel, Id {
  protected int id; // discord_channel_id
  protected final long discordId; // discord_id
  protected final DiscordChannelType channelType; // channel_type
  protected String name; // channel_name
  protected ChannelType permissionType; // permission_type

  public AbstractDiscordChannel(long discordId, String name, ChannelType permissionType, @NotNull DiscordChannelType channelType) {
    this.discordId = discordId;
    this.name = name;
    this.permissionType = permissionType;
    this.channelType = DiscordChannelType.valueOf(channelType.name());
  }

  @Override
  public void setId(int id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
    new Query<>(AbstractDiscordChannel.class).col("channel_name", name).update(id);
    Database.connection().commit();
  }

  public void setPermissionType(ChannelType permissionType) {
    this.permissionType = permissionType;
    new Query<>(AbstractDiscordChannel.class).col("permission_type", permissionType).update(id);
  }


  public AudioChannel getVoiceChannel() {
    return Jinx.instance.getChannels().getVoiceChannel(discordId);
  }
}
