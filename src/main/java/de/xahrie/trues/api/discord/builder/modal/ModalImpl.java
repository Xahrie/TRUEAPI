package de.xahrie.trues.api.discord.builder.modal;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.xahrie.trues.api.coverage.team.model.AbstractTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeam;
import de.xahrie.trues.api.community.orgateam.OrgaTeamFactory;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.scouting.scouting.Scouting;
import de.xahrie.trues.api.scouting.scouting.ScoutingManager;
import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.Util;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.ExtensionMethod;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
@ExtensionMethod(StringUtils.class)
public abstract class ModalImpl extends ModalBase {
  public ModalImpl() {
    super();
  }

  public ModalImpl create(String title) {
    builder = Modal.create(name, title);
    return this;
  }

  public ModalImpl required(String index, String title, String description, int length) {
    builder.addComponents(getRow(index, title, description, length, TextInputStyle.SHORT, true));
    return this;
  }

  public ModalImpl optional(String index, String title, String description, int length) {
    builder.addComponents(getRow(index, title, description, length, TextInputStyle.SHORT, false));
    return this;
  }

  public ModalImpl optional(String index, String title, List<? extends Enum<?>> enums) {
    final String desc = enums.stream().map(Enum::toString).filter(Objects::nonNull).collect(Collectors.joining(", "));
    final int length = enums.stream().map(Enum::toString).filter(Objects::nonNull).map(String::length).mapToInt(Integer::intValue).max().orElse(25);
    return optional(index, title, desc, length);
  }

  public ModalImpl required(String index, String title, Query<?> description, int length) {
    final String desc = description.list().stream().map(objects -> (String) objects[0]).collect(Collectors.joining(", "));
    return required(index, title, desc, length);
  }

  public ModalImpl required(String index, String title, List<? extends Enum<?>> enums) {
    final String desc = enums.stream().map(Enum::toString).filter(Objects::nonNull).collect(Collectors.joining(", "));
    final int length = enums.stream().map(Enum::toString).filter(Objects::nonNull).map(String::length).mapToInt(Integer::intValue).max().orElse(25);
    return required(index, title, desc, length);
  }

  public ModalImpl requiredMulti(String index, String title, String description, int length) {
    builder.addComponents(getRow(index, title, description, length, TextInputStyle.PARAGRAPH, true));
    return this;
  }

  public ModalImpl optionalMulti(String index, String title, String description, int length) {
    builder.addComponents(getRow(index, title, description, length, TextInputStyle.PARAGRAPH, false));
    return this;
  }

  public ModalImpl requiredMulti(String index, String title, List<? extends Enum<?>> enums) {
    final String desc = enums.stream().map(Enum::toString).filter(Objects::nonNull).collect(Collectors.joining(", "));
    final int length = enums.stream().map(Enum::toString).filter(Objects::nonNull).map(String::length).mapToInt(Integer::intValue).max().orElse(25);
    return requiredMulti(index, title, desc, length);
  }

  private ActionRow getRow(String index, String title, String description, int length, TextInputStyle style, boolean required) {
    return ActionRow.of(TextInput.create(index, title, style).setPlaceholder(description).setMaxLength(length).setRequired(required).build());
  }

  public Modal get() {
    return builder.build();
  }

  public String getString(String index) {
    final String string = Util.nonNull(modalEvent().getValue(index)).getAsString();
    return string.isEmpty() ? null : string;
  }

  public Integer getInt(String index) {
    return getInt(index, null);
  }

  public Integer getInt(String index, Integer otherwise) {
    try {
      return Integer.parseInt(getString(index));
    } catch (NumberFormatException ignored) {
      return otherwise;
    }
  }

  public Boolean getBoolean(String index) {
    return switch (getString(index).toLowerCase()) {
      case "ja" -> true;
      case "nein" -> false;
      default -> null;
    };
  }

  @Nullable
  protected <T extends Enum<T>> T getEnum(Class<T> enumClass, String index) {
    final String string = getString(index);
    return Arrays.stream(enumClass.getEnumConstants()).filter(t -> t.toString() != null).filter(t -> t.toString().equalsIgnoreCase(string)).findFirst().orElse(null);
  }

  //<editor-fold desc="objects">
  @Override
  protected DiscordUser getInvoker() {
    final String targetIdString = Util.nonNull(modalEvent().getValue("target-name")).getAsString();
    return new Query<>(DiscordUser.class).entity(targetIdString.intValue());
  }
  //</editor-fold>

  protected AbstractTeam determineTeam() {
    AbstractTeam team2 = null;
    final Integer anInt = getInt("1");
    if (anInt != null) team2 = new Query<>(AbstractTeam.class).where("prm_id", anInt).entity();
    if (getString("1") != null) {
      if (team2 == null) team2 = new Query<>(AbstractTeam.class).where("team_name", getString("1")).entity();
      if (team2 == null) team2 = new Query<>(AbstractTeam.class).where("team_abbr", getString("1")).entity();
    } else {
      GuildChannel gc = event.getGuildChannel();
      if (gc instanceof ThreadChannel thread) gc = thread.getParentChannel();
      final OrgaTeam team = OrgaTeamFactory.getTeamFromChannel(gc);
      final Scouting scouting = ScoutingManager.forTeam(team);
      if (scouting != null) team2 = scouting.participator().getTeam();
    }
    return team2;
  }

  private ModalInteractionEvent modalEvent() {
    return ((ModalInteractionEvent) event);
  }
}
