package de.xahrie.trues.api.minecraft;

import de.xahrie.trues.api.minecraft.beans.MinecraftUser;
import de.xahrie.trues.api.minecraft.beans.MinecraftUserFactory;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

@Data
public abstract class SubScoreboard {
  private final String name;
  private final DisplaySlot displaySlot;
  private final boolean playerUnique;

  public void update() {
    final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    Objective scoreboardObjective = scoreboard.registerNewObjective("scoreboard", "dummy", name);
    scoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

    if (playerUnique) {
      for (MinecraftUser user : Bukkit.getOnlinePlayers().stream().map(MinecraftUserFactory::findUser).toList()) {
        final List<String> data = getData(user);
        for (int i = 0; i <data.size(); i++) {
          final String outputString = data.get(i);
          final Score score = scoreboardObjective.getScore(outputString);
          score.setScore(data.size() - i);
        }
        final Player player = user.getPlayer().getPlayer();
        if (player != null) {
          player.setScoreboard(scoreboard);
        }
      }
      return;
    }
    final List<String> data = getData(null);
    for (int i = 0; i < data.size(); i++) {
      final String outputString = data.get(i);
      final Score score = scoreboardObjective.getScore(outputString);
      score.setScore(data.size() - i);
    }
    for (MinecraftUser user : Bukkit.getOnlinePlayers().stream().map(MinecraftUserFactory::findUser).toList()) {
      final Player player = user.getPlayer().getPlayer();
      if (player != null) {
        player.setScoreboard(scoreboard);
      }
    }
  }

  protected abstract List<String> getData(MinecraftUser minecraftUser);

}
