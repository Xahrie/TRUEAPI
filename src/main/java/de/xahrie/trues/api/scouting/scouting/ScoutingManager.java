package de.xahrie.trues.api.scouting.scouting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.participator.model.Participator;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.riot.performance.Lane;
import de.xahrie.trues.api.scouting.ScoutingGameType;
import de.xahrie.trues.api.util.Util;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.Nullable;

public class ScoutingManager {
  private static final Map<OrgaTeam, Scouting> scoutings = new HashMap<>();

  @Nullable
  public static Scouting forTeam(OrgaTeam team) {
    return scoutings.getOrDefault(team, null);
  }

  public static void updateThread(ThreadChannel threadChannel) {
    scoutings.values().stream().filter(scouting -> scouting.thread() != null).filter(scouting -> scouting.thread().equals(threadChannel)).findFirst().ifPresent(Scouting::update);
  }

  public static void addForTeam(OrgaTeam orgaTeam, @NonNull Participator participator, Match match) {
    Scouting scouting = forTeam(orgaTeam);
    if (scouting == null || !scouting.match().equals(match)) {
      scouting = new Scouting(orgaTeam, participator, match);
      scoutings.put(scouting.orgaTeam(), scouting);
    }
    Arrays.stream(match.getParticipators()).forEach(matchParticipator -> matchParticipator.getTeamLineup().updateLineups());
    scouting.update();
  }

  public static void custom(AbstractTeam team, IReplyCallback event, ScoutingType type) {
    custom(team, event, type, null, 365, 1);
  }

  public static void custom(AbstractTeam team, IReplyCallback event, ScoutingType type, ScoutingGameType gameType, Integer days) {
    custom(team, event, type, gameType, days, 1);
  }

  public static void custom(AbstractTeam team, IReplyCallback event, ScoutingType type, ScoutingGameType gameType, Integer days, Integer page) {
    new CustomScouting(team).sendCustom(event, type, gameType, days, page);
  }

  public static void handlePlayerHistory(IReplyCallback event, @NonNull Player player, @Nullable
  Champion champion, @NonNull ScoutingGameType gameType, @Nullable
  Lane lane) {
    final String championOutput = Util.avoidNull(champion, "alle", Champion::getName);
    final String laneOutput = Util.avoidNull(lane, "alle", Lane::getDisplayName);
    final EmbedBuilder builder = new EmbedBuilder()
        .setTitle("Matchhistory von " + player.getSummonerName())
        .setDescription("Gametyp: **" + gameType.getDisplayName() + "**\nChampion: **" + championOutput + "**\nLane: **" + laneOutput + "**");
    player.analyze(gameType, 1000).analyzeGamesWith(champion, lane).forEach(builder::addField);
    event.getHook().sendMessageEmbeds(builder.build()).queue();
  }
}
