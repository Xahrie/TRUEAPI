package de.xahrie.trues.api.discord.ticket;

import java.io.Serial;
import java.util.List;

import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.discord.channel.ChannelType;
import de.xahrie.trues.api.discord.channel.DiscordChannel;
import de.xahrie.trues.api.discord.channel.DiscordChannelType;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.Const;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table("ticket")
@ExtensionMethod(SQLUtils.class)
public class Ticket implements Entity<Ticket> {
  @Serial
  private static final long serialVersionUID = -236212712288035758L;

  @Setter
  private int id;
  private final int creatorId;
  private final Topic topic;
  private final String title;
  private int channelId;
  private DiscordUser creator;

  public DiscordUser getCreator() {
    if (creator == null) this.creator = new Query<>(DiscordUser.class).entity(channelId);
    return creator;
  }

  private DiscordChannel channel;

  public DiscordChannel getChannel() {
    if (channel == null) this.channel = new Query<>(DiscordChannel.class).entity(channelId);
    return channel;
  }

  public Ticket(DiscordUser creator, Topic topic, String title) {
    this.creator = creator;
    this.creatorId = creator.getId();
    this.topic = topic;
    this.title = title;
  }

  private Ticket(int id, int creatorId, Topic topic, String title, int channelId) {
    this.id = id;
    this.creatorId = creatorId;
    this.topic = topic;
    this.title = title;
    this.channelId = channelId;
  }

  public void setChannel(DiscordChannel channel) {
    this.channel = channel;
    this.channelId = channel.getId();
  }

  public static Ticket get(List<Object> objects) {
    return new Ticket(
        (int) objects.get(0),
        (int) objects.get(1),
        new SQLEnum<>(Topic.class).of(objects.get(2)),
        (String) objects.get(3),
        (int) objects.get(4)
    );
  }

  @Override
  public Ticket create() {
    final Integer o = (Integer) new Query<>(Ticket.class).get("count(*)", Integer.class).single()[0];
    this.id = o+1;
    final TextChannel textChannel = Jinx.instance.getGuild().createTextChannel(id + ": " + title,
        Jinx.instance.getGuild().getCategoryById(Const.Channels.ADMIN_INTERN)).complete();
    applyPermissions(textChannel);
    final var discordChannel = new DiscordChannel(textChannel.getIdLong(), textChannel.getName(),
                                                  ChannelType.SUPPORT_TICKET, DiscordChannelType.TEXT).create();
    setChannel(discordChannel);
    textChannel.sendMessage("Dein Support Ticket wurde erstellt! Du kannst dein Anliegen mit 'end' schließen, was zur Löschung des Channels führt. Anfragen, die länger als eine Woche nicht beantwortet werden und beantwortet sind, werden wir manuell schließen. Alle Nachrichten werden aufgezeichnet, sodass du dir den Verlauf auch später noch anschauen kannst.").queue();
    return new Query<>(Ticket.class)
        .col("topic", topic).col("title", title).col("discordchannel", channelId)
        .insert(this);
  }

  private void applyPermissions(TextChannel channel) {
    final DiscordGroup allowed = topic.getVisibility();
    channel.getManager().putRolePermissionOverride(allowed.getDiscordId(), List.of(Permission.VIEW_CHANNEL), List.of()).queue();
    channel.getManager().putMemberPermissionOverride(getCreator().getDiscordId(), List.of(Permission.VIEW_CHANNEL), List.of()).queue();
  }
}
