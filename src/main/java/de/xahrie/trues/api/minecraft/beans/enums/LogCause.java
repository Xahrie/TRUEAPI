package de.xahrie.trues.api.minecraft.beans.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public enum LogCause {
  BANNED("Spieler gesperrt"),
  CHATMODE_ALL("Chatmodus zu Alle geändert"),
  CHATMODE_DM("Chatmodus zu DM geändert"),
  CONNECT_ERROR_FULL("Server zu voll"),
  CONNECT_ERROR_OTHER("Verbindungsfehler"),
  CONNECT_ERROR_WHITELIST_FAIL("Spieler nicht zugelassen"),
  USER_JOIN("Spielerverbindung aufgebaut"),
  USER_LEAVE("Spielerverbindung getrennt");

  private final String message;
}
