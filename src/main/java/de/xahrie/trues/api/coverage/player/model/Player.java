package de.xahrie.trues.api.coverage.player.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Id;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.riot.Zeri;
import de.xahrie.trues.api.riot.game.GameType;
import de.xahrie.trues.api.riot.performance.PerformanceFactory;
import de.xahrie.trues.api.scouting.PlayerAnalyzer;
import de.xahrie.trues.api.scouting.ScoutingGameType;
import de.xahrie.trues.api.scouting.analyze.RiotPlayerAnalyzer;
import de.xahrie.trues.api.util.Util;
import lombok.Getter;
import lombok.Setter;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Table(value = "player")
public abstract class Player implements Comparable<Player>, Id, APlayer {
  @Setter
  protected int id; // player_id
  protected String puuid; // lol_puuid
  protected String summonerId; // lol_summoner
  protected String summonerName; // lol_name
  protected Integer discordUserId; // discord_user
  protected Integer teamId; // team
  protected LocalDateTime updated; // updated
  protected boolean played; // played

  protected DiscordUser discordUser;

  public DiscordUser getDiscordUser() {
    if (discordUser == null) this.discordUser = new Query<>(DiscordUser.class).entity(discordUserId);
    return discordUser;
  }

  public void setDiscordUser(@Nullable DiscordUser discordUser) {
    if (Objects.equals(this.discordUser, discordUser)) return;
    this.discordUser = discordUser;
    this.discordUserId = Util.avoidNull(discordUser, DiscordUser::getId);
    new Query<>(Player.class).col("discord_user", discordUserId).update(id);
    loadGames(LoaderGameType.CLASH_PLUS);
  }

  public void setPlayed(boolean played) {
    if (this.played != played) new Query<>(Player.class).col("played", played).update(id);
    this.played = played;
  }

  protected AbstractTeam team;

  public AbstractTeam getTeam() {
    if (team == null) this.team = new Query<>(AbstractTeam.class).entity(teamId);
    return team;
  }

  public void setTeam(AbstractTeam team) {
    if (getTeam() == team) return;
    if (teamId != null && teamId == team.getId()) return;

    this.team = team;
    this.teamId = Util.avoidNull(team, AbstractTeam::getId);
    if (team != null) {
      team.getPlayers().add(this);
      if (team instanceof PRMTeam prmTeam && prmTeam.getCurrentLeague() != null && prmTeam.getCurrentLeague().getLeague().isOrgaLeague()) {
        loadGames(LoaderGameType.CLASH_PLUS);
      }
    }
    new Query<>(Player.class).col("team", team).update(id);
  }

  public String getSummonerId() {
    if (summonerId == null) {
      final String summonerId = Util.avoidNull(Zeri.get().getSummonerAPI().getSummonerByPUUID(LeagueShard.EUW1, puuid), Summoner::getSummonerId);
      if (summonerId != null) {
        this.summonerId = summonerId;
        new Query<>(Player.class).col("lol_summoner", summonerId).update(id);
      }
    }
    return summonerId;
  }

  public Player(String summonerName, String puuid, String summonerId) {
    this.summonerName = summonerName;
    this.puuid = puuid;
    this.summonerId = summonerId;
    final Player playerFound = new Query<>(Player.class).where("lol_puuid", puuid).entity();
    if (playerFound != null) {
      this.updated = playerFound.getUpdated();
      this.played = playerFound.isPlayed();
      this.discordUserId = playerFound.getDiscordUserId();
      this.teamId = playerFound.getTeamId();
    } else {
      this.updated = LocalDateTime.now().minusYears(1);
      this.played = false;
    }
  }

  protected Player(int id, String puuid, String summonerId, String summonerName, Integer discordUserId, Integer teamId, LocalDateTime updated, boolean played) {
    this.id = id;
    this.puuid = puuid;
    this.summonerId = summonerId;
    this.summonerName = summonerName;
    this.discordUserId = discordUserId;
    this.teamId = teamId;
    this.updated = updated;
    this.played = played;
  }

  public void setPuuidAndName(String puuid, String summonerId, String name) {
    this.puuid = puuid;
    this.summonerId = summonerId;
    this.summonerName = name;
    new Query<>(Player.class).col("lol_puuid", puuid).col("lol_summoner", puuid).col("lol_name", name).update(id);
  }

  public void setSummonerName(String summonerName) {
    if (!summonerName.equals(this.summonerName)) new Query<>(Player.class).col("lol_name", summonerName).update(id);
    this.summonerName = summonerName;
  }

  public void setUpdated(LocalDateTime updated) {
    this.updated = updated;
    new Query<>(Player.class).col("updated", updated).update(id);
  }

  private PlayerRankHandler ranks;

  public PlayerRankHandler getRanks() {
    if (ranks == null) this.ranks = new PlayerRankHandler(this);
    return ranks;
  }

  private PlayerAnalyzer analyzer;

  public PlayerAnalyzer analyze(ScoutingGameType type, int days) {
    if (type.equals(ScoutingGameType.MATCHMADE) && days == 180) {
      if (analyzer == null) analyzer = new PlayerAnalyzer(this, type, days);
      return analyzer;
    }
    return new PlayerAnalyzer(this, type, days);
  }

  public void loadGames(LoaderGameType gameType) {
    new RiotPlayerAnalyzer(this).analyzeGames(gameType, false);
  }

  public void loadChampionMastery() {
    new RiotPlayerAnalyzer(this).analyzeMastery();
  }

  public void forceLoadMatchmade() {
    new RiotPlayerAnalyzer(this).analyzeGames(LoaderGameType.MATCHMADE, true);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Player && this.id == ((Player) obj).id;
  }


  @Override
  public int compareTo(@NotNull Player o) {
    return Integer.compare(getId(), o.getId());
  }

  @Override
  public String toString() {
    return summonerName + " | " + getRanks().getCurrent();
  }

  public List<Object[]> getLastGames(GameType gameType) {
    return PerformanceFactory.getLastPlayerGames(gameType, this);
  }
}
