package de.xahrie.trues.api.calendar.event;

import java.util.List;

import de.xahrie.trues.api.datatypes.collections.SortedList;

public record EventParticipators(PlayerLimit limit, List<RoundParticipator> players) {
  public EventParticipators(PlayerLimit limit) {
    this(limit, SortedList.of());
  }

  public boolean add(RoundParticipator participator) {
    if (players.contains(participator)) return true;
    if (players.size() >= limit.full()) return false;
    players.add(participator);
    return true;
  }

  public boolean remove(RoundParticipator participator) {
    participator.delete();
    return players.remove(participator);
  }
}
