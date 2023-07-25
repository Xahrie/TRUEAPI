package de.xahrie.trues.api.coverage;

import java.util.Comparator;
import java.util.List;

import de.xahrie.trues.api.database.connector.DTO;
import de.xahrie.trues.api.datatypes.calendar.TimeRange;
import org.jetbrains.annotations.NotNull;

public record EventDTO(TimeRange range, String name, boolean playday) implements DTO<EventDTO> {
  @Override
  public List<Object> getData() {
    return List.of(playday ? range.display() : range.displayRange(), name);
  }

  @Override
  public int compareTo(@NotNull EventDTO o) {
    return Comparator.comparing(EventDTO::playday).thenComparing(EventDTO::range).compare(this, o);
  }
}
