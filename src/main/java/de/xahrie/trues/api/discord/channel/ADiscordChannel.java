package de.xahrie.trues.api.discord.channel;

import java.util.List;

import de.xahrie.trues.api.discord.util.Jinx;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;

public interface ADiscordChannel {
  long getDiscordId();

  DiscordChannelType getChannelType();

  String getName();

  void setName(String name);

  ChannelType getPermissionType();

  void setPermissionType(ChannelType permissionType);

  default void updatePermissions() {
    final List<ChannelPermissionType.APermissionOverride> permissions = getPermissionType().get(getChannelType()).getPermissions(null);
    if (permissions == null) return;

    for (ChannelPermissionType.APermissionOverride permission : permissions)
      getChannel().getManager().putPermissionOverride(permission.holder(), permission.allowed(), permission.denied()).queue();
  }

  default IPermissionContainer getChannel() {
    return (IPermissionContainer) Jinx.instance.getChannels().getChannel(getDiscordId());
  }
}
