package de.xahrie.trues.api.discord.user;

import de.xahrie.trues.api.calendar.scheduling.SchedulingHandler;
import de.xahrie.trues.api.community.application.ApplicationHandler;
import de.xahrie.trues.api.community.application.TeamRole;
import de.xahrie.trues.api.community.member.Membership;
import de.xahrie.trues.api.coverage.player.model.Player;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.ModifyOutcome;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.datatypes.calendar.TimeFormat;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import de.xahrie.trues.api.discord.group.DiscordGroup;
import de.xahrie.trues.api.discord.group.RoleGranter;
import de.xahrie.trues.api.discord.util.DefinedTextChannel;
import de.xahrie.trues.api.calendar.ApplicationCalendar;
import de.xahrie.trues.api.discord.notify.NotificationManager;
import de.xahrie.trues.api.discord.util.Jinx;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Table("discord_user")
@ExtensionMethod(SQLUtils.class)
public class DiscordUser implements Entity<DiscordUser> {
  @Serial
  private static final long serialVersionUID = 675455029296764536L;
  private int id;
  private final long discordId; // discord_id
  private String nickname; // mention
  private int points = 1000; // points
  private int messagesSent = 0; // msg_count
  private int digitsWritten = 0; // msg_digits
  private int secondsOnline = 0; // seconds_online
  private boolean active = false; // active
  private LocalDateTime lastTimeJoined; // joined#
  private Integer acceptedBy; // accepted
  private short notification = 0; // notification
  private LocalDate birthday; // birthday
  private boolean notifyRank = true; // notify_rank

  public DiscordUser(long discordId, String nickname) {
    this.discordId = discordId;
    this.nickname = nickname;
  }

  public DiscordUser(int id, long discordId, String nickname, int points, int messagesSent,
          int digitsWritten, int secondsOnline, boolean active, LocalDateTime lastTimeJoined,
          Integer acceptedBy, short notification, LocalDate birthday, boolean notifyRank) {
    this.id = id;
    this.discordId = discordId;
    this.nickname = nickname;
    this.points = points;
    this.messagesSent = messagesSent;
    this.digitsWritten = digitsWritten;
    this.secondsOnline = secondsOnline;
    this.active = active;
    this.lastTimeJoined = lastTimeJoined;
    this.acceptedBy = acceptedBy;
    this.notification = notification;
    this.birthday = birthday;
    this.notifyRank = notifyRank;
  }

  @Nullable
  public Player getPlayer() {
    return new Query<>(Player.class).where("discord_user", this).entity();
  }

  @Override
  public void setId(int id) {
    this.id = id;
  }

  public void setNickname(String nickname) {
    if (!this.nickname.equals(nickname)) new Query<>(DiscordUser.class).col("mention", nickname).update(id);
    this.nickname = nickname;
  }

  public void setNotifyRank(boolean notifyRank) {
    if (this.notifyRank != notifyRank) new Query<>(DiscordUser.class).col("notify_rank", notifyRank).update(id);
    this.notifyRank = notifyRank;
  }

  public String getMention() {
    return "<@" + discordId + ">";
  }

  public void setPoints(int points) {
    this.points = points;
    new Query<>(DiscordUser.class).col("points", points).update(id);
  }

  public void setAcceptedBy(@NotNull DiscordUser acceptedBy) {
    this.acceptedBy = acceptedBy.getId();
    new Query<>(DiscordUser.class).col("accepted", acceptedBy).update(id);
    getApplications().updateApplicationStatus();
  }

  public DiscordUser getAcceptedBy() {
    if (acceptedBy == null) return null;
    return new Query<>(DiscordUser.class).where("discord_id", acceptedBy).entity();
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
    new Query<>(DiscordUser.class).col("birthday", birthday).update(id);
  }

  public static DiscordUser get(List<Object> objects) {
    return new DiscordUser(
        (int) objects.get(0),
        (long) objects.get(1),
        (String) objects.get(2),
        (int) objects.get(3),
        (int) objects.get(4),
        (int) objects.get(5),
        (int) objects.get(6),
        (boolean) objects.get(7),
        (LocalDateTime) objects.get(8),
        (Integer) objects.get(9),
        objects.get(10).shortValue(),
        (LocalDate) objects.get(11),
        (boolean) objects.get(12)
    );
  }

  @Override
  public DiscordUser create() {
    return new Query<>(DiscordUser.class).key("discord_id", discordId)
        .col("mention", nickname).col("points", points).col("msg_count", messagesSent).col("msg_digits", digitsWritten)
        .col("seconds_online", secondsOnline).col("active", active).col("joined", lastTimeJoined).col("accepted", acceptedBy)
        .col("notification", notification).col("birthday", birthday).col("notify_rank", notifyRank)
        .insert(this);
  }

  public void setNotification(short notification) {
    final Integer difference = notification == -1 ? null : this.notification - notification;
    if (difference != null && difference.equals(0)) return;
    this.notification = notification;
    NotificationManager.addNotifiersFor(this,difference);
    new Query<>(DiscordUser.class).col("notification", notification).update(id);
  }

  public void setLastTimeJoined(LocalDateTime lastTimeJoined) {
    if (this.lastTimeJoined == null || !this.lastTimeJoined.equals(lastTimeJoined))
      new Query<>(DiscordUser.class).col("joined", lastTimeJoined).update(id);
    this.lastTimeJoined = lastTimeJoined;
  }

