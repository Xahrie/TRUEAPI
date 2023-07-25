package de.xahrie.trues.api.discord.permissible;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import net.dv8tion.jda.api.Permission;

@Data
public class PermissionPattern {
  public static final PermissionPattern CHANNEL_ACCESS = new PermissionPattern(
      Permission.MESSAGE_HISTORY,
      Permission.VIEW_CHANNEL,
      Permission.MESSAGE_ADD_REACTION,
      Permission.MESSAGE_EXT_EMOJI,
      Permission.MESSAGE_EXT_STICKER
  );

  public static final PermissionPattern CHANNEL_INTERACT = new PermissionPattern(
      Permission.MESSAGE_ATTACH_FILES,
      Permission.MESSAGE_EMBED_LINKS,
      Permission.MESSAGE_SEND,
      Permission.MESSAGE_SEND_IN_THREADS,
      Permission.REQUEST_TO_SPEAK,
      Permission.VOICE_CONNECT,
      Permission.CREATE_PUBLIC_THREADS,
      Permission.CREATE_PRIVATE_THREADS,
      Permission.USE_APPLICATION_COMMANDS
  ).add(CHANNEL_ACCESS);

  public static final PermissionPattern CHANNEL_INTERACT_TALK = new PermissionPattern(
      Permission.VOICE_SPEAK,
      Permission.VOICE_START_ACTIVITIES,
      Permission.VOICE_STREAM,
      Permission.VOICE_USE_VAD
  ).add(CHANNEL_INTERACT);

  public static final PermissionPattern CHANNEL_INTERACT_ADVANCED = new PermissionPattern(
      Permission.VOICE_MOVE_OTHERS,
      Permission.MESSAGE_MENTION_EVERYONE
  ).add(CHANNEL_INTERACT_TALK);

  public static final PermissionPattern CHANNEL_INTERACT_MODERATE = new PermissionPattern(
      Permission.MANAGE_THREADS,
      Permission.MESSAGE_TTS,
      Permission.PRIORITY_SPEAKER,
      Permission.VOICE_MUTE_OTHERS,
      Permission.VOICE_DEAF_OTHERS
  ).add(CHANNEL_INTERACT_TALK);

  public static final PermissionPattern GUILD_VIEW = new PermissionPattern(
      Permission.VIEW_AUDIT_LOGS,
      Permission.VIEW_GUILD_INSIGHTS
  );

  public static final PermissionPattern GUILD_MODERATE = new PermissionPattern(
      Permission.MESSAGE_MANAGE,
      Permission.MODERATE_MEMBERS,
      Permission.NICKNAME_MANAGE
  ).add(GUILD_VIEW);

  public static final PermissionPattern GUILD_ADMINISTRATE = new PermissionPattern(
      Permission.MANAGE_CHANNEL,
      Permission.KICK_MEMBERS
  ).add(GUILD_MODERATE);

  public static final PermissionPattern CONTENT_CREATION = new PermissionPattern(
      Permission.MANAGE_GUILD_EXPRESSIONS,
      Permission.MANAGE_EVENTS,
      Permission.MANAGE_WEBHOOKS
  );

  public static final PermissionPattern ALL = new PermissionPattern(
      Permission.MANAGE_SERVER,
      Permission.MANAGE_ROLES,
      Permission.MANAGE_PERMISSIONS,
      Permission.NICKNAME_CHANGE,
      Permission.CREATE_INSTANT_INVITE,
      Permission.ADMINISTRATOR
  ).add(CHANNEL_INTERACT_MODERATE).add(GUILD_ADMINISTRATE).add(CONTENT_CREATION);


  protected final Map<Permission, Boolean> permissions;

  public PermissionPattern(Permission... permissions) {
    this.permissions = new HashMap<>();
    allow(permissions);
  }

  public PermissionPattern add(PermissionPattern pattern) {
    pattern.getPermissions().forEach(this::addSingle);
    return this;
  }

  public PermissionPattern allow(Permission... permissions) {
    return set(true, permissions);
  }

  public PermissionPattern allow(PermissionPattern pattern) {
    final var permissions = (Permission[]) pattern.getPermissions().entrySet().stream().filter(Map.Entry::getValue).toArray();
    return set(true, permissions);
  }

  public PermissionPattern deny(Permission... permissions) {
    return set(false, permissions);
  }

  public PermissionPattern deny(PermissionPattern pattern) {
    final var permissions = (Permission[]) pattern.getPermissions().entrySet().stream().filter(entry -> !entry.getValue()).toArray();
    return set(false, permissions);
  }

  private PermissionPattern set(Boolean value, Permission[] permissions) {
    Arrays.asList(permissions).forEach(permission -> addSingle(permission, value));
    return this;
  }

  public PermissionPattern remove(Permission... permissions) {
    Arrays.stream(permissions).forEach(this.permissions::remove);
    return this;
  }

  public PermissionPattern remove(PermissionPattern pattern) {
    final List<Permission> permissionList = permissions.keySet().stream().filter(permission -> pattern.getPermissions().containsKey(permission)).toList();
    permissionList.forEach(permissions::remove);
    return this;
  }

  private void addSingle(Permission permission, Boolean value) {
    permissions.put(permission, value);
  }

  public Set<Permission> getAllowed() {
    return permissions.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toSet());
  }

  public Set<Permission> getDenied() {
    return permissions.entrySet().stream().filter(entry -> !entry.getValue()).map(Map.Entry::getKey).collect(Collectors.toSet());
  }
}
