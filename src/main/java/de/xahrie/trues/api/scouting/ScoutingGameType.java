package de.xahrie.trues.api.scouting;

import java.util.Arrays;
import java.util.List;

import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

@RequiredArgsConstructor
@Getter
public enum ScoutingGameType {
  PRM_ONLY("nur Prime League"),
  PRM_CLASH("PRM & Clash"),
  TEAM_GAMES("Team Games"),
  MATCHMADE("alle Games");
  private final String displayName;

  @Override
  public String toString() {
    return displayName;
  }

  public SelectOption toSelectOption() {
    return SelectOption.of(displayName, name());
  }

  public static ActionRow ACTION_ROW() {
    final List<SelectOption> selectOptions = Arrays.stream(ScoutingGameType.values()).map(ScoutingGameType::toSelectOption).toList();
    return ActionRow.of(StringSelectMenu.create("scouting-game-type").setPlaceholder("Gametyp").addOptions(selectOptions)
        .setRequiredRange(0, 1).build());
  }

  public TeamAnalyzer teamQuery(AbstractTeam team, int days) {
    return team.analyze(this, days);
  }
}
