package de.xahrie.trues.api.coverage.match;

import java.io.Serial;
import java.util.List;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.calendar.Cast;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Caster implements Entity<Caster> {
  @Serial
  private static final long serialVersionUID = -2415845953608913813L;
  @Setter
  private int id;
  private final int castId; // cast
  private Cast cast;

  public Cast getCast() {
    if (cast == null) this.cast = new Query<>(Cast.class).entity(castId);
    return cast;
  }

  private final CasterRole role; // cast_role
  private final int userId; // discord_user
  private DiscordUser user;

  public DiscordUser getUser() {
    if (user == null) this.user = new Query<>(DiscordUser.class).entity(userId);
    return user;
  }

  public Caster(Cast cast, CasterRole role, DiscordUser user) {
    this.cast = cast;
    this.castId = cast.getId();
    this.role = role;
    this.user = user;
    this.userId = user.getId();
  }

  public Caster(int id, int castId, CasterRole role, int userId) {
    this.id = id;
    this.castId = castId;
    this.role = role;
    this.userId = userId;
  }

  public static Caster get(List<Object> objects) {
    return new Caster(
        (int) objects.get(0),
        (int) objects.get(1),
        new SQLEnum<>(CasterRole.class).of(objects.get(2)),
        (int) objects.get(3)
    );
  }

  @Override
  public Caster create() {
    return new Query<>(Caster.class)
        .col("cast", castId)
        .col("caster_role", role)
        .col("discord_user", userId).insert(this);
  }


  @Listing(Listing.ListingType.LOWER)
  enum CasterRole {
    HOST,
    COLOR,
    SECONDARY,
    DRAFTER,
    INTERVIEWER_1,
    INTERVIEWER_2
  }
}
