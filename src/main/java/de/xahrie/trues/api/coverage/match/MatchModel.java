package de.xahrie.trues.api.coverage.match;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import de.xahrie.trues.api.coverage.match.model.PRMMatch;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.coverage.ModelBase;
import de.xahrie.trues.api.util.io.request.HTML;
import lombok.Getter;

/**
 * Created by Lara on 15.02.2023 for TRUEbot
 */
@Getter
public class MatchModel extends ModelBase implements Serializable {
  @Serial
  private static final long serialVersionUID = -6308081229468669808L;

  protected String url;
  protected PRMMatch match;
  protected List<HTML> logs;
  protected List<PRMTeam> teams;

  public MatchModel(HTML html, String url, PRMMatch match, List<HTML> logs, List<PRMTeam> teams) {
    super(html);
    this.url = url;
    this.match = match;
    this.logs = logs;
    this.teams = teams;
  }

}
