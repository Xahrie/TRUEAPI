package de.xahrie.trues.api.discord.command.slash.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
  String name();
  String descripion() default "";
  Option[] options() default {};
  Perm perm();
}
