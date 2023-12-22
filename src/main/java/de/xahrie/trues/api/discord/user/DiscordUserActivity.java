package de.xahrie.trues.api.discord.user;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.channel.DiscordChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table("discord_activity")
@ExtensionMethod(SQLUtils.class)
public class DiscordUserActivity implements Entity<DiscordUserActivity> {
  @Serial private static final long serialVersionUID = 675455029296764536L;
  @Setter private int id;
  private final LocalDateTime joined;
  private LocalDateTime left;

  private final int userId;
  private DiscordUser user;

  public DiscordUser getUser() {
    if (user == null)
      this.user = new Query<>(DiscordUser.class).entity(userId);
    return user;
  }

  private final Integer channelId;
  private DiscordChannel channel;

  public DiscordChannel getChannel() {
    if (channel == null)
      this.channel = new Query<>(DiscordChannel.class).entity(channelId);
    return channel;
  }

  public DiscordUserActivity(@NotNull DiscordUser user, @NotNull DiscordChannel channel) {
    this.user = user;
    this.userId = user.getId();
    this.channel = channel;
    this.channelId = channel.getId();
    this.joined = LocalDateTime.now();
  }

  private DiscordUserActivity(int id, int userId, @Nullable Integer channelId, @NotNull LocalDateTime joined,
                              @Nullable LocalDateTime left) {
    this.id = id;
    this.userId = userId;
    this.channelId = channelId;
    this.joined = joined;
    this.left = left;
  }

  @NotNull
  @Contract("_ -> new")
  public static DiscordUserActivity get(@NotNull List<Object> objects) {
    return new DiscordUserActivity(
        (int) objects.get(0),
        (int) objects.get(1),
        objects.get(2).intValue(),
        (LocalDateTime) objects.get(3),
        (LocalDateTime) objects.get(4)
    );
  }

  @Override
  public DiscordUserActivity create() {
    return new Query<>(DiscordUserActivity.class)
        .col("discord_user", userId).col("discord_channel", channelId).col("join_time", joined).col("left_time", left)
        .insert(this);
  }

  public boolean leave() {
    if (left != null) return false;

    boolean isInChannel =
        getChannel().getVoiceChannel().getMembers().stream().anyMatch(member -> member.getIdLong() == userId);
    if (isInChannel) return false;

    this.left = LocalDateTime.now();
    new Query<>(DiscordUserActivity.class).col("left_time", left).update(id);
    return true;
  }
}
