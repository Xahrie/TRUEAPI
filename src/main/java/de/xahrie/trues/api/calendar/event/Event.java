package de.xahrie.trues.api.calendar.event;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.JoinQuery;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.riot.GameMap;
import de.xahrie.trues.api.riot.api.RiotName;
import de.xahrie.trues.api.util.Const;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Getter
@Setter
@Table(value = "event")
public class Event implements Entity<Event> {
  @Serial
  private static final long serialVersionUID = 1470862657594090344L;
  private int id;
  private final int calendarId;
  private EventCalendar calendar;

  public EventCalendar getCalendar() {
    if (calendar == null) this.calendar = new Query<>(EventCalendar.class).entity(calendarId);
    return calendar;
  }

  private GameMode gameMode;
  private int index;
  private GameMap map;
  private PlayerLimit playerLimit;
  private Long messageId;

  public Event(EventCalendar calendar, GameMode gameMode, GameMap map, PlayerLimit playerLimit) {
    this.calendar = calendar;
    this.calendarId = calendar.getId();
    this.gameMode = gameMode;
    this.index = new Query<>(Event.class).where("mode", gameMode).list().size();
    this.map = map;
    this.playerLimit = playerLimit;
  }

  private Event(int id, int calendarId, GameMode gameMode, int index, GameMap map, PlayerLimit playerLimit, Long messageId) {
    this.id = id;
    this.calendarId = calendarId;
    this.gameMode = gameMode;
    this.index = index;
    this.map = map;
    this.playerLimit = playerLimit;
    this.messageId = messageId;
  }

  public static Event get(List<Object> objects) {
    return new Event(
        (int) objects.get(0),
        (int) objects.get(1),
        new SQLEnum<>(GameMode.class).of(objects.get(2)),
        (int) objects.get(3),
        new SQLEnum<>(GameMap.class).of(objects.get(4)),
        new PlayerLimit((int) objects.get(5), (int) objects.get(6)),
        (Long) objects.get(7)
    );
  }

  @Override
  public Event create() {
    return new Query<>(Event.class).key("calendar", calendarId)
        .col("mode", gameMode).col("event_index", index).col("map", map)
        .col("players_min", playerLimit.required()).col("players_max", playerLimit.full()).col("message", messageId).insert(this);
  }

  private List<Round> rounds;

  public List<Round> getRounds() {
    if (rounds == null) this.rounds = new Query<>(Round.class).where("event", id).entityList();
    return rounds;
  }

  public Round getNextRound(Round round) {
    final LocalTime nextRoundTime = round.getStartTime().plusMinutes(15);

    return getRounds().stream().filter(r -> r.getStartTime() == nextRoundTime).findFirst()
        .orElse(nextRoundTime.isAfter(getCalendar().getRange().getEndTime().toLocalTime()) ? null :
            new Round(this, nextRoundTime).create());
  }

  public void addRound(Round round) {
    getRounds().add(round);
    editScheduledEvent();
    sendMessage();
  }

  public void removePlayer(Player player) {
    final List<RoundParticipator> roundParticipators = new Query<>(RoundParticipator.class).join(new JoinQuery<>(RoundParticipator.class, Round.class).col("event_round")).where("player", player).and("_round.event", id).entityList();
    roundParticipators.forEach(roundParticipator -> roundParticipator.getRound().getParticipators().remove(roundParticipator));
  }

  public void setMessageId(Long messageId) {
    if (Objects.equals(this.messageId, messageId)) return;
    new Query<>(Event.class).col("message", messageId).update(id);
    this.messageId = messageId;
  }

  public void sendMessage() {
    final NewsChannel channel = Jinx.instance.getGuild().getNewsChannelById(Const.Channels.TICKER_CHANNEL);
    if (channel == null) throw new NullPointerException("Der Livechannel existiert nicht.");

    if (messageId == null) {
      channel.sendMessageEmbeds(determineEmbed()).setComponents(List.of(ActionRow.of(determineButtons())))
          .queue(message -> setMessageId(message.getIdLong()));
    } else {
      channel.retrieveMessageById(messageId).queue(message -> message.editMessageEmbeds(determineEmbed())
          .setComponents(List.of(ActionRow.of(determineButtons()))).queue());
    }
  }

