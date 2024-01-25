package de.xahrie.trues.api.util.io.request;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.xahrie.trues.api.util.StringUtils;
import de.xahrie.trues.api.util.io.cfg.JSON;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

@ExtensionMethod(StringUtils.class)
public class HTML {
  public static final String DESCRIPTION = "txt-info";
  public static final String ICON_INFO = "quick-info";
  public static final String MATCH_RESULT = "league-match-result";
  public static final String MATCH_SUBTITLE = "content-match-subtitles";
  public static final String MATCH_TEAMS = "content-match-head-team-top";
  public static final String MATCH_SCORE = "content-match-head-score";
  public static final String MATCHES = "league-stage-matches";
  public static final String MATCH_TIME = "tztime";
  public static final String PLAYDAY = "widget-ticker";
  public static final String PLAYERS = "content-portrait-grid";
  public static final String STAGE = "league-team-stage";
  public static final String SUBTITLE = "txt-subtitle";
  public static final String TEAM_HEAD = "content-portrait-head";
  public static final String TIME = "itime ";
  public static final String TIME_ATTRIBUTE = "data-time";

  private final String html;

  HTML() {
    this("");
  }

  HTML(String html) {
    this.html = html;
  }

  public HTML find(@NonNull String tag) {
    final List<HTML> htmlFound = findAll(tag);
    return htmlFound.isEmpty() ? new HTML() : htmlFound.get(0);
  }

  public HTML find(@NonNull String tag, @NonNull String clazz) {
    final List<HTML> htmlFound = findAll(tag, clazz);
    return htmlFound.isEmpty() ? new HTML() : htmlFound.get(0);
  }

  public HTML findId(@NonNull String tag, @NonNull String id) {
    final List<HTML> htmlFound = findAllId(tag, id);
    return htmlFound.isEmpty() ? new HTML() : htmlFound.get(0);
  }

  public List<HTML> findAll(@NonNull String tag) {
    return findTags(tag).stream()
        .mapToInt(html::indexOf)
        .mapToObj(index -> html.substring(index - 1 - tag.length(), findClosingIndex(tag, index)))
        .map(HTML::new).collect(Collectors.toList());
  }

  public List<HTML> findAll(@NonNull String tag, @NonNull String clazz) {
    return Arrays.stream(html.split("<" + tag))
        .filter(str -> str.contains("class=\"") && str.between("class=\"", "\"").contains(clazz))
        .mapToInt(html::indexOf)
        .mapToObj(index -> html.substring(index - 1 - tag.length(), findClosingIndex(tag, index)))
        .map(HTML::new).collect(Collectors.toList());
  }

  private List<HTML> findAllId(@NonNull String tag, @NonNull String id) {
    return Arrays.stream(html.split("<" + tag))
        .filter(str -> str.contains("id=\"") && str.between("id=\"", "\"").contains(id))
        .mapToInt(html::indexOf)
        .mapToObj(index -> html.substring(index - 1 - tag.length(), findClosingIndex(tag, index))).map(HTML::new).collect(Collectors.toList());
  }

  @NotNull
  private List<String> findTags(@NonNull String tag) {
    final List<String> split = new LinkedList<>(Arrays.asList(html.split("<" + tag)));
    if (!html.startsWith("<" + tag)) {
      split.remove(0);
    }
    return split;
  }

  private int findClosingIndex(@NonNull String tag, int index) {
    int opened = 1;
    for (int i = index; i < html.length(); i++) {
      final String s = html.substring(i);
      if (s.startsWith("<" + tag)) {
        opened++;
      }
      if (s.startsWith("</" + tag + ">")) {
        opened--;
      }
      if (opened == 0) {
        return i + 2 + tag.length();
      }
    }
    return html.length();
  }



  public String text() {
    if (html.isEmpty()) {
      return null;
    }
    final StringBuilder output = new StringBuilder();
    int tagsOpened = 0;
    for (int i = 0; i < html.length(); i++) {
      if (html.startsWith("<", i) && html.charAt(i+1) != ' ') {
        tagsOpened++;
      }
      if (tagsOpened == 0) {
        output.append(html.charAt(i));
      }
      if (tagsOpened > 0 && html.startsWith(">", i) && html.charAt(i-1) != ' ') {
        tagsOpened--;
      }
    }
    return output.toString();
  }

  public List<Attribute> getAttributes() {
    if (!this.html.contains("<") || !this.html.contains(">")) {
      return List.of();
    }

    String attrSection = html.between("<", ">");
    if (attrSection.isBlank() || !attrSection.contains(" ")) {
      return List.of();
    }

    attrSection = attrSection.substring(attrSection.indexOf(" ") + 1);
    final String[] tagStrings = attrSection.split("\" ");
    if (tagStrings.length == 0) {
      if (attrSection.contains("=")) {
        final var attribute = new Attribute(attrSection.split("=\"")[0], attrSection.split("=\"")[1]);
        return List.of(attribute);
      }
    }
    return Arrays.stream(attrSection.split("\" "))
        .map(str -> new Attribute(str.split("=\"")[0], str.split("=\"")[1].replace("\"", ""))).toList();
  }

  public String getAttribute(@NonNull String key) {
    return getAttributes().stream().filter(attribute -> attribute.key().equalsIgnoreCase(key))
        .map(Attribute::value).findFirst().orElse(null);
  }

  @Override
  public String toString() {
    return this.html;
  }

  public record Attribute(String key, String value) { }
}
