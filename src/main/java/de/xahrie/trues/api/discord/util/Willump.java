package de.xahrie.trues.api.discord.util;

import java.util.ArrayList;
import java.util.List;

import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.builder.leaderboard.LeaderboardHandler;
import de.xahrie.trues.api.discord.builder.modal.ModalImpl;
import de.xahrie.trues.api.discord.command.InputHandler;
import de.xahrie.trues.api.discord.command.context.ContextCommand;
import de.xahrie.trues.api.discord.command.slash.SlashCommand;
import de.xahrie.trues.api.scheduler.ScheduleManager;
import de.xahrie.trues.api.scheduler.ScheduledTask;
import de.xahrie.trues.api.util.Connectable;
import de.xahrie.trues.api.util.Const;
import de.xahrie.trues.api.discord.command.EventLogger;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Getter
public abstract class Willump implements Connectable {
  protected JDA client;
  protected Guild guild;

  public abstract List<SlashCommand> getCommands();

  public abstract List<ContextCommand> getContext();
  public abstract List<ListenerAdapter> getEvents();
  public abstract List<ModalImpl> getModals();
  public abstract List<ScheduledTask> getTasks();

  public void connect() {
    if (client != null) return;

    this.client = new BotConfigurator().run();
    handleEvents();
    try {
      client.awaitReady();
      this.guild = client.getGuildById(ConfigLoader.getGuildId());
      if (Const.REGISTER_COMMANDS) {
        register();
      }
      LeaderboardHandler.handleLeaderboards();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    new Query<>("UPDATE discord_user SET joined = null WHERE joined is not null").update(List.of());
    ScheduleManager.run();
  }

  public abstract void register();

  @Override
  public void disconnect() {
    new Query<>("UPDATE discord_user SET joined = null WHERE joined is not null").update(List.of());
  }

  private void handleEvents() {
    final var adapters = new ArrayList<>(List.of(new BotConfigurator(), new EventLogger(), new InputHandler()));
    adapters.addAll(getEvents());
    client.addEventListener(adapters.toArray());
  }

  public DiscordChannel getChannels() {
    return new DiscordChannel(this);
  }

  public Member getMember(User user) {
    return guild.getMember(user);
  }

  public DiscordRole getRoles() {
    return new DiscordRole(this);
  }
}
