package de.xahrie.trues.api.scouting.scouting.teaminfo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.xahrie.trues.api.calendar.Calendar;
import de.xahrie.trues.api.calendar.event.SeasonEventCalendar;
import de.xahrie.trues.api.calendar.scheduling.TeamTrainingScheduleHandler;
import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannel;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelType;
import de.xahrie.trues.api.coverage.league.model.AbstractLeague;
import de.xahrie.trues.api.coverage.league.model.PRMLeague;
import de.xahrie.trues.api.coverage.match.MatchResult;
import de.xahrie.trues.api.coverage.match.log.MatchLogBuilder;
import de.xahrie.trues.api.coverage.match.model.LeagueMatch;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.match.model.PRMMatch;
import de.xahrie.trues.api.coverage.participator.TeamLineup;
import de.xahrie.trues.api.coverage.participator.model.Lineup;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.playday.Playday;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.player.model.PlayerRank;
import de.xahrie.trues.api.coverage.player.model.PlayerRankHandler;
import de.xahrie.trues.api.coverage.player.model.Rank;
import de.xahrie.trues.api.coverage.season.OrgaCupSeason;
import de.xahrie.trues.api.coverage.season.PRMSeason;
import de.xahrie.trues.api.coverage.season.SeasonFactory;
import de.xahrie.trues.api.coverage.stage.model.GroupStage;
import de.xahrie.trues.api.coverage.stage.model.PlayoffStage;
import de.xahrie.trues.api.coverage.stage.model.Stage;
import de.xahrie.trues.api.coverage.team.leagueteam.LeagueTeam;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.coverage.team.model.Standing;
import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.discord.builder.embed.EmbedFieldBuilder;
import de.xahrie.trues.api.util.Util;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

@AllArgsConstructor
@Getter
public class TeamInfo {
  private final OrgaTeam orgaTeam;
  private Message message;
  @Setter
  private LocalDateTime lastUpdate;

  public TeamInfo(OrgaTeam orgaTeam) {
    this(orgaTeam, findOrCreate(orgaTeam), LocalDateTime.now().minusDays(2));
  }

  private static Message findOrCreate(OrgaTeam orgaTeam) {
    final TeamChannel teamChannel = orgaTeam.getChannels().get(TeamChannelType.INFO, false);
    if (teamChannel == null) return null;
    final MessageChannel messageChannel = (MessageChannel) teamChannel.getChannel();
    return MessageHistory.getHistoryFromBeginning(messageChannel).complete().getRetrievedHistory().stream()
        .filter(message -> message.getAuthor().isBot())
        .filter(message -> !message.getEmbeds().isEmpty()).findFirst().orElse(null);
  }

  public void updateAll() {
    TeamInfoManager.addTeam(orgaTeam);
  }

  void create() {
    final TeamChannel teamChannel = orgaTeam.getChannels().get(TeamChannelType.INFO, false);
    if (teamChannel == null) return;

    final MessageChannel messageChannel = (MessageChannel) teamChannel.getChannel();
    messageChannel.sendMessageEmbeds(getList()).queue(message1 -> this.message = message1);
  }

  List<MessageEmbed> getList() {
    return List.of(getOverview(), getScheduling(), getNextMatch(), getDivision(), getInternCup());
  }

