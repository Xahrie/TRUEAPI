package de.xahrie.trues.api.coverage.stage.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;

@Table(value = "coverage_stage", department = "Anmeldung")
public class SignupStage extends Stage implements Entity<SignupStage>, WaitingStage {
  @Serial
  private static final long serialVersionUID = -4885691038267717445L;

  public SignupStage(Season season, TimeRange range) {
    super(season, range);
  }

  public SignupStage(int id, int seasonId, TimeRange range, Long discordEventId) {
    super(id, seasonId, range, discordEventId);
  }

  public static SignupStage get(List<Object> objects) {
    return new SignupStage(
        (int) objects.get(0),
        (int) objects.get(2),
        new TimeRange((LocalDateTime) objects.get(3), (LocalDateTime) objects.get(4)),
        (Long) objects.get(5)
    );
  }

  @Override
  public SignupStage create() {
    return new Query<>(SignupStage.class).key("season", seasonId)
        .col("stage_start", range.getStartTime()).col("stage_end", range.getEndTime()).col("discord_event", discordEventId)
        .insert(this);
  }
}
