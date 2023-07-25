package de.xahrie.trues.api.discord.group;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.discord.permissible.PermissionPattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;

@RequiredArgsConstructor
@Getter
@Listing(Listing.ListingType.LOWER)
public enum GroupType {
  DEFAULT(new PermissionPattern(Permission.NICKNAME_CHANGE).add(PermissionPattern.CHANNEL_INTERACT_TALK)),
  PINGABLE(new PermissionPattern()),
  ORGA_MEMBER(new PermissionPattern().add(PermissionPattern.CHANNEL_INTERACT_ADVANCED)),
  STAFF(new PermissionPattern().add(PermissionPattern.GUILD_MODERATE).add(PermissionPattern.CHANNEL_INTERACT_MODERATE)),
  ADMIN(new PermissionPattern().add(PermissionPattern.GUILD_ADMINISTRATE).add(PermissionPattern.CHANNEL_INTERACT_MODERATE)),
  CONTENT(new PermissionPattern().add(PermissionPattern.CONTENT_CREATION));
  
  private final PermissionPattern pattern;
}
