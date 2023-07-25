package de.xahrie.trues.api.util.io.request;

import java.net.MalformedURLException;
import java.net.URL;
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
    try {
      if (urlString.startsWith("http")) {
        final URL url = new URL(urlString);
        return new HTMLRequester(url).html();
      }
      if (!errorUrls.contains(urlString)) {
        errorUrls.add(urlString);
        log.severe("No URL requested: " + urlString);
      }
    } catch (MalformedURLException urlException) {
      log.severe("Wrong url");
      log.throwing("Request", "requestHTML(String): HTML", urlException);
    }
    return new HTML();
  }

}
