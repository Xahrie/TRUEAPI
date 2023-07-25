package de.xahrie.trues.api.coverage.match.log;

import de.xahrie.trues.api.database.connector.Listing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Listing(Listing.ListingType.LOWER)
public enum MatchLogAction {
  CREATE(EventStatus.CREATED, false, "Spiel erstellt"),
  TEAM_ADDED(null, false, null),
  SCHEDULING_EXPIRED(EventStatus.CREATED, false, "Vorschlag ausgelaufen"),
  CHANGE_TIME(EventStatus.SCHEDULING_SUGGEST, false, "Matchzeit geändert"),
  SCHEDULING_SUGGEST(EventStatus.SCHEDULING_SUGGEST, false, "neuer Vorschlag"),
  SCHEDULING_AUTOCONFIRM(EventStatus.SCHEDULING_CONFIRM, false, "Termin bestätigt"),
  SCHEDULING_CONFIRM(EventStatus.SCHEDULING_CONFIRM, false, "Termin bestätigt"),
  CHANGE_STATUS(EventStatus.LINEUP_SUBMIT, true, "Status geändert"),
  LINEUP_SUBMIT(EventStatus.LINEUP_SUBMIT, false, "Lineup eingetragen"),
  LINEUP_PLAYER_READY(null, false, null),
  HOSTING_REQUEST(EventStatus.HOSTING_REQUEST, false, null),
  HOSTING_SUBMIT(EventStatus.HOSTING_REQUEST, false, null),
  SCORE_REPORT(EventStatus.SCORE_REPORT, false, "Ergebnis gemeldet"),
  REPORT(EventStatus.SCORE_REPORT, false, "Ergebnis gemeldet"),
  LINEUP_FAIL(EventStatus.PLAYED, false, "Lineup fehlerhaft"),
  LINEUP_NOTREADY(EventStatus.PLAYED, false, "Lineup fehlerhaft"),
  LINEUP_MISSING(EventStatus.PLAYED, false, null),
  PLAYED(EventStatus.PLAYED, false, "Spiel beendet"),
  DISQUALIFY(EventStatus.PLAYED, false, "Team disqualifiziert"),
  CHANGE_SCORE(null, false, "Ergebnis geändert");

  private final EventStatus status;
  private final boolean force;
  private final String output;
}
