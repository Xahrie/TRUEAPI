package de.xahrie.trues.api.coverage.match.model;

import de.xahrie.trues.api.coverage.league.model.AbstractLeague;

public interface ATournament {
  AbstractLeague getLeague(); // league
  int getMatchIndex(); // match_index
  int getMatchId(); // match_id
}
