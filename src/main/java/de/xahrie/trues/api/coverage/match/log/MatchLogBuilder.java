package de.xahrie.trues.api.coverage.match.log;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import de.xahrie.trues.api.coverage.match.model.Match;
import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.discord.builder.embed.EmbedFieldBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public record MatchLogBuilder(Match match, AbstractTeam team, List<MatchLog> matchLogs) {
  public MatchLogBuilder(Match match, AbstractTeam team) {
    this(match, team, match.getLogs().stream().filter(matchLog -> matchLog.getAction().getOutput() != null).sorted(Comparator.reverseOrder()).toList());
  }

  public MessageEmbed getLog() {
    final AbstractTeam opponent = match.getOpponentOf(team);
    final EmbedBuilder builder = new EmbedBuilder()
        .setTitle("Match " + match.getId() + " gegen " + opponent.getName() + " (" + opponent.getId() + ")")
        .setDescription(getDescription());
    getFields().forEach(builder::addField);
    return builder.build();
  }

  private String getDescription() {
    if (matchLogs.isEmpty()) {
      return "Match erstellt - Ausweichtermin am: **" + TimeFormat.DEFAULT_FULL.of(match.getStart()) + "**";
    }
    final MatchLog log = matchLogs.get(0);
    return log.getAction().getOutput() + "( von " + log.getParticipator().getTeam().getName() + " )\n" + log.detailsOutput();
  }

  public List<MessageEmbed.Field> getFields() {
    if (matchLogs.size() <= 1) return List.of();
    return new EmbedFieldBuilder<>(matchLogs.subList(1, matchLogs.size()))
        .add("Action", MatchLog::actionOutput)
        .add("Team", MatchLog::teamOutput)
        .add("Details", MatchLog::detailsOutput)
        .build();
  }

  @Override
  public String toString() {
    return matchLogs.stream().map(log -> log.getAction().getOutput() + "( von " + log.getParticipator().getTeam().getName() + " )\n" + log.detailsOutput()).collect(Collectors.joining("\n"));
  }
}
