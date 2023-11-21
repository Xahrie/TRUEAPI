package de.xahrie.trues.api.minecraft.beans;

import java.util.List;

import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import lombok.Getter;
import lombok.Setter;

@Table("mc_christmas_calendar")
@Getter
public class ChristmasCalendar implements Entity<ChristmasCalendar> {
  private static final long serialVersionUID = 0L;
  @Setter private int id;
  private final int userId;
  private final byte dayofDecember;
  private MinecraftUser user;

  public MinecraftUser getUser() {
    if (this.user == null)
      this.user = new Query<>(MinecraftUser.class).entity(userId);
    return this.user;
  }

  public ChristmasCalendar(MinecraftUser user, byte day) {
    this.userId = user.getId();
    this.user = user;
    this.dayofDecember = day;
  }

  private ChristmasCalendar(int id, short userId, byte dayofDecember) {
    this.id = id;
    this.userId = userId;
    this.dayofDecember = dayofDecember;
  }

  public static ChristmasCalendar get(List<Object> objects) {
    return new ChristmasCalendar(
        SQLUtils.intValue(objects.get(0)),
        SQLUtils.shortValue(objects.get(1)),
        SQLUtils.byteValue(objects.get(2))
    );
  }

  public ChristmasCalendar create() {
    return new Query<>(ChristmasCalendar.class)
        .col("minecraft_user", userId)
        .col("day_of_december", dayofDecember)
        .insert(this);
  }
}