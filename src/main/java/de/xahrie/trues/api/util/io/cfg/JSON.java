package de.xahrie.trues.api.util.io.cfg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import lombok.NonNull;
import org.json.JSONObject;

public final class JSON extends JSONObject {
  private static FileSystem currentFileSystem;

  public static JSON read(@NonNull String fileName) {
    try {
      final String content = Files.readString(getPath(fileName));
      closeResource();
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
      closeResource();
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @NonNull
  static Path getPath(String fileName) throws URISyntaxException, FileNotFoundException {
    if (Files.exists(Paths.get("./resources/"))) {
      return Paths.get("./resources/" + fileName);
    } else {
      URL resource = JSON.class.getResource("/" + fileName); // for jars
      if (resource == null) {
        resource = JSON.class.getResource(fileName);
        if (resource == null) {
          resource = JSON.class.getClassLoader().getResource("/" + fileName); // for jars
          if (resource == null) {
            resource = JSON.class.getClassLoader().getResource(fileName);
            if (resource == null) {
              throw new FileNotFoundException("File " + fileName + " konnte nicht gefunden werden.");
            }
          }
        }
      }

      try {
        return Path.of(resource.toURI());
      } catch (FileSystemNotFoundException fileSystemException) { // for jars
        final String[] array = resource.toURI().toString().split("!");
        try {
          currentFileSystem = FileSystems.newFileSystem(URI.create(array[0]), new HashMap<>());
          return currentFileSystem.getPath(array[1]);
        } catch (IOException exception) {
          throw new FileNotFoundException("File " + fileName + " konnte nicht gefunden werden.");

        }
      }
    }
  }

  private static void closeResource() throws IOException {
    if (currentFileSystem != null) {
      currentFileSystem.close();
      currentFileSystem = null;
    }
  }

  private JSON(String source) {
    super(source);
  }
}
