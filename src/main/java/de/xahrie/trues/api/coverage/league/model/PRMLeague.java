package de.xahrie.trues.api.coverage.league.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.coverage.playday.scheduler.PlaydayScheduler;
import de.xahrie.trues.api.coverage.playday.Playday;
import de.xahrie.trues.api.coverage.season.PRMSeason;
import de.xahrie.trues.api.coverage.stage.model.PlayStage;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.util.io.request.URLType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Table(value = "coverage_group", department = "prime")
public class PRMLeague extends AbstractLeague implements Entity<PRMLeague> {
  @Serial
  private static final long serialVersionUID = -6947551713641103275L;

  private int prmId; // prm_id

  public PRMLeague(int prmId, @NotNull Stage stage, @NotNull String name) {
    super(stage, name);
    this.prmId = prmId;
  }

  private PRMLeague(int id, int stageId, @NotNull String name, int prmId) {
    super(id, stageId, name);
    this.prmId = prmId;
  }

  @NotNull
  @Contract("_ -> new")
  public static PRMLeague get(@NotNull List<Object> objects) {
    return new PRMLeague(
        (int) objects.get(0),
        (int) objects.get(2),
        (String) objects.get(3),
        (int) objects.get(4)
    );
  }

  @Override
  public PRMLeague create() {
    return new Query<>(PRMLeague.class).key("stage", stageId).key("group_name", name)
        .col("prm_id", prmId)
        .insert(this);
  }


  public LocalDateTime getAlternative(@NotNull Playday playday) {
    final PlaydayScheduler scheduler = PlaydayScheduler.create(getStage(), playday.getId(), getTier());
    return scheduler.defaultTime();
  }

  public boolean isStarter() {
    return getName().equals(Const.Gamesports.STARTER_NAME) || getName().contains(Const.Gamesports.CALIBRATION_NAME) || getName().contains(Const.Gamesports.PLAYOFF_NAME);
  }

  public String getUrl() {
    return String.format(URLType.LEAGUE.getUrlName(), ((PRMSeason) getStage().getSeason()).getPrmId(), ((PlayStage) getStage()).pageId(), prmId);
  }
}
