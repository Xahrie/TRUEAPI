package de.xahrie.trues.api.scheduler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Schedule {
  String minute() default "*";
  String hour() default "*";
  String dayOfMonth() default "*";
  String month() default "*";
  String dayOfWeek() default "*";
  String year() default "*";
}
