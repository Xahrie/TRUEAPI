package de.xahrie.trues.api.discord.group;

import de.xahrie.trues.api.database.query.Query;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiscordRoleFactory {
  /**
   * Erhalte eine CustomGroup. Ist sie nicht vorhanden, erstelle sie.
   *
   * @return null indicates {@link DiscordGroup}
   */
  @Nullable
  public static CustomDiscordGroup getCustomGroup(Role role) {
    final CustomDiscordGroup customGroup = determineCustomGroup(role);
    if (customGroup == null) {
      final var group = DiscordGroup.of(role);
      if (group == null) return createCustomGroup(role);
    }
    return customGroup;
  }

  private static CustomDiscordGroup determineCustomGroup(Role role) {
    return new Query<>(CustomDiscordGroup.class).where("discord_id", role.getIdLong()).entity();
  }

  private static CustomDiscordGroup createCustomGroup(Role role) {
    role.getManager().setMentionable(true).setPermissions().queue();
    return new CustomDiscordGroup(role.getIdLong(), role.getName(), GroupType.PINGABLE, false).forceCreate();
  }

  /**
   * Entferne CustomGroup ohne sie zu erstellen
   */
  public static void removeCustomGroup(Role role) {
    final CustomDiscordGroup customGroup = determineCustomGroup(role);
    if (customGroup != null) customGroup.forceDelete();
  }

  public static String getRoleName(@NotNull String teamName) {
    final String newName = teamName.replace("Technical Really Unique ", "")
        .replace("Technical Really ", "")
        .replace("TRUEsports ", "")
        .replace("TRUE ", "");
    return "TRUE " + newName;

  }
}
