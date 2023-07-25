package de.xahrie.trues.api.util;

import de.xahrie.trues.api.riot.RankedState;

public class Const {
  public static class Channels {
    public static final long DEV_LOGGING_CHANNEL = 1110145279753212036L;
    public static final long TEAM_LOGGING_CHANNEL = 1087093094815633428L;
    public static final long ADMIN_INTERN = 1118121503419027536L;
    public static final long EVENT_CHANNEL = 980157099780759622L;
    public static final long TICKER_CHANNEL = 1072112297717284884L;
  }

  public static class Gamesports {
    public static final String STARTER_NAME = "Swiss Starter";
    public static final String CALIBRATION_NAME = "Kalibrierung";
    public static final String PLAYOFF_NAME = "Playoff";
  }

  public static final int DISCORD_MESSAGE_MAX_CHARACTERS = 2000;
  public static final double PREDICTION_FACTOR = .015;
  public static final boolean REGISTER_COMMANDS = false;
  public static final boolean SAVE_LOGS = false;
  public static final String SCHEDULING_PREFIX = "<--";
  public static final boolean SHOW_SQL = false;
  public static final String THREAD_CHANNEL_START = "Scouting vs. ";
  public static final RankedState RANKED_STATE = RankedState.RANKUPS;

  public static boolean check() {
    return true;
  }
}