  private MessageEmbed getDivision() {
    final PRMSeason currentSeason = SeasonFactory.getCurrentPRMSeason();
    final AbstractTeam team = orgaTeam.getTeam();
    if (!(team instanceof PRMTeam prmTeam)) {
      return new EmbedBuilder().setTitle("keine Division").setDescription("Das Team ist nicht auf Prime League registriert.").build();
    }

    final PRMLeague lastLeague = prmTeam.getLastLeague();
    if (lastLeague == null) {
      return new EmbedBuilder().setTitle("keine Division").setDescription("Das Team hat nie Prime League gespielt.").build();
    }

    final String signupStatus = Util.avoidNull(currentSeason, "", season -> " - " + season.getSignupStatusForTeam((PRMTeam) orgaTeam.getTeam()));
    final var builder = new EmbedBuilder()
        .setTitle(lastLeague.getStage().getSeason().getName() + " - " + lastLeague.getName() + signupStatus, lastLeague.getUrl());
    final String descriptionPrefix = lastLeague.getStage().getSeason().equals(currentSeason) ? "aktuelle" : "letzte";
    builder.setDescription(descriptionPrefix + " Gruppe im Prime League Split");

    final Map<Playday, List<LeagueMatch>> playdayMatches = new HashMap<>();
    final Map<AbstractTeam,MatchResult> results = new LinkedHashMap<>();
    final Map<AbstractTeam, MatchResult> realresults = new LinkedHashMap<>();
    for (LeagueMatch match : lastLeague.getMatches()) {
      if (!playdayMatches.containsKey(match.getPlayday())) playdayMatches.put(match.getPlayday(), new ArrayList<>());
      playdayMatches.get(match.getPlayday()).add(match);
      for (final Participator participator : match.getParticipators()) {
        final AbstractTeam participatingTeam = participator.getTeam();
        if (participatingTeam == null) continue;

        final MatchResult resultHandler = results.containsKey(participatingTeam) ? results.get(participatingTeam) :
            new MatchResult(match, 0, 0);
        final MatchResult realresultHandler = realresults.containsKey(participatingTeam) ? realresults.get(participatingTeam) :
            new MatchResult(match, 0, 0);
        final MatchResult resultHandler2 = match.getExpectedResult().ofTeam(participatingTeam);
        final MatchResult resultHandler3 = match.getResult().ofTeam(participatingTeam);
        if (resultHandler2 != null) results.put(participatingTeam, resultHandler.add(resultHandler2));
        if (resultHandler3 != null) realresults.put(participatingTeam, realresultHandler.add(resultHandler3));
      }
    }
    final List<Map.Entry<AbstractTeam, MatchResult>> r = results.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
    builder.addField("Teamname", r.stream().map(entry -> entry.getKey().getName()).collect(Collectors.joining("\n")), true);
    builder.addField("Standing", r.stream().map(entry -> realresults.get(entry.getKey()).toString()).collect(Collectors.joining("\n")), true);
    builder.addField("Prognose", r.stream().map(entry -> entry.getValue().toString()).collect(Collectors.joining("\n")), true);

    playdayMatches.keySet().stream().sorted().forEach(playday -> new EmbedFieldBuilder<>(playdayMatches.get(playday).stream().sorted().toList())
        .add("Spielwoche " + playday.getIdx(), match -> TimeFormat.WEEKLY.of(match.getStart()))
        .add("Standing", match -> match.getHomeAbbr() + " vs " + match.getGuestAbbr())
        .add("Prognose", Match::getExpectedResultString)
        .build().forEach(builder::addField));
    final int correct = (int) lastLeague.getMatches().stream().map(match -> match.getResult().wasAcurate()).filter(Objects::nonNull)
        .filter(bool -> bool).count();
    final int incorrect = (int) lastLeague.getMatches().stream().map(match -> match.getResult().wasAcurate()).filter(Objects::nonNull)
        .filter(bool -> !bool).count();
    builder.addField("Fehlerrate", new Standing(correct, incorrect).getWinrate().toString(), false);
    return builder.build();
  }

  private MessageEmbed getInternCup() {
    final OrgaCupSeason lastSeason = SeasonFactory.getLastInternSeason();
    final OrgaCupSeason currentSeason = SeasonFactory.getCurrentInternSeason();
    final EmbedBuilder builder = new EmbedBuilder()
        .setTitle(currentSeason == null ? "keine Season" : (currentSeason.getFullName() + " - TRUE-Cup - " + currentSeason.getSignupStatusForTeam((PRMTeam) orgaTeam.getTeam())))
        .setDescription("Aktueller Spielplan im TRUE-Cup")
        .setFooter("zuletzt aktualisiert " + TimeFormat.DEFAULT.now())
        .addField("Kurzregeln", /* OrgaCupSeason.RULES + */"Stand-in Slots verbleibend: " + orgaTeam.getStandins(), false);

    if (currentSeason == null) return builder
        .addField("Zeitpunkt", "keine Daten", false).addField("Cup-Phase", "keine Daten", true)
        .build();

    new EmbedFieldBuilder<>(currentSeason.getEvents())
        .add("Zeitpunkt", eventDTO -> eventDTO.getString(0))
        .add("Cup-Phase", eventDTO -> eventDTO.getString(1))
        .build().forEach(builder::addField);

    if (lastSeason == null) return builder.build();

    final GroupStage groupStage = (GroupStage) lastSeason.getStage(Stage.StageType.GROUP_STAGE);
    for (AbstractLeague league : groupStage.leagues()) {
      final List<LeagueTeam> signups = league.getSignups();
      new EmbedFieldBuilder<>(signups.stream().sorted().toList())
          .add(league.getName(), l -> l.getTeam().getName())
          .add("Standing", l -> l.getScore().toString())
          .add("Prognose", l -> l.getExpectedScore().toString()).build().forEach(builder::addField);
    }
    getFieldsForStage(groupStage, "Gruppenspiele").forEach(builder::addField);

    final PlayoffStage playoffStage = (PlayoffStage) lastSeason.getStage(Stage.StageType.PLAYOFF_STAGE);
    getFieldsForStage(playoffStage, "Endrunde").forEach(builder::addField);

    final List<Match> games = orgaTeam.getTeam() == null ? List.of() : orgaTeam.getTeam().getMatches().getMatchesOf(lastSeason).stream().filter(Match::isRunning).toList();
    getFieldsOfGames("kommende Spiele", games);

    return builder.build();
  }

