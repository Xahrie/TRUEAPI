package de.xahrie.trues.api.coverage.player.model;

import java.time.LocalDateTime;

import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.discord.user.DiscordUser;

public interface APlayer {
  String getPuuid();
  String getSummonerName();
  void setSummonerName(String summonerName);
  DiscordUser getDiscordUser();
  void setDiscordUser(DiscordUser discordUser);
  AbstractTeam getTeam();
  void setTeam(AbstractTeam team);
  LocalDateTime getUpdated();
  void setUpdated(LocalDateTime updated);
  boolean isPlayed();
}
