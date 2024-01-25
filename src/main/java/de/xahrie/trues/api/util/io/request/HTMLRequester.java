package de.xahrie.trues.api.util.io.request;

import java.io.IOException;

import lombok.extern.java.Log;
import org.jsoup.Jsoup;

@Log
public record HTMLRequester(String path) {

  public HTML html() {
    String content = determineContent();
    if (content.contains("webmaster has already been notified")) {
      content = determineContent();
    }
    return new HTML(content);
  }

  private String determineContent() {
    try {
      return Jsoup.connect(path).get().html();
    } catch (IOException e) {
      log.severe("No URL requested: " + path);

      if (!Request.errorUrls.contains(path)) {
        Request.errorUrls.add(path);
        log.throwing("Request", "requestHTML(String): HTML", e);
      }
    }
    return "";
  }
}