  private List<MessageEmbed.Field> getFieldsForStage(Stage stage, String name) {
    final List<Match> games = orgaTeam.getTeam() == null ? List.of() : orgaTeam.getTeam().getMatches().getMatchesOf(stage);
    return getFieldsOfGames(name, games);
  }

  private List<MessageEmbed.Field> getFieldsOfGames(String name, List<Match> games) {
    if (games.isEmpty()) {
      return List.of(new MessageEmbed.Field("Gruppenspiele", "keine Spiele verfügbar", false));
    }
    return new EmbedFieldBuilder<>(games)
        .add(name, match -> TimeFormat.WEEKLY.of(match.getStart()))
        .add("Standing", match -> match.getHomeAbbr() + " vs " + match.getGuestAbbr())
        .add("Prognose", Match::getExpectedResultString).build();
  }

  private MessageEmbed getNextMatch() {
    final Match nextMatch = orgaTeam.getTeam() == null ? null : orgaTeam.getTeam().getMatches().getNextMatch(true);
    final String matchType;
    if (nextMatch == null) {
      matchType = "kein Match";
    } else {
      final AbstractTeam opponentOf = nextMatch.getOpponentOf(orgaTeam.getTeam());
      matchType = (nextMatch.getTypeString() + " gegen " + (opponentOf == null ? "kein Gegner" : opponentOf.getName()));
    }
    final String url = nextMatch instanceof PRMMatch primeMatch ? primeMatch.get().getURL() : null;
    final EmbedBuilder builder = new EmbedBuilder()
        .setTitle("Nächstes Match: " + matchType, url)
        .setDescription(nextMatch == null ? "kein Match" : TimeFormat.DISCORD.of(nextMatch.getStart()))
        .setFooter("zuletzt aktualisiert " + TimeFormat.DEFAULT.now());
    if (nextMatch != null) {
      determineMatchLineupFields(nextMatch, nextMatch.getOpponentOf(orgaTeam.getTeam())).forEach(builder::addField);
      determineMatchLineupFields(nextMatch, orgaTeam.getTeam()).forEach(builder::addField);
      builder.addField("Matchlog:", "hier findet ihr den vollständigen Matchlog", false);
      new MatchLogBuilder(nextMatch, orgaTeam.getTeam()).getFields().forEach(builder::addField);
    }
    return builder.build();
  }

  private List<MessageEmbed.Field> determineMatchLineupFields(Match match, AbstractTeam team) {
    final Participator participator = match.getParticipator(team);
    final TeamLineup lineup = participator.getTeamLineup();
    final List<Player> players = lineup.getFixedLineups().stream().map(Lineup::getPlayer).toList();
    return List.of(
        new MessageEmbed.Field(team.getFullName(), "Team", false),
        new MessageEmbed.Field("Lineup", players.stream().map(Player::getSummonerName).collect(Collectors.joining("\n")), true),
        new MessageEmbed.Field("Elo (" + lineup.getAverageRank().toString() + ")", players.stream().map(Player::getRanks)
                                                                                          .map(PlayerRankHandler::getCurrent).map(
                        PlayerRank::toString).collect(Collectors.joining("\n")), true)
    );
  }

