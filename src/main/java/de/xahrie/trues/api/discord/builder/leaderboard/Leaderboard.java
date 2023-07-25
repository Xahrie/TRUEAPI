package de.xahrie.trues.api.discord.builder.leaderboard;

import java.util.List;

import de.xahrie.trues.api.discord.builder.queryCustomizer.Alternative;
import de.xahrie.trues.api.discord.builder.queryCustomizer.SimpleCustomQuery;
import de.xahrie.trues.api.discord.builder.EmbedWrapper;
import de.xahrie.trues.api.discord.builder.InfoPanelBuilder;
import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class Leaderboard {
  protected final SimpleCustomQuery customQuery;

  public String createNewPublic(@NotNull MessageChannel channel) {
    final var leaderboard = new PublicLeaderboard(customQuery, channel.getIdLong());
    leaderboard.createNewPublic();
    return LeaderboardHandler.add(leaderboard);
  }

  public void buildNew(SlashCommandInteractionEvent event) {
    final Alternative alternative = customQuery.getNamedQuery().getAlternative();
    final EmbedWrapper data = getDataList(alternative);
    final List<MessageEmbed> wrapperEmbeds = data.getEmbeds();
    final String first = data.merge().get(0);
    final InteractionHook hook = event.getHook();
    if (first.isBlank()) hook.sendMessageEmbeds(wrapperEmbeds).queue();
    else {
      final WebhookMessageCreateAction<Message> msg = hook.sendMessage(first);
      if (!wrapperEmbeds.isEmpty()) msg.addEmbeds(wrapperEmbeds);
      msg.queue();
    }
  }

  protected EmbedWrapper getDataList(@Nullable Alternative alternative) {
    final List<List<Object[]>> data = customQuery.getData();
    final List<SimpleCustomQuery> queries = data == null ? List.of(customQuery) : data.stream().map(d -> SimpleCustomQuery.custom(customQuery.getNamedQuery(), d)).toList();

    return new InfoPanelBuilder(customQuery.getHeadTitle(), customQuery.getHeadDescription(), queries, alternative).build();
  }
}
