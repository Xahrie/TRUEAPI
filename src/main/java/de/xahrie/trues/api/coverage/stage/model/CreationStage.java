package de.xahrie.trues.api.coverage.stage.model;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;

@Table(value = "coverage_stage", department = "Auslosung")
public class CreationStage extends Stage implements Entity<CreationStage>, WaitingStage {
  @Serial
  private static final long serialVersionUID = 4107910991901449582L;

  public CreationStage(Season season, TimeRange range) {
    super(season, range);
  }

  public CreationStage(int id, int seasonId, TimeRange range, Long discordEventId) {
    super(id, seasonId, range, discordEventId);
  }

  public static CreationStage get(List<Object> objects) {
    return new CreationStage(
        (int) objects.get(0),
        (int) objects.get(2),
        new TimeRange((LocalDateTime) objects.get(3), (LocalDateTime) objects.get(4)),
        (Long) objects.get(5)
    );
  }

  @Override
  public CreationStage create() {
    return new Query<>(CreationStage.class).key("season", seasonId)
        .col("stage_start", range.getStartTime()).col("stage_end", range.getEndTime()).col("discord_event", discordEventId)
        .insert(this);
  }
}
