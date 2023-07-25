package de.xahrie.trues.api.calendar.event;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.Const;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Table(value = "event_round")
public class Round implements Entity<Round>, Comparable<Round> {
  @Serial
  private static final long serialVersionUID = 7208176611901623344L;
  private int id;
  private final int eventId;
  private Event event;

  public Event getEvent() {
    if (event == null) this.event = new Query<>(Event.class).entity(eventId);
    return event;
  }

  private final LocalTime startTime;

  public LocalDateTime getTimestamp() {
    return LocalDateTime.of(event.getCalendar().getRange().getStartTime().toLocalDate(), startTime);
  }

  private EventParticipators participators;

  public EventParticipators getParticipators() {
    if (participators == null) this.participators = new EventParticipators(getEvent().getPlayerLimit());
    return participators;
  }

  public void addPlayer(Player player) {
    final RoundParticipator roundParticipator = new RoundParticipator(this, player);
    if (participators.add(roundParticipator)) {
      getEvent().sendMessage();
      player.getDiscordUser().dm("Du wurdest für heute " + TimeFormat.AUTO.of(getTimestamp()) + " eingetragen. Sollte eine Runde 15 Minuten vorher frei werden, werden wir auf dich zurückkommen.");
    } else {
      final Round nextRound = event.getNextRound(this);
      if (nextRound == null) player.getDiscordUser().dm("Du konntest in keine Runde für heute eingetragen werden.");
      else nextRound.addPlayer(player);
    }
  }

  public Round(Event event, LocalTime startTime) {
    this.event = event;
    this.eventId = event.getId();
    this.startTime = startTime;
  }

  private Round(int id, int eventId, LocalTime startTime) {
    this.id = id;
    this.eventId = eventId;
    this.startTime = startTime;
  }

  public static Round get(List<Object> objects) {
    return new Round(
        (int) objects.get(0),
        (int) objects.get(1),
        (LocalTime) objects.get(2)
    );
  }

  @Override
  public Round create() {
    final Round r = new Query<>(Round.class).col("event", eventId).col("start", startTime).insert(this);
    event.addRound(r);
    event.sendMessage();
    return r;
  }

  public void shuffle() {
    getParticipators().players().stream().findFirst().ifPresent(participator -> {
      if (participator.getTeamIndex() == null) doShuffle();
    });
  }

  private void doShuffle() {
    final List<PlayerEntry> players = getParticipators().players().stream().map(participator -> new PlayerEntry(participator.getPlayer())).toList();
    List<PlayerEntry> bestTeam1 = new ArrayList<>();
    List<PlayerEntry> bestTeam2 = new ArrayList<>();
    int difference = Integer.MAX_VALUE;
    final int teamAmount = players.size() / 2;
    for (int i = 0; i < 100; i++) {
      final ArrayList<PlayerEntry> pls = new ArrayList<>(players);
      Collections.shuffle(pls);
      final List<PlayerEntry> playerEntries1 = pls.subList(0, teamAmount);
      final List<PlayerEntry> playerEntries2 = pls.subList(teamAmount, players.size());
      final int diff = Math.abs(playerEntries1.stream().mapToInt(PlayerEntry::mmr).sum() - playerEntries2.stream().mapToInt(PlayerEntry::mmr).sum());
      if (difference > diff) {
        difference = diff;
        bestTeam1 = playerEntries1;
        bestTeam2 = playerEntries2;
      }
    }
    bestTeam1.forEach(playerEntry -> new Query<>(RoundParticipator.class).col("team_index", 1)
        .where("event_round", id).and("player", playerEntry.player()).update(List.of()));
    bestTeam2.forEach(playerEntry -> new Query<>(RoundParticipator.class).col("team_index", 2)
        .where("event_round", id).and("player", playerEntry.player()).update(List.of()));

    final NewsChannel channel = Jinx.instance.getGuild().getNewsChannelById(Const.Channels.TICKER_CHANNEL);
    if (channel == null) throw new NullPointerException("Der Tickerchannel wurde gelöscht.");
    channel.sendMessage("Die Runde wurde erstellt.").queue();
  }

  @Override
  public int compareTo(@NotNull Round o) {
    return Comparator.comparing(Round::getStartTime).compare(this, o);
  }

  public record PlayerEntry(Player player, int mmr) {
    public PlayerEntry(Player player) {
      this(player, player.getRanks().getLastRelevant().getRank().getMMR());
    }
  }
}
