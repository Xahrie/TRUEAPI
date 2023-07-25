package de.xahrie.trues.api.discord.command.context;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UseView {
  String[] value();
}
