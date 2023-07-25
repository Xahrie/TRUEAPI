package de.xahrie.trues.api.coverage.stage.model;

import java.util.List;

import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.coverage.stage.IdAble;
import de.xahrie.trues.api.database.query.Id;
import de.xahrie.trues.api.database.query.Query;

public interface PlayStage extends IdAble, Playable, Id {
  default List<AbstractLeague> leagues() {
    return new Query<>(AbstractLeague.class).where("stage", this).entityList();
  }
}
