package de.xahrie.trues.api.calendar;

import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table("calendar")
public abstract class AbstractUserCalendar extends Calendar {
  protected int userId; // discord_user

  public AbstractUserCalendar(TimeRange timeRange, String details, DiscordUser user) {
    super(timeRange, details);
    this.user = user;
    this.userId = user.getId();
  }

  protected AbstractUserCalendar(int id, TimeRange range, String details, int userId) {
    super(id, range, details);
    this.userId = userId;
  }

  protected DiscordUser user;

  public DiscordUser getUser() {
    if (user == null) this.user = new Query<>(DiscordUser.class).entity(userId);
    return user;
  }
}