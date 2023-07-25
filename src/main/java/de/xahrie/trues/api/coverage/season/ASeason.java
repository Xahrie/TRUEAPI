package de.xahrie.trues.api.coverage.season;

import java.util.List;

import de.xahrie.trues.api.coverage.stage.model.PlayStage;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.coverage.EventDTO;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ASeason extends Comparable<ASeason> {
  List<Stage> getStages();

  @NonNull Stage getStage(@NonNull Stage.StageType stageType);

  @Nullable
  Stage getStage(int prmId);

  @NonNull PlayStage getStageOfId(int id);

  String getSignupStatusForTeam(PRMTeam team);

  @NonNull List<EventDTO> getEvents();

  String getName();

  String getFullName();

  TimeRange getRange();

  boolean isActive();

  @Override
  default int compareTo(@NotNull ASeason o) {
    return getRange().compareTo(o.getRange());
  }
}
