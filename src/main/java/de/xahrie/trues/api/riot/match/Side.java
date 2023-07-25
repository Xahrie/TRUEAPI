package de.xahrie.trues.api.riot.match;

import java.util.Arrays;

import de.xahrie.trues.api.database.connector.Listing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import no.stelar7.api.r4j.basic.constants.types.lol.TeamType;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchTeam;

@Listing(Listing.ListingType.LOWER)
@RequiredArgsConstructor
@Getter
public enum Side {
  BLUE(100),
  RED(200),
  NONE(300);

  public static Side ofId(final int id) {
    return Arrays.stream(Side.values()).filter(side -> side.getId() == id).findFirst().orElse(NONE);
  }

  private final int id;

  public MatchTeam getTeam(LOLMatch match) {
    return switch (this) {
      case BLUE -> match.getTeams().stream().filter(matchTeam -> matchTeam.getTeamId().equals(TeamType.BLUE)).findFirst().orElse(null);
      case RED -> match.getTeams().stream().filter(matchTeam -> matchTeam.getTeamId().equals(TeamType.RED)).findFirst().orElse(null);
      default -> null;
    };
  }

  public MatchTeam getOpponent(LOLMatch match) {
    if (this == NONE) return null;
    return (this == BLUE ? RED : BLUE).getTeam(match);
  }
}
