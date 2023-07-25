package de.xahrie.trues.api.coverage.season;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.match.model.AScheduleable;
import de.xahrie.trues.api.coverage.stage.model.PlayStage;
import de.xahrie.trues.api.coverage.stage.model.SignupStage;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Id;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.coverage.ABetable;
import de.xahrie.trues.api.coverage.EventDTO;
import de.xahrie.trues.api.util.Util;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
@Table("coverage_season")
public abstract class Season implements ABetable, Id, AScheduleable, ASeason {
  @Setter
  protected int id;
  protected final String name; // season_name
  protected final String fullName; // season_full
  protected TimeRange range; // season_start, season_end
  protected final boolean active; // active

  @Override
  public void setRange(TimeRange range) {
    this.range = range;
  }

  @Override
  public List<Stage> getStages() {
    return new Query<>(Stage.class).where("season", this).entityList();
  }

  @Override
  @NonNull
  public Stage getStage(@NonNull Stage.StageType stageType) {
    return getStages().stream().filter(stage -> stageType.getEntityClass().isInstance(stage)).findFirst().orElseThrow();
  }

  @Override
  @Nullable
  public Stage getStage(int prmId) {
    final Stage.StageType stageType = Stage.StageType.fromPrmId(prmId);
    return Util.avoidNull(stageType, null, this::getStage);
  }

  @Override
  @NonNull
  public PlayStage getStageOfId(int id) {
    for (Stage stage : getStages()) {
      final var playStage = (PlayStage) stage;
      if ((playStage.pageId() == id)) return Util.nonNull(playStage);
    }
    final RuntimeException exception = new NullPointerException("Stage cannot be null");
    new DevInfo().severe(exception);
    throw exception;
  }

  @Override
  public String getSignupStatusForTeam(PRMTeam team) {
    if (team == null) return "kein Team gefunden";
    if (team.getSignupForSeason(this) != null) return "angemeldet";
    return getStages().stream().filter(stage -> stage instanceof SignupStage).findFirst()
                      .map(stage -> stage.getRange().hasStarted() ? "Anmeldung gestartet" : "Anmeldung " +
                                                                                            TimeFormat.DISCORD.of(stage.getRange().getStartTime()))
                      .orElse("keine Anmeldung eingerichtet");
  }

  @Override
  @NonNull
  public List<EventDTO> getEvents() {
    return new ArrayList<>(getStages().stream().map(stage -> new EventDTO(stage.getRange(), stage.type(), false)).toList()).stream().sorted().toList();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof final Season season)) return false;
    return this == o || getId() == season.getId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}
