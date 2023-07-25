package de.xahrie.trues.api.community.betting;

import java.io.Serial;
import java.util.List;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.user.DiscordUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Table("bet")
public final class Bet implements Entity<Bet> {
  @Serial
  private static final long serialVersionUID = -3029760281191504565L;

  @Setter
  private int id;
  private final int matchId, userId;

  private final String outcome; // bet_outcome
  private final int amount; // bet_amount
  private Integer difference; // bet_difference

  private Match match; // coverage

  public Match getMatch() {
    if (match == null) this.match = new Query<>(Match.class).entity(matchId);
    return match;
  }

  private DiscordUser user; // discord_user

  public DiscordUser getUser() {
    if (user == null) this.user = new Query<>(DiscordUser.class).entity(userId);
    return user;
  }

  public Bet(Match match, DiscordUser user, String outcome, int amount) {
    this.match = match;
    this.matchId = match.getId();
    this.user = user;
    this.userId = user.getId();
    this.outcome = outcome;
    this.amount = amount;
  }

  private Bet(int id, int matchId, int userId, String outcome, int amount, Integer difference) {
    this.id = id;
    this.matchId = matchId;
    this.userId = userId;
    this.outcome = outcome;
    this.amount = amount;
    this.difference = difference;
  }

  public void setDifference(int difference) {
    if (this.difference != difference) new Query<>(Bet.class).col("bet_difference", difference).update(id);
    this.difference = difference;
  }

  public static Bet get(List<Object> objects) {
    return new Bet(
        (int) objects.get(0),
        (int) objects.get(1),
        (int) objects.get(2),
        (String) objects.get(3),
        (int) objects.get(4),
        (int) objects.get(5)
    );
  }

  @Override
  public Bet create() {
    return new Query<>(Bet.class).key("coverage", matchId).key("discord_user", userId)
        .col("bet_outcome", outcome).col("bet_amount", amount).col("bet_difference", difference)
        .insert(this);
  }
}
