package de.xahrie.trues.api.discord.util;

import de.xahrie.trues.api.Registerer;
import de.xahrie.trues.api.discord.builder.modal.ModalImpl;
import de.xahrie.trues.api.discord.command.context.ContextCommand;
import de.xahrie.trues.api.discord.command.slash.SlashCommand;
import de.xahrie.trues.api.scheduler.ScheduledTask;
import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

@Getter
public class Jinx extends Willump {
  public static Jinx instance;
  private final List<SlashCommand> commands;
  private final List<ContextCommand> context;
  private final List<ListenerAdapter> events;
  private final List<ModalImpl> modals;
  private List<ScheduledTask> tasks;
  private final Registerer<ScheduledTask> taskRegisterer;

  public Jinx(Registerer<SlashCommand> commands, Registerer<ContextCommand> context,
          Registerer<ListenerAdapter> events, Registerer<ModalImpl> modals,
          Registerer<ScheduledTask> tasks) {
    this.commands = commands.register();
    this.context = context.register();
    this.events = events.register();
    this.modals = modals.register();
    this.tasks = tasks.register();
    this.taskRegisterer = tasks;
  }

  public void resetTasks() {
    this.tasks = taskRegisterer.register();
  }

  @Override
  public void register() {
    guild.updateCommands()
         .addCommands(commands.stream().map(SlashCommand::commandData).toList())
         .addCommands(context.stream().map(ContextCommand::commandData).toList())
         .queue();
  }
}
