package de.xahrie.trues.api.coverage.team;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import de.xahrie.trues.api.coverage.player.model.PRMPlayer;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.coverage.ModelBase;
import de.xahrie.trues.api.util.io.request.HTML;
import lombok.Getter;

@Getter
public class TeamModel extends ModelBase implements Serializable {
  @Serial
  private static final long serialVersionUID = -5471189659485625565L;

  protected String url;
  protected PRMTeam team;
  protected List<PRMPlayer> players;

  public TeamModel(HTML html, String url, PRMTeam team, List<PRMPlayer> players) {
    super(html);
    this.url = url;
    this.team = team;
    this.players = players;
  }
}
