package de.xahrie.trues.api.coverage.match.log;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.experimental.ExtensionMethod;
import lombok.extern.java.Log;
import org.jetbrains.annotations.Nullable;

@Log
@ExtensionMethod(StringUtils.class)
public final class LogFactory {
  @Nullable
  public static Participator handleUserWithTeam(Match match, String content) {
    content = content.between("(", ")", -1);
    if (content.equals("admin")) return null;

    final int teamIndex = content.replace("Team ", "").intValue();
    return switch (teamIndex) {
      case 1 -> match.getHome();
      case 2 -> match.getGuest();
      default -> {
        final PRMTeam prmTeam = new Query<>(PRMTeam.class).where("prm_id", teamIndex).entity();
        if (prmTeam == null) {
          final RuntimeException exception = new IllegalArgumentException("Matchlog fehlerhaft");
          new DevInfo(teamIndex + "").severe(exception);
          throw exception;
        }
        yield match.getParticipator(prmTeam);
      }
    };
  }
}
