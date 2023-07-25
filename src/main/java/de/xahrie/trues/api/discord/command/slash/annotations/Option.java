package de.xahrie.trues.api.discord.command.slash.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.dv8tion.jda.api.interactions.commands.OptionType;

@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
  String name();
  String description() default "keine Beschreibung";
  boolean required() default true;
  OptionType type() default OptionType.STRING;
  String[] choices() default {};
  String completion() default "";
}
