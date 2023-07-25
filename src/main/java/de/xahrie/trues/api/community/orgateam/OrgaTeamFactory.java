package de.xahrie.trues.api.community.orgateam;


import java.util.Arrays;
import java.util.stream.Collectors;

import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannel;
import de.xahrie.trues.api.community.orgateam.teamchannel.TeamChannelRepository;
import de.xahrie.trues.api.coverage.team.model.PRMTeam;
import de.xahrie.trues.api.coverage.team.TeamFactory;
import de.xahrie.trues.api.database.connector.Database;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.group.CustomDiscordGroup;
import de.xahrie.trues.api.discord.group.DiscordRoleFactory;
import de.xahrie.trues.api.discord.group.GroupType;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(StringUtils.class)
public final class OrgaTeamFactory {
  /**
   * Erhalte {@link OrgaTeam} vom Channel oder der Categorie in der sich der registrierte Channel befindet
   */
  @Nullable
  public static OrgaTeam getTeamFromChannel(@NonNull GuildChannel channel) {
    if (channel instanceof ThreadChannel threadChannel) return getTeamFromChannel(threadChannel.getParentChannel());
    Category category = null;
    if (channel instanceof ICategorizableChannel categorizableChannel) category = categorizableChannel.getParentCategory();
    if (channel instanceof Category categoryChannel) category = categoryChannel;
    channel = Util.avoidNull(category, channel);
    final TeamChannel teamChannel = TeamChannelRepository.getTeamChannelFromChannel(channel);
    return Util.avoidNull(teamChannel, null, TeamChannel::getOrgaTeam);
  }

  /**
   * Erhalte {@link OrgaTeam} vom Channel oder der Categorie in der sich der registrierte Channel befindet
   */
  public static boolean isRoleOfTeam(@NonNull Role role) {
    final CustomDiscordGroup discordGroup = new Query<>(CustomDiscordGroup.class).where("discord_Id", role.getIdLong()).entity();
    return Util.avoidNull(discordGroup, false, customDiscordGroup -> customDiscordGroup.getOrgaTeam() != null);
  }

  /**
   * Erstelle Team der Organisation
   * @param name Name des Teams (darf nicht Null sein)
   * @param abbreviation wenn {@code null} wird eine Abkürzung generiert
   * @param id wenn {@code not null} wird das {@link OrgaTeam} einem {@link PRMTeam} zugewiesen
   * @return erstelltes {@link OrgaTeam} oder {@code null} wenn nicht erstellt
   */
  public static OrgaTeam create(@NonNull String name, @Nullable String abbreviation, @Nullable Integer id) {
    if (abbreviation == null) {
      abbreviation = "TRU" + Arrays.stream(name.split(" ")).map(word -> word.substring(0, 1).upper()).collect(Collectors.joining());
    }
    if (fromAbbreviation(abbreviation) != null) return null;

    final String teamName = DiscordRoleFactory.getRoleName("TRUE " + name);
    final PRMTeam team = Util.avoidNull(id, null, TeamFactory::getTeam);
    final String finalAbbreviation = abbreviation;
    final Role role = Jinx.instance.getGuild().createRole().setName(teamName)
        .setPermissions().setMentionable(true).setHoisted(true).complete();
    final CustomDiscordGroup discordGroup = new CustomDiscordGroup(role.getIdLong(), teamName, GroupType.PINGABLE, true).create();
    final var orgaTeam = new OrgaTeam(teamName, finalAbbreviation, discordGroup).create();
    orgaTeam.setTeam(team);
    orgaTeam.getChannels().createChannels();
    Database.connection().commit();
    return orgaTeam;
  }

  @Nullable
  public static OrgaTeam fromAbbreviation(String abbreviation) {
    return new Query<>(OrgaTeam.class).where("team_abbr_created", abbreviation).entity();
  }
}
