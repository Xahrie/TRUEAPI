package de.xahrie.trues.api.scouting.scouting.teaminfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.database.query.Query;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Log
public class TeamInfoManager {
  private static final Map<OrgaTeam, TeamInfo> infos = new HashMap<>();
  private static final Set<OrgaTeam> toUpdate = Collections.synchronizedSet(new HashSet<>());

  public static TeamInfo fromTeam(OrgaTeam orgaTeam) {
    TeamInfo info = infos.getOrDefault(orgaTeam, null);
    if (info != null) return info;

    log.info("Lade " + orgaTeam.getName());
    info = new TeamInfo(orgaTeam);
    infos.put(orgaTeam, info);
    return info;
  }

  static void addTeam(OrgaTeam orgaTeam) {
    toUpdate.add(orgaTeam);
  }

  private static void load(OrgaTeam orgaTeam) {
    final TeamInfo info = fromTeam(orgaTeam);
    if (info.getMessage() == null) info.create();
    else {
      final List<MessageEmbed> infoList = info.getList();
      info.getMessage().editMessageEmbeds(infoList).queue();
    }
    info.setLastUpdate(LocalDateTime.now());
  }

  public static void loadAllData() {
    try {
      for (OrgaTeam fromOrgaTeam : new Query<>(OrgaTeam.class).entityList()) {
        final TeamInfo info = fromTeam(fromOrgaTeam);
        if (Duration.between(info.getLastUpdate(), LocalDateTime.now()).get(ChronoUnit.SECONDS) >= 24*3600) toUpdate.add(fromOrgaTeam);
      }
      toUpdate.forEach(TeamInfoManager::load);
      toUpdate.clear();
    } catch (ConcurrentModificationException modificationException) {
      toUpdate.clear();
    }
  }
}