  public List<DiscordUserGroup> getGroups() {
    return new Query<>(DiscordUserGroup.class).where("discord_user", this).entityList();
  }

  public List<Membership> getMemberships() {
    return new Query<>(Membership.class).where("discord_user", this).entityList();
  }

  public List<Membership> getMainMemberships() {
    return new Query<>(Membership.class).where("discord_user", this).and("role", TeamRole.MAIN).entityList();
  }

  public Member getMember() {
    final Member member = Jinx.instance.getGuild().getMemberById(discordId);
    if (member != null && nickname.startsWith("<@")) setNickname(member.getEffectiveName());
    return member;
  }

  public Set<DiscordGroup> getActiveGroups() {
    return getMember().getRoles().stream().map(DiscordGroup::of).filter(Objects::nonNull).collect(Collectors.toSet());
  }

  public void addTempGroups() {
    for (DiscordUserGroup discordUserGroup : getGroups()) {
      if (!discordUserGroup.isActive() && discordUserGroup.getRange().getEndTime().isAfter(LocalDateTime.now())) {
        addGroup(discordUserGroup.getDiscordGroup());
        discordUserGroup.setActive(true);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof final DiscordUser that)) return false;
    return getDiscordId() == that.getDiscordId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDiscordId());
  }

  public void addGroup(DiscordGroup group) {
    addGroup(group, LocalDateTime.now(), 0);
  }

  public void addGroup(DiscordGroup group, LocalDateTime start, int days) {
    new RoleGranter(this).add(group, start, days);
  }

  public void removeTempGroups() {
    for (DiscordUserGroup discordUserGroup : getGroups()) {
      if (discordUserGroup.isActive() && !discordUserGroup.getRange().hasEnded()) {
        removeGroup(discordUserGroup.getDiscordGroup());
        discordUserGroup.setActive(false);
      }
    }
  }

  public void removeGroup(DiscordGroup group) {
    new RoleGranter(this).remove(group);
  }

  public boolean isAbove(DiscordGroup group) {
    return getActiveGroups().stream().anyMatch(group::isAbove);
  }

  public boolean isEvenOrAbove(DiscordGroup group) {
    return getActiveGroups().contains(group) || getActiveGroups().stream().anyMatch(group::isAbove);
  }

  public void dm(String content) {
    getMember().getUser().openPrivateChannel()
        .flatMap(privateChannel -> privateChannel.sendMessage(content))
        .queue();
  }

  public void schedule(@NotNull LocalDateTime dateTime, @NotNull DiscordUser invoker) {
    setAcceptedBy(invoker);
    final var timeRange = new TimeRange(dateTime, Duration.ofMinutes(30));
    Jinx.instance.getChannels().getTextChannel(DefinedTextChannel.ADMIN_CHANNEL)
                 .sendMessage("Neuer Bewerbungstermin für " + invoker.getNickname())
                 .queue(message -> message.createThreadChannel("Bewerbung von " + invoker.getMember().getNickname())
            .queue(threadChannel -> new ApplicationCalendar(timeRange, "by " + getId() + " - " + nickname, this, threadChannel.getIdLong()).create()));
    dm("Neuer Termin für Vorstellungsgespräch: " + TimeFormat.DISCORD.of(dateTime));
  }

  public void addSeconds(boolean stillOnline) {
    if (lastTimeJoined != null) {
      final Duration duration = Duration.between(lastTimeJoined, LocalDateTime.now());
      this.secondsOnline += duration.getSeconds();
      this.points += Math.round(duration.getSeconds() / 60.);
      new Query<>(DiscordUser.class).col("joined", lastTimeJoined).col("seconds_online", secondsOnline).col("points", points).update(id);
    }
    setLastTimeJoined(stillOnline ? LocalDateTime.now() : null);
  }

  public void addPoints(int points) {
    if (points == 0) return;
    this.points += points;
    new Query<>(DiscordUser.class).col("points", this.points).update(id);
  }

  public void addMessage(String content) {
    this.messagesSent++;
    this.digitsWritten += content.length();
    this.points += content.length();
    new Query<>(DiscordUser.class).col("msg_count", messagesSent).col("msg_digits", digitsWritten).col("points", points).update(id);
  }

  private SchedulingHandler scheduling;

  public SchedulingHandler getScheduling() {
    if (scheduling == null) this.scheduling = new SchedulingHandler(this);
    return scheduling;
  }

  private ApplicationHandler applications;

  public ApplicationHandler getApplications() {
    if (applications == null) this.applications = new ApplicationHandler(this);
    return applications;
  }

  public ModifyOutcome setPlayer(@Nullable Player player) {
    final Player currentPlayer = getPlayer();
    final boolean wasChanged = currentPlayer != null;
    if (Objects.equals(currentPlayer, player)) return ModifyOutcome.NOTHING;
    if (currentPlayer != null && !currentPlayer.equals(player)) currentPlayer.setDiscordUser(null);
    if (player == null) return ModifyOutcome.REMOVED;
    player.setDiscordUser(this);
    return wasChanged ? ModifyOutcome.CHANGED : ModifyOutcome.ADDED;
  }
}
