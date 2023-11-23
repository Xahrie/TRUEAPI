package de.xahrie.trues.api.coverage.match.log;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.match.model.PRMMatch;
import de.xahrie.trues.api.coverage.participator.model.Lineup;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.PrimePlayerFactory;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.riot.api.RiotName;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table("coverage_log")
@ExtensionMethod({StringUtils.class, SQLUtils.class})
public class MatchLog implements Entity<MatchLog>, Comparable<MatchLog> {
  @Serial private static final long serialVersionUID = 6671472810762353290L;

  @Setter private int id;
  private final LocalDateTime timestamp; // log_time
  private final int matchId; // coverage
  private final MatchLogAction action; // action
  private final String details; // details
  private final Integer participatorId; // coverage_team

  public MatchLog(@NotNull
  Match match, @NotNull MatchLogAction action, @NotNull String details, @Nullable
  Participator participator) {
    this(LocalDateTime.now(), match, action, details, participator);
  }

  public MatchLog(@NotNull LocalDateTime timestamp, @NotNull Match match, @NotNull MatchLogAction action, @NotNull String details,
                  @Nullable Participator participator) {
    this.timestamp = timestamp;
    this.match = match;
    this.matchId = match.getId();
    this.action = action;
    this.details = details;
    this.participator = participator;
    this.participatorId = Util.avoidNull(participator, Participator::getId);
  }

  private MatchLog(int id, LocalDateTime timestamp, int matchId, MatchLogAction action, String details, Integer participatorId) {
    this.id = id;
    this.timestamp = timestamp;
    this.matchId = matchId;
    this.action = action;
    this.details = details;
    this.participatorId = participatorId;
  }

  public static MatchLog get(List<Object> objects) {
    return new MatchLog(
        (int) objects.get(0),
        (LocalDateTime) objects.get(1),
        (int) objects.get(2),
        new SQLEnum<>(MatchLogAction.class).of(objects.get(3)),
        (String) objects.get(4),
        objects.get(5).intValue()
    );
  }

  @Override
  public MatchLog create() {
    return new Query<>(MatchLog.class)
        .key("log_time", timestamp).key("coverage", matchId).key("action", action).key("details", details)
        .key("coverage_team", participatorId)
        .insert(this, getMatch()::addLog);
  }

  @Override
  public int compareTo(@NotNull MatchLog o) {
    return Comparator.comparing(MatchLog::getTimestamp).compare(this, o);
  }

  public String detailsOutput() {
    if (details.countMatches("+0100") + details.countMatches("+0200") > 0) {
      return Arrays.stream(details.split(":00 \\+0100"))
          .flatMap(s -> Arrays.stream(s.split(":00 \\+0200")))
          .collect(Collectors.joining("\n"));
    }
    if (action.equals(MatchLogAction.LINEUP_SUBMIT)) {
      final String players = getParticipator().getTeamLineup().getFixedLineups().stream()
          .map(Lineup::getPlayer).map(Player::getName).map(RiotName::toString)
          .collect(Collectors.joining(", "));
      if (isMostRecentLogOfType()) {
        final String linkPlayers = players.replace(", ", ",").replace(" ", "%20")
            .replace("#", "%23");
        return players + "[Op.gg](https://euw.op.gg/multisearch/euw?summoners=" + linkPlayers + ") - [Poro](" + linkPlayers + "/season)";
      }
      return players;
    }
    return details;
  }

  public String actionOutput() {
    return TimeFormat.DISCORD.of(timestamp) + " - " + action.getOutput() + "\n".repeat(extralinesRequired());
  }

  public String teamOutput() {
    return getParticipator().getAbbreviation() + "\n".repeat(extralinesRequired());
  }

  private int extralinesRequired() {
    return Math.max(details.countMatches("+0100") + details.countMatches("+0200"), 1) - 1;
  }

  private boolean isMostRecentLogOfType() {
    final MatchLog entity = new Query<>(MatchLog.class)
        .where("coverage", matchId).and("action", MatchLogAction.LINEUP_SUBMIT).and("coverage_team", getParticipator())
        .descending("log_time").entity();
    return entity == null || !entity.getTimestamp().isAfter(timestamp);
  }

  public List<Player> determineLineup() {
    return Arrays.stream(getDetails().split(", "))
        .map(playerString -> playerString.before(":").intValue())
        .map(playerId -> getMatch() instanceof PRMMatch ? PrimePlayerFactory.getPlayer(playerId) : new Query<>(Player.class).entity(playerId))
        .collect(Collectors.toList());
  }

  private Match match; // coverage

  public Match getMatch() {
    if (match == null) this.match = new Query<>(Match.class).entity(matchId);
    return match;
  }

  private Participator participator; // coverage_team

  @NotNull
  public Participator getParticipator() {
    if (participator == null)
      this.participator = Util.avoidNull(new Query<>(Participator.class).entity(participatorId), Participator.ADMIN(match));
    return participator;
  }
}
