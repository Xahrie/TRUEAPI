package de.xahrie.trues.api.discord.command.context;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.dv8tion.jda.api.interactions.commands.Command;

@Retention(RetentionPolicy.RUNTIME)
public @interface Context {
  String value();
  Command.Type type() default Command.Type.USER;
}
