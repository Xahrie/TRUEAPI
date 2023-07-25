package de.xahrie.trues.api.discord.ticket;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;

@Getter
@Table("ticket_message")
@ExtensionMethod(SQLUtils.class)
public class TicketMessage implements Entity<TicketMessage> {
  @Serial
  private static final long serialVersionUID = 5114783779061644300L;

  @Setter
  private int id;
  private final int ticketId;
  private final LocalDateTime timestamp;
  private final int userId;
  private final String content;
  private Ticket ticket;

  public Ticket getTicket() {
    if (ticket == null) this.ticket = new Query<>(Ticket.class).entity(ticketId);
    return ticket;
  }

  private DiscordUser user;

  public DiscordUser getUser() {
    if (user == null) this.user = new Query<>(DiscordUser.class).entity(userId);
    return user;
  }

  public TicketMessage(Ticket ticket, LocalDateTime timestamp, DiscordUser user, String content) {
    this.ticketId = ticket.getId();
    this.ticket = ticket;
    this.timestamp = timestamp;
    this.userId = user.getId();
    this.user = user;
    this.content = content;
  }

  public TicketMessage(int id, int ticketId, LocalDateTime timestamp, int userId, String content) {
    this.id = id;
    this.ticketId = ticketId;
    this.timestamp = timestamp;
    this.userId = userId;
    this.content = content;
  }

  public static TicketMessage get(List<Object> objects) {
    return new TicketMessage(
        (int) objects.get(0),
        (int) objects.get(1),
        (LocalDateTime) objects.get(2),
        (int) objects.get(3),
        (String) objects.get(4)
    );
  }

  @Override
  public TicketMessage create() {
    return new Query<>(TicketMessage.class)
        .col("ticket", ticketId).col("timestamp", timestamp).col("discord_user", userId).col("message", content)
        .insert(this);
  }
}
