package de.xahrie.trues.api.discord.message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import de.xahrie.trues.api.datatypes.collections.SortedList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;

@RequiredArgsConstructor
@Getter
public enum Emote {
  TOP("<:top1:1004702918399758427>", null),
  JUNGLE("<:jgl1:1004702947789254656>", null),
  MIDDLE("<:mid1:1004703031104905286>", null),
  BOTTOM("<:bot1:1004703088982106112>", null),
  SUPPORT("<:sup1:1004703127796187246>", null);

  private final String name;
  private final Long animatedId;

  public Emoji getEmoji() {
    return animatedId == null ? Emoji.fromFormatted(name) : Emoji.fromCustom(name, animatedId, true);
  }

  public static List<Emote> getEmotesFromMessage(Message message) {
    final String contentRaw = message.getContentRaw();
    final Stream<Emote> stream = Arrays.stream(Emote.values()).filter(emote -> contentRaw.contains(emote.getName()));
    return SortedList.of(stream);
  }
}