  private MessageEmbed determineEmbed() {
    final EmbedBuilder builder = new EmbedBuilder().setTitle(gameMode.toString() + " #" + index + " (" + map.getAbbreviation() + ")")
        .setDescription("Map: " + map.getName() + "\nSpieler benötigt: " + playerLimit.required() + "\nSpielerslots: " + playerLimit.full())
        .setImage("https://cdn.discordapp.com/attachments/1111949026221375500/1119242192364523530/image.png")
        .setFooter("zuletzt aktualisiert " + TimeFormat.DEFAULT.now());
    if (getCalendar().getDetails() != null) builder.addField("wichtige Informationen: ", getCalendar().getDetails(), false);
    if (getGameMode().rules != null) builder.addField("Regeln:", getGameMode().getRules(), false);
    for (int i = 0; i < getRounds().size(); i++) {
      final Round round = getRounds().get(i);
      String title = "Runde " + i+1 + " - " + TimeFormat.HOUR.of(round.getTimestamp()) + " Uhr";
      String description;
      if (round.getTimestamp().isAfter(LocalDateTime.now())) {
        final List<RoundParticipator> roundParticipators = new Query<>(RoundParticipator.class).where("event_round", round.getId()).and("team_index", 1).entityList();
        final List<RoundParticipator> roundParticipators2 = new Query<>(RoundParticipator.class).where("event_round", round.getId()).and("team_index", 1).entityList();
        description = "Team 1: " + roundParticipators.stream().map(RoundParticipator::getPlayer)
            .map(Player::getName).map(RiotName::toString).collect(Collectors.joining(", ")) +
            "\nTeam 2: " + roundParticipators2.stream().map(RoundParticipator::getPlayer)
            .map(Player::getName).map(RiotName::toString).collect(Collectors.joining(", "));
      } else {
        description = "Angemeldete Spieler: " + round.getParticipators().players().stream().map(RoundParticipator::getPlayer)
            .map(Player::getName).map(RiotName::toString).collect(Collectors.joining(", "));
      }
      builder.addField(title, description, false);
    }
    return builder.build();
  }

  private Button[] determineButtons() {
    final List<Button> buttons = getRounds().stream().map(round ->
        Button.primary("round-" + round.getId(), TimeFormat.HOUR.of(round.getTimestamp()) + " (" + round.getParticipators().players().size() + ")")).collect(Collectors.toList());
    buttons.add(Button.danger("event-" + id, "Nicht mehr teilnehmen"));
    return buttons.toArray(Button[]::new);
  }

  public void createScheduledEvent() {
    final GuildChannel eventChannel = Jinx.instance.getChannels().getChannel(Const.Channels.EVENT_CHANNEL);
    if (eventChannel == null) throw new NullPointerException("Der Eventchannel existiert nicht.");

    final ScheduledEvent complete = Jinx.instance.getGuild().createScheduledEvent(gameMode.name() + " #" + index, eventChannel, getCalendar().getRange().getStartTime().atZone(ZoneId.systemDefault()).toOffsetDateTime())
        .setEndTime(getCalendar().getRange().getEndTime().atZone(ZoneId.systemDefault()).toOffsetDateTime())
        .setDescription(description()).complete();

    getCalendar().setThreadId(complete.getIdLong());
  }

  void editScheduledEvent() {
    final ScheduledEvent complete =  Jinx.instance.getGuild().retrieveScheduledEventById(getCalendar().getThreadId()).complete();
    complete.getManager()
        .setName(gameMode.name() + " #" + index)
        .setStartTime(getCalendar().getRange().getStartTime().atZone(ZoneId.systemDefault()).toOffsetDateTime())
        .setEndTime(getCalendar().getRange().getEndTime().atZone(ZoneId.systemDefault()).toOffsetDateTime())
        .setDescription(description()).queue();
  }

  private String description() {
    return (getCalendar().getDetails() == null ? "" : "__Wichtige Informationen:__\n" + getCalendar().getDetails() + "\n\n") + gameMode.getRules();
  }
}
