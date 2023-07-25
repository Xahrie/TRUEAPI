package de.xahrie.trues.api.coverage.team.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.model.Lineup;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.model.PlayerRank;
import de.xahrie.trues.api.coverage.player.model.PlayerRankHandler;
import de.xahrie.trues.api.coverage.player.model.Rank;
import de.xahrie.trues.api.coverage.season.signup.SeasonSignup;
import de.xahrie.trues.api.coverage.team.leagueteam.LeagueTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.coverage.season.Season;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Id;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLField;
import de.xahrie.trues.api.scouting.ScoutingGameType;
import de.xahrie.trues.api.scouting.TeamAnalyzer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString(exclude = {"orgaTeam", "players", "signups", "participators"})
@Table("team")
public abstract class AbstractTeam implements ATeam, Id, Comparable<AbstractTeam> {
  @Setter protected int id;
  protected String name; // team_name
  protected String abbreviation; // team_abbr
  protected LocalDateTime refresh; // refresh
  protected boolean highlight = false; // highlight
  protected Integer lastMMR; // last_team_mmr

  public Integer getLastMMR() {
    if (lastMMR == null) {
      final double averageMMR = getPlayers().stream().map(Player::getRanks).map(PlayerRankHandler::getLastRelevant).map(
              PlayerRank::getRank).mapToInt(Rank::getMMR).average().orElse(0);
      setLastMMR((int) Math.round(averageMMR));
    }
    return lastMMR;
  }

  public AbstractTeam(String name, String abbreviation) {
    this.name = name;
    this.abbreviation = abbreviation;
  }

  protected AbstractTeam(int id, String name, String abbreviation, LocalDateTime refresh, boolean highlight, Integer lastMMR) {
    this.id = id;
    this.name = name;
    this.abbreviation = abbreviation;
    this.refresh = refresh;
    this.highlight = highlight;
    this.lastMMR = lastMMR;
  }

  public void setName(String name) {
    if (orgaTeam != null && !orgaTeam.getNameCreation().equals(name)) {
      orgaTeam.setNameCreation(name);
    }
    this.name = name;
    new Query<>(AbstractTeam.class).col("team_name", name).update(id);
  }

  public void setAbbreviation(String abbreviation) {
    if (orgaTeam != null && !orgaTeam.getAbbreviation().equals(abbreviation)) {
      orgaTeam.setAbbreviationCreation(abbreviation);
    }
    this.abbreviation = abbreviation;
    new Query<>(AbstractTeam.class).col("team_abbr", abbreviation).update(id);
  }

  @Override
  public void setRefresh(LocalDateTime refresh) {
    final LocalDateTime refreshUntil = orgaTeam == null ? refresh.plusDays(70) : LocalDateTime.MAX;
    if (this.refresh == null || refreshUntil.isAfter(this.refresh)) {
      this.refresh = refreshUntil;
      new Query<>(AbstractTeam.class).col("refresh", refreshUntil).update(id);
    }
  }

  public void setHighlight(boolean highlight) {
    this.highlight = highlight;
    new Query<>(AbstractTeam.class).col("highlight", highlight).update(id);
  }

  @Override
  public void setLastMMR(Integer lastMMR) {
    this.lastMMR = lastMMR;
    new Query<>(AbstractTeam.class).col("last_team_mmr", lastMMR).update(id);
  }

  public boolean highlight() {
    setHighlight(!highlight);
    return highlight;
  }

  public String getFullName() {
    return name + " (" + abbreviation + ")";
  }

  public Match nextOrLastMatch() {
    final List<Match> matches = new Query<>(
            Participator.class).field(SQLField.get("_coverage.coverage_id", Integer.class))
                               .join(new JoinQuery<>(Participator.class, Match.class).col("coverage"))
                               .where("team", this).and("_coverage.active", true)
                               .ascending("_coverage.start")
                               .convertList(Match.class).stream().toList();
    return matches.stream().filter(match -> match.getStart().isAfter(LocalDateTime.now())).findFirst().orElse(matches.get(matches.size() - 1));
  }

  public MatchManager getMatches() {
    return new MatchManager(this);
  }

  @Nullable
  public SeasonSignup getSignupForSeason(Season season) {
    return new Query<>(SeasonSignup.class).where("team", this).and("season", season).entity();
  }

  @Override
  public int compareTo(@NotNull AbstractTeam o) {
    if (this instanceof PRMTeam prmTeam && o instanceof PRMTeam oPRM) return Integer.compare(prmTeam.getPrmId(), oPRM.getPrmId());
    return Integer.compare(getId(), o.getId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final AbstractTeam team)) return false;
    return getName().equals(team.getName()) && getAbbreviation().equals(team.getAbbreviation());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getAbbreviation());
  }

  /**
   * Bestimme das wahrscheinlichste Lineup für ein Team
   */
  public List<Lineup> determineLineup() {
    final var participator = new Participator(null, false, this);
    return participator.getTeamLineup().getLineup();
  }

  public TeamAnalyzer analyze(ScoutingGameType gameType, int days) {
    return new TeamAnalyzer(this, gameType, days);
  }

  private final Map<AbstractLeague,LeagueTeam> storedLeagueEntries = new HashMap<>();

  public LeagueTeam getLeagueEntry(AbstractLeague league) {
    return storedLeagueEntries.computeIfAbsent(league, league1 ->
        new Query<>(LeagueTeam.class).where("league", league1).and("team", id).entity());
  }

  @Setter private OrgaTeam orgaTeam;

  public OrgaTeam getOrgaTeam() {
    if (orgaTeam == null) this.orgaTeam = new Query<>(OrgaTeam.class).where("team", this).entity();
    return orgaTeam;
  }

  private List<Participator> participators;

  @Override
  public List<Participator> getParticipators() {
    if (participators == null) participators = new Query<>(Participator.class).where("team", this).entityList();
    return participators;
  }

  private List<SeasonSignup> signups;

  @Override
  public List<SeasonSignup> getSignups() {
    if (signups == null) signups = new Query<>(SeasonSignup.class).where("team", this).entityList();
    return signups;
  }

  private List<Player> players;

  @Override
  public List<Player> getPlayers() {
    if (players == null) players = new Query<>(Player.class).where("team", this).entityList();
    return players;
  }
}
