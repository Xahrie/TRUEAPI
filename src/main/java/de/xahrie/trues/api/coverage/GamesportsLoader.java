package de.xahrie.trues.api.coverage;

import de.xahrie.trues.api.util.io.request.HTML;
import de.xahrie.trues.api.util.io.request.Request;
import de.xahrie.trues.api.util.io.request.URLType;
import lombok.Getter;

public class GamesportsLoader {
  @Getter
  protected final int id;
  protected final HTML html;
  protected final String url;

  @SuppressWarnings("ConfusingArgumentToVarargsMethod")
  public GamesportsLoader(URLType urlType, Integer... ids) {
    this.id = ids[ids.length - 1];
    this.html = Request.requestHTML(urlType, ids);
    this.url = String.format(urlType.getUrlName(), ids);
  }
}
