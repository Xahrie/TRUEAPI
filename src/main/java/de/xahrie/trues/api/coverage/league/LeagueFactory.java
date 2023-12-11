package de.xahrie.trues.api.coverage.league;

import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.season.PRMSeason;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.util.exceptions.EntryMissingException;
import de.xahrie.trues.api.util.io.log.Level;
import org.jetbrains.annotations.NotNull;

public final class LeagueFactory {
  public static PRMLeague getGroup(@NotNull PRMSeason season, @NotNull String divisionName, int stageId, int divisionId) {
    final Stage stage = season.getStage(stageId);
    return new PRMLeague(divisionId, stage, divisionName).create();
  }
}
