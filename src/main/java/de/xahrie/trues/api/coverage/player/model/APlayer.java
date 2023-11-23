package de.xahrie.trues.api.coverage.player.model;

import java.time.LocalDateTime;

import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.riot.api.RiotName;

public interface APlayer {
  String getPuuid();
  RiotName getName();
  void setSummonerName(RiotName name);
  DiscordUser getDiscordUser();
  void setDiscordUser(DiscordUser discordUser);
  AbstractTeam getTeam();
  void setTeam(AbstractTeam team);
  LocalDateTime getUpdated();
  void setUpdated(LocalDateTime updated);
  boolean isPlayed();
}
