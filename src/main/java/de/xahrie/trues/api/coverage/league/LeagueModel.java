package de.xahrie.trues.api.coverage.league;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LeagueModel implements Serializable {
  @Serial
  private static final long serialVersionUID = 2667181184120392512L;

  protected String url;
  protected PRMLeague league;
  protected List<PRMTeam> teams;
  protected List<LeaguePlayday> playdays;

}
