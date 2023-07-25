package de.xahrie.trues.api.riot.match;

import java.util.Arrays;
import java.util.List;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.riot.performance.TeamPerf;
import de.xahrie.trues.api.riot.game.Game;
import de.xahrie.trues.api.riot.game.MatchUtils;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import no.stelar7.api.r4j.basic.constants.types.lol.GameQueueType;
import no.stelar7.api.r4j.basic.constants.types.lol.MapType;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import org.jetbrains.annotations.Nullable;

@Data
@RequiredArgsConstructor
@ExtensionMethod(MatchUtils.class)
public class RiotMatchAnalyzer {
  private final Player originatedPlayer;
  private final LOLMatch match;

  private boolean requiresSelection;
  private boolean hasNoSelections;

  @Nullable
  public Game analyze() {
    if (match.getParticipants().stream().filter(participant -> !participant.getPuuid().equals("BOT")).count() != 10) return null;
    if (!match.getMap().equals(MapType.SUMMONERS_RIFT)) return null;
    if (List.of(GameQueueType.BOT_5X5_INTRO, GameQueueType.BOT_5X5_BEGINNER, GameQueueType.BOT_5X5_INTERMEDIATE,
            GameQueueType.ONEFORALL_5X5, GameQueueType.URF)
        .contains(match.getQueue())) return null;

    final Game game = createGame();
    if (game == null) return null;

    final MatchSideAnalyzer blueSide = new MatchSideAnalyzer(originatedPlayer, match, game, Side.BLUE);
    final MatchSideAnalyzer redSide = new MatchSideAnalyzer(originatedPlayer, match, game, Side.RED);
    this.requiresSelection = requiresSelection(blueSide, redSide);
    this.hasNoSelections = game.hasSelections();
    final TeamPerf home = handleSide(blueSide);
    final TeamPerf guest = handleSide(redSide);
    if (match.getTournamentCode().isBlank()) return game;
    handleMatch(game, home, guest);
    return game;
  }

  public void handleMatch(Game game, TeamPerf home, TeamPerf guest) {
    assert home != null;
    assert guest != null;
    final Match m = findMatch(home, guest);
    if (m != null) game.setMatch(m);
  }

  private Match findMatch(@NonNull TeamPerf home, @NonNull TeamPerf guest) {
    if (home.getTeam() != null && guest.getTeam() != null) {
      final List<Match> matches = new Query<>(Participator.class).get("_participator.coverage")
                                                                 .join(new JoinQuery<>(Participator.class, Match.class).col("coverage"))
                                                                 .where("team", home.getTeamId()).and(
                      Condition.Comparer.NOT_EQUAL, "result", "0:0").descending("_match.coverage_start").convertList(Match.class);
      for (final Match match1 : matches) {
        if (match1.getParticipator(home.getTeam()) == null) continue;
        if (match1.getParticipator(guest.getTeam()) == null) continue;
        return match1;
      }
    }
    return null;
  }

  private boolean requiresSelection(MatchSideAnalyzer... sides) {
    final GameQueueType queue = match.getQueue();
    if (queue.equals(GameQueueType.CUSTOM)) return true;

    final int max = Arrays.stream(sides).map(side -> side.getValidParticipants().size()).max(Integer::compareTo).orElse(0);
    return max > 2 || (queue.equals(GameQueueType.CLASH) && max > 0);
  }

  private TeamPerf handleSide(MatchSideAnalyzer analyzer) {
    if (requiresSelection) analyzer.analyzeSelections();
    if (analyzer.getValidParticipants().isEmpty()) return null;

    return analyzer.analyze();
  }

  @Nullable
  private Game createGame() {
    final int durationInSeconds = (int) match.getGameDurationAsDuration().getSeconds();
    if (durationInSeconds < 300) return null;

    return new Game(match.getMatchId(), match.getCreation(), durationInSeconds, match.getGameQueue()).create();
  }
}
