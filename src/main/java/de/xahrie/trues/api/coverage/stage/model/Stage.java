package de.xahrie.trues.api.coverage.stage.model;

import java.util.Arrays;
import java.util.Objects;

import de.xahrie.trues.api.coverage.ABetable;
import de.xahrie.trues.api.coverage.AEventable;
import de.xahrie.trues.api.coverage.match.model.AScheduleable;
import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Id;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table("coverage_stage")
public abstract class Stage implements ABetable, AEventable, Comparable<Stage>, AScheduleable, AStage, Id {
  @Setter
  protected int id;
  protected final int seasonId; // season
  protected TimeRange range;  // stage_start, stage_end
  protected Long discordEventId; // discord_event

  protected Season season;

  public Season getSeason() {
    if (season == null) this.season = new Query<>(Season.class).entity(seasonId);
    return season;
  }

  public Stage(Season season, TimeRange range) {
    this.season = season;
    this.seasonId = season.getId();
    this.range = range;
  }

  public Stage(int id, int seasonId, TimeRange range, Long discordEventId) {
    this.id = id;
    this.seasonId = seasonId;
    this.range = range;
    this.discordEventId = discordEventId;
  }

  public void setDiscordEventId(@NotNull Long discordEventId) {
    if (!discordEventId.equals(this.discordEventId)) new Query<>(Stage.class).col("discord_event", discordEventId).forId(id).update(id);
    this.discordEventId = discordEventId;
  }

  @Override
  public void setRange(TimeRange timeRange) {
    if (getRange().getStartTime() != range.getStartTime() || getRange().getEndTime() != timeRange.getEndTime()) {
      new Query<>(Stage.class).col("stage_start", timeRange.getStartTime()).col("stage_end", timeRange.getEndTime()).update(id);
    }
    this.range = timeRange;
  }

  @Override
  public int compareTo(@NotNull Stage o) {
    return getRange().compareTo(o.getRange());
  }

  public String type() {
    if (this instanceof SignupStage) return "Anmeldung";
    if (this instanceof CalibrationStage) return "Kalibrierungsphase";
    if (this instanceof CreationStage) return "Auslosung";
    if (this instanceof GroupStage) return "Gruppenphase";
    if (this instanceof PlayoffStage) return "Playoffs";
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final Stage stage)) return false;
    return getId() == stage.getId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  @RequiredArgsConstructor
  @Getter
  public enum StageType {
    SIGNUP_STAGE(SignupStage.class, 6, "Anmeldung", null),
    CALIBRATION_STAGE(CalibrationStage.class, 5, "Kalibrierungsphase", 506),
    CREATION_STAGE(CreationStage.class, 6, "Auslosung", null),
    GROUP_STAGE(GroupStage.class, 5, "Gruppenphase", 509),
    PLAYOFF_STAGE(PlayoffStage.class, 5, "Playoffs", 512);

    @Nullable
    public static StageType fromClass(Class<? extends Stage> clazz) {
      return Arrays.stream(StageType.values()).filter(stageType -> stageType.getEntityClass().equals(clazz)).findFirst().orElse(null);
    }

    @Nullable
    public static StageType fromName(String name) {
      return Arrays.stream(StageType.values()).filter(stageType -> stageType.getDisplayName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Nullable
    public static StageType fromPrmId(int prmId) {
      if (prmId < 1) return null;
      return Arrays.stream(StageType.values()).filter(stageType -> stageType.getPrmId() != null && stageType.getPrmId() == prmId).findFirst().orElse(null);
    }

    private final Class<? extends Stage> entityClass;
    @Getter(AccessLevel.NONE)
    private final int subTypeId;
    private final String displayName;
    private final Integer prmId;

    public StageType getSubType() {
      return subTypeId == -1 ? null : StageType.values()[subTypeId];
    }
  }
}
