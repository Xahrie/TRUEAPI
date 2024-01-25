package de.xahrie.trues.api.util.io.request;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.java.Log;

@Log
public final class Request {
  static final List<String> errorUrls = new ArrayList<>();

  public static HTML requestHTML(URLType urlType, Object... arguments) {
    String urlString = urlType.getUrlName();
    urlString = String.format(urlString, arguments);
    return requestHTML(urlString);
  }

  public static HTML requestHTML(String urlString) {
    if (urlString.startsWith("http"))
      return new HTMLRequester(urlString).html();

    if (!errorUrls.contains(urlString)) {
      errorUrls.add(urlString);
      log.severe("No URL requested: " + urlString);
    }
    return new HTML();
  }
}