  private MessageEmbed getOverview() {
    String recordAndSeasons = "";
    String standingPRM = "keine PRM-Teilnahme";
    final String standingTRUE = orgaTeam.getPlace() == null ? "keine TRUE-Cup Teilnahme" : ("TRUE-Rating: Platz" + orgaTeam.getPlace());
    if (orgaTeam.getTeam() instanceof PRMTeam prmTeam) {
      recordAndSeasons = " - " + prmTeam.getRecord().toString();
      final LeagueTeam leagueTeam = prmTeam.getCurrentLeague();
      if (leagueTeam != null) {
        standingPRM = leagueTeam.toString();
      }
    }
    final EmbedBuilder builder = new EmbedBuilder()
        .setTitle(orgaTeam.getName() + " (" + orgaTeam.getAbbreviation() + recordAndSeasons + ")")
        .setDescription(standingPRM + " || " + standingTRUE)
        .setFooter("zuletzt aktualisiert " + TimeFormat.DEFAULT.now());
    final double averageMMR = orgaTeam.getMainMemberships().stream().map(membership -> membership.getUser().getPlayer())
        .filter(Objects::nonNull).mapToInt(player -> player.getRanks().getLastRelevant().getRank().getMMR()).average().orElse(0);
    final Rank teamRank = PlayerRank.fromMMR((int) averageMMR);

    new EmbedFieldBuilder<>(orgaTeam.getActiveMemberships().stream().sorted().toList())
        .add("Position", Membership::getPositionString)
        .add("Spieler (og.gg)", membership -> membership.getUser().getNickname())
        .add("Elo (" + teamRank + ")", membership -> membership.getUser().getPlayer() != null ? membership.getUser().getPlayer().getRanks().getCurrent().getRank().toString() : "nicht registriert")
        .build().forEach(builder::addField);

    new EmbedFieldBuilder<>(orgaTeam.getScheduler().getCalendarEntries())
        .add("nächste Events", calendar -> calendar.getString(0))
        .add("Art", calendar -> calendar.getString(1))
        .add("Information", calendar -> calendar.getString(2))
        .build().forEach(builder::addField);

    new EmbedFieldBuilder<>(new TeamTrainingScheduleHandler(orgaTeam).getTeamAvailabilitySince(LocalDate.now()))
        .add("Trainingstage", TimeRange::display)
        .add("Dauer", TimeRange::duration)
        .add("geplant", timeRange -> timeRange.trainingReserved(orgaTeam))
        .build().forEach(builder::addField);

    final PRMSeason upcomingPRMSeason = SeasonFactory.getUpcomingPRMSeason();
    if (upcomingPRMSeason != null) {
      new EmbedFieldBuilder<>(upcomingPRMSeason.getEvents())
          .add("Zeitpunkt", eventDTO -> eventDTO.getString(0))
          .add("PRM-Phase", eventDTO -> eventDTO.getString(1))
          .build().forEach(builder::addField);
    }

    final List<SeasonEventCalendar> events = new Query<>(SeasonEventCalendar.class)
            .where(Condition.Comparer.GREATER_EQUAL, "calendar_end", LocalDateTime.now().plusDays(1)).entityList();
    if (!events.isEmpty()) {
      new EmbedFieldBuilder<>(events.subList(0, Math.min(10, events.size())))
          .add("Zeitpunkt", matchCalendar -> matchCalendar.getRange().displayRange())
          .add("Event", Calendar::getDetails)
          .build().forEach(builder::addField);
    }
    return builder.build();
  }

  public MessageEmbed getScheduling() {
    final EmbedBuilder builder = new EmbedBuilder()
        .setTitle("Terminplanung")
        .setDescription("Terminplanung für " + orgaTeam.getName())
        .setFooter("zuletzt aktualisiert " + TimeFormat.DEFAULT.now());

    final DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
    for (int i = 0; i < 7; i++) {


      final List<List<String>> lists = new TeamTrainingScheduleHandler(orgaTeam).ofDay(LocalDate.now().plusDays(i));
      if (lists.isEmpty()) continue;

      new EmbedFieldBuilder<>(lists)
          .outline(dayOfWeek.plus(i).getDisplayName(TextStyle.FULL, Locale.GERMANY) + ", den " +
              TimeFormat.DAY_LONG.of(LocalDate.now().plusDays(i)),  "coming soon")
          .add("Position", list -> list.get(0))
          .add("Zeiten oder Ersatz", list -> list.get(1))
          .build().forEach(builder::addField);
    }
    for (int i = 1; i < 3; i++) {
      new EmbedFieldBuilder<>(new TeamTrainingScheduleHandler(orgaTeam).ofWeekStarting(LocalDate.now().plusDays(7 * i)))
          .outline("Folgewoche " + i, "Terminfindung für diese Woche")
          .add("Position", list -> list.get(0))
          .add("Zeiten oder Ersatz", list -> list.get(1))
          .build().forEach(builder::addField);
    }
    if (builder.getFields().isEmpty()) builder.setDescription("keine Termine festgelegt.");
    return builder.build();
  }
}
