package de.xahrie.trues.api.discord.builder.leaderboard;

import de.xahrie.trues.api.discord.builder.EmbedWrapper;
import de.xahrie.trues.api.discord.builder.queryCustomizer.Alternative;
import de.xahrie.trues.api.discord.builder.queryCustomizer.NamedQuery;
import de.xahrie.trues.api.discord.builder.queryCustomizer.SimpleCustomQuery;
import de.xahrie.trues.api.discord.util.Jinx;
import de.xahrie.trues.api.util.io.log.Console;
import de.xahrie.trues.api.util.io.log.DevInfo;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PublicLeaderboard extends Leaderboard {
  private final long channelID;
  private MessageChannel channel;
  @Getter
  private final List<Long> messageIds;

  protected int required;

  public PublicLeaderboard(SimpleCustomQuery customQuery, long channelID) {
    this(customQuery, channelID, new ArrayList<>());
  }

  private PublicLeaderboard(SimpleCustomQuery customQuery, long channelID, List<Long> messageIds) {
    super(customQuery);
    this.channelID = channelID;
    this.messageIds = messageIds;
  }

  public MessageChannel getChannel() {
    if (channel == null) {
      final GuildChannel eventChannel = Jinx.instance.getChannels().getChannel(channelID);
      if (!(eventChannel instanceof MessageChannel messageChannel)) {
        new DevInfo("Leaderboard-Error").with(Console.class).warn(new IllegalStateException("Der Channel muss ein MessageChannel sein!"));
        return null;
      }
      this.channel = messageChannel;
    }
    return channel;
  }

  public void createNewPublic() {
    if (getChannel() == null) {
      new DevInfo("Channel konnte nicht gefunden werden").with(Console.class).warn(new NullPointerException("Kein Channel für Leaderboard"));
      return;
    }

    final Alternative alternative = customQuery.getNamedQuery().getAlternative();
    final EmbedWrapper data = getDataList(alternative);
    final List<MessageEmbed> wrapperEmbeds = data.getEmbeds();
    final List<String> merge = data.merge();
    this.required = Math.max(1, merge.size());
    if ((merge.isEmpty() || merge.stream().allMatch(String::isBlank)) && wrapperEmbeds.isEmpty())
      channel.sendMessage("keine Daten").queue(this::addMessage);

    for (int i = 0; i < merge.size(); i++) {
      final String content = merge.get(i);
      if (content.isBlank()) continue;

      final MessageCreateAction msg = channel.sendMessage(content);
      if (i + 1 == merge.size() && !wrapperEmbeds.isEmpty()) msg.addEmbeds(wrapperEmbeds);
      msg.queue(this::addMessage);
    }
  }

  public void updateData() {
    System.err.println(customQuery.getName());
    if (getChannel() == null) return;

    final Alternative alternative = customQuery.getNamedQuery().getAlternative();
    final EmbedWrapper data = getDataList(alternative);
    final List<MessageEmbed> wrapperEmbeds = data.getEmbeds();
    final List<String> merge = data.merge();
    if (merge.isEmpty() || merge.get(0).isBlank()) {
      if (!wrapperEmbeds.isEmpty())
        getChannel().retrieveMessageById(messageIds.get(0)).queue(message -> message.editMessageEmbeds(wrapperEmbeds).queue());
      return;
    }
    for (int i = 0; i < merge.size(); i++) {
      if (messageIds.size() < merge.size())
        delete().createNewPublic();

      final String content = merge.get(i);
      if (i + 1 == merge.size() && !wrapperEmbeds.isEmpty())
        getChannel().retrieveMessageById(messageIds.get(i)).queue(message -> message.editMessage(content).setEmbeds(wrapperEmbeds).queue());
      else getChannel().retrieveMessageById(messageIds.get(i)).queue(message -> message.editMessage(content).setEmbeds().queue());
    }
  }

  void addMessage(@NotNull Message message) {
    messageIds.add(message.getIdLong());
  }

  PublicLeaderboard delete() {
    try {
      getMessageIds().forEach(msgId -> getChannel().retrieveMessageById(msgId).complete().delete().queue());
      messageIds.clear();
    } catch (ErrorResponseException exception) {
      System.out.println(String.join(",", getMessageIds().stream().map(String::valueOf).toList()));
    }
    return this;
  }

  private void fromTo(int page, int maxPages, @NotNull List<MessageEmbed> embeds, Message message) {
    final int embedPages = (int) Math.ceil(embeds.size() / 10.);
    final int firstRemainingPage = maxPages - embedPages;
    if (page >= firstRemainingPage) {
      final int upcomingPages = maxPages - page;
      final int end = embeds.size() - 10 * upcomingPages;
      final int start = Math.max(0, end - 10);
      message.editMessageEmbeds(embeds.subList(start, end)).queue();
    } else {
      message.editMessageEmbeds().queue();
    }
  }

  public JSONObject toJSON() {
    final var leaderboardData = new JSONObject();
    leaderboardData.put("key", customQuery.getName());
    leaderboardData.put("channelId", channelID);
    leaderboardData.put("messageIds", new JSONArray(messageIds));
    leaderboardData.put("parameters", new JSONArray(customQuery.getParameters().stream().map(param -> (String) param).toList()));
    return leaderboardData;
  }

  @NotNull
  public static PublicLeaderboard fromJSON(JSONObject entry) {
    final List<String> parameters = IntStream.range(0, entry.getJSONArray("parameters").length()).mapToObj(entry.getJSONArray("parameters")::getString).toList();
    return new PublicLeaderboard(
        SimpleCustomQuery.params(NamedQuery.valueOf(entry.getString("key")), parameters.stream().map(string -> (Object) string).toList()),
        entry.getLong("channelId"),
        IntStream.range(0, entry.getJSONArray("messageIds").length()).mapToObj(entry.getJSONArray("messageIds")::getLong).collect(Collectors.toList()));
  }
}
