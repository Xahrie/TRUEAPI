package de.xahrie.trues.api.discord.message;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.DateTimeUtils;
import de.xahrie.trues.api.discord.channel.AbstractDiscordChannel;
import de.xahrie.trues.api.discord.channel.DiscordChannel;
import de.xahrie.trues.api.discord.channel.DiscordChannelFactory;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.DefinedTextChannel;
import de.xahrie.trues.api.discord.util.Jinx;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table("discord_message")
@ExtensionMethod({SQLUtils.class, DateTimeUtils.class})
public class DiscordMessage implements Entity<DiscordMessage> {
  @Serial
  private static final long serialVersionUID = 675455029296764536L;
  private int id;
  private final Integer discordChannelId; // discord_channel
  private Long messageId; // discord_id
  private final Integer discordUserId; // discord_user
  private String content; // content
  private final int length; // content_length
  private LocalDateTime scheduled; // scheduled

  private AbstractDiscordChannel discordChannel;
  private DiscordUser discordUser;

  public AbstractDiscordChannel getDiscordChannel() {
    if (discordChannel == null) this.discordChannel = new Query<>(DiscordChannel.class).entity(discordChannelId);
    return discordChannel;
  }

  public DiscordUser getDiscordUser() {
    if (discordUser == null) this.discordUser = new Query<>(DiscordUser.class).entity(discordUserId);
    return discordUser;
  }

  @Nullable
  public String getContent() {
    if (content == null) {
      final TextChannel channel = Jinx.instance.getGuild().getTextChannelById(messageId);
      if (channel == null) return null;
      final Message message = channel.retrieveMessageById(messageId).complete();
      return message.getContentRaw();
    }
    return content;
  }

  public DiscordMessage(Message message, DiscordUser user) {
    this(message.getChannel().getIdLong(), message.getIdLong(), user, message.getContentRaw(), null);
  }

  public DiscordMessage(long channelId, @Nullable Long messageId, @NotNull DiscordUser discordUser, @NotNull String content,
                        @Nullable LocalDateTime scheduled) {
    this.discordChannel = DiscordChannelFactory.getDiscordChannel(Jinx.instance.getChannels().getChannel(channelId));
    this.discordChannelId = discordChannel.getId();
    this.messageId = messageId;
    this.discordUser = discordUser;
    this.discordUserId = discordUser.getId();
    this.content = scheduled == null ? null : content;
    this.length = content.length();
    this.scheduled = scheduled;
  }

  public DiscordMessage(int id, int discordChannelId, @Nullable Long messageId, @Nullable Integer discordUserId,
                        @Nullable String content, int length, @Nullable LocalDateTime scheduled) {
    this.id = id;
    this.discordChannelId = discordChannelId;
    this.messageId = messageId;
    this.discordUserId = discordUserId;
    this.content = content;
    this.length = length;
    this.scheduled = scheduled;
  }

  @Override
  public void setId(int id) {
    this.id = id;
  }

  private void setMessageId(long messageId) {
    this.messageId = messageId;
    new Query<>(DiscordMessage.class).col("discord_id", messageId).update(id);
  }

  private void resetScheduled() {
    this.scheduled = null;
    new Query<>(DiscordMessage.class).col("scheduled", null).update(id);
  }

  private void resetContent() {
    this.content = null;
    new Query<>(DiscordMessage.class).col("content", null).update(id);
  }

  @NotNull
  @Contract("_ -> new")
  public static DiscordMessage get(@NotNull List<Object> objects) {
    return new DiscordMessage(
        (int) objects.get(0),
        (int) objects.get(1),
        objects.get(2).longValue(),
        objects.get(3).intValue(),
        (String) objects.get(4),
        objects.get(5).intValue(),
        (LocalDateTime) objects.get(6)
    );
  }

  @Override
  public DiscordMessage create() {
    final AbstractDiscordChannel channel = getDiscordChannel();
    if (channel != null && channel.getDiscordId() == DefinedTextChannel.DEV_LOG.getId()) return null;

    return new Query<>(DiscordMessage.class)
        .col("discord_channel", discordChannelId)
        .col("discord_message", messageId)
        .col("discord_user", discordUserId)
        .col("content", content)
        .col("content_length", length)
        .col("scheduled", scheduled)
        .insert(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final DiscordMessage that)) return false;
    if (messageId != null)
      return Objects.equals(getMessageId(), that.getMessageId());

    return getDiscordChannelId().equals(that.getDiscordChannelId())
        && Objects.equals(getContent(), that.getContent());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMessageId());
  }

  public void send() {
    if (scheduled != null && LocalDateTime.now().isAfterEqual(scheduled)) {
      resetScheduled();
      final TextChannel channel = Jinx.instance.getGuild().getTextChannelById(getDiscordChannel().getDiscordId());
      if (channel == null)
        getDiscordUser().dm("Der hinterlegte Channel wurde inzwischen gelöscht!");
      else
        channel.sendMessage(content).queue(message -> setMessageId(message.getIdLong()));
      resetContent();
    }
  }
}
