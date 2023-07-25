package de.xahrie.trues.api.coverage.season;

import de.xahrie.trues.api.database.query.Condition;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.util.Util;
import org.jetbrains.annotations.Nullable;

public final class SeasonFactory {

  @Nullable
  public static PRMSeason getSeason(int seasonId) {
    return new Query<>(PRMSeason.class).where("season_id", seasonId).entity();
  }

  @Nullable
  public static PRMSeason getLastPRMSeason() {
    return new Query<>(PRMSeason.class).where("season_start <= now()").descending("season_start").entity();
  }

  @Nullable
  public static PRMSeason getUpcomingPRMSeason() {
    return new Query<>(PRMSeason.class)
            .where(Condition.between("now()", "season_start", "season_end")).or("season_start >= now()")
            .ascending("season_start").entity();
  }

  @Nullable
  public static PRMSeason getCurrentPRMSeason() {
    final PRMSeason last = getLastPRMSeason();
    return Util.nonNull(last).getRange().hasEnded() ? getUpcomingPRMSeason() : last;
  }

  @Nullable
  public static Season getLastSeason() {
    return new Query<>(Season.class).where("season_start <= now()").descending("season_start").entity();
  }

  @Nullable
  public static Season getUpcomingSeason() {
    return new Query<>(Season.class)
        .where(Condition.between("now()", "season_start", "season_end")).or("season_start >= now()")
        .ascending("season_start").entity();
  }

  @Nullable
  public static Season getCurrentSeason() {
    final Season last = getLastSeason();
    return Util.nonNull(last).getRange().hasEnded() ? getUpcomingSeason() : last;
  }

  @Nullable
  public static OrgaCupSeason getLastInternSeason() {
    return new Query<>(OrgaCupSeason.class).where("season_start <= now()").descending("season_start").entity();
  }

  @Nullable
  public static OrgaCupSeason getUpcomingInternSeason() {
    return new Query<>(OrgaCupSeason.class)
        .where(Condition.between("now()", "season_start", "season_end")).or("season_start >= now()")
        .ascending("season_start").entity();
  }

  @Nullable
  public static OrgaCupSeason getCurrentInternSeason() {
    final PRMSeason last = getLastPRMSeason();
    return Util.nonNull(last).getRange().hasEnded() ? getUpcomingInternSeason() : getLastInternSeason();
  }
}
