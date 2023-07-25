package de.xahrie.trues.api.util.io.request;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import lombok.extern.java.Log;

@Log
public record HTMLRequester(URL url) {

  public HTML html() {
    String content = determineContent();
    if (content.contains("webmaster has already been notified")) {
      content = determineContent();
    }
    return new HTML(content);
  }

  private String determineContent() {
    try (final Scanner scanner = new Scanner(this.url.openStream(), StandardCharsets.UTF_8).useDelimiter("\\A")) {
      return scanner.hasNext() ? scanner.next() : "";
    } catch (IOException e) {
      if (!Request.errorUrls.contains(url.getPath())) {
        Request.errorUrls.add(url.getPath());
        log.severe("No URL requested: " + url.getPath());
        log.throwing(getClass().getName(), "determineContent", e);
      }
    }
    return "";
  }

}
