package de.xahrie.trues.api.util.io.cfg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import lombok.NonNull;
import org.json.JSONObject;

public final class JSON extends JSONObject {
  public static JSON read(@NonNull String fileName) {
    try {
      final String content = Files.readString(getPath(fileName));
      return new JSON(content);
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static void write(String fileName, String content) {
    doWrite(fileName, content, StandardOpenOption.WRITE);
  }

  public static void append(String fileName, String content) {
    doWrite(fileName, content, StandardOpenOption.APPEND);
  }

  private static void doWrite(String fileName, String content, StandardOpenOption mode) {
    try {
      Files.writeString(getPath(fileName), content, mode, StandardOpenOption.CREATE);
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @NonNull
  static Path getPath(@NonNull String fileName) throws URISyntaxException, FileNotFoundException {
    if (Files.exists(Paths.get("./resources/"))) {
      return Paths.get("./resources/" + fileName);
    } else {
      URL resource = JSON.class.getResource("/" + fileName);
      if (resource == null) {
        resource = JSON.class.getResource(fileName);
        if (resource == null) {
          resource = JSON.class.getClassLoader().getResource("/" + fileName);
          if (resource == null) {
            resource = JSON.class.getClassLoader().getResource(fileName);
            if (resource == null) {
              throw new FileNotFoundException("File " + fileName + " konnte nicht gefunden werden.");
            }
          }
        }
      }
      return Path.of(resource.toURI());
    }
  }

  private JSON(String source) {
    super(source);
  }
}
