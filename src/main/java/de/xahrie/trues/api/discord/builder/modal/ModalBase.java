package de.xahrie.trues.api.discord.builder.modal;

import de.xahrie.trues.api.discord.user.DiscordUser;
import de.xahrie.trues.api.discord.util.Replyer;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;

@EqualsAndHashCode(callSuper = true)
public abstract class ModalBase extends Replyer {
  protected Modal.Builder builder;
  protected DiscordUser target;

  public ModalBase() {
    super(ModalInteractionEvent.class);
    final View annotation = getClass().asSubclass(this.getClass()).getAnnotation(View.class);
    if (annotation == null) {
      return;
    }
    this.name = annotation.value();
  }

  public void setTarget(DiscordUser target) {
    this.target = target;
  }

  public abstract Modal getModal(boolean value);

  public abstract boolean execute(ModalInteractionEvent event);
}
