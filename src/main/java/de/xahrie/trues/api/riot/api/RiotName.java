package de.xahrie.trues.api.riot.api;

import java.util.Objects;

import de.xahrie.trues.api.util.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import no.stelar7.api.r4j.pojo.shared.RiotAccount;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@ExtensionMethod(StringUtils.class)
public class RiotName {

  @NotNull
  @Contract("_ -> new")
  public static RiotName of(@NotNull RiotAccount account) {
    return new RiotName(account.getName(), account.getTag());
  }

  @NotNull
  @Contract("_ -> new")
  public static RiotName of(String name) {
    return new RiotName(name);
  }

  @NotNull
  @Contract("_, _ -> new")
  public static RiotName of(String name, String tag) {
    return new RiotName(name, tag);
  }

  private String name;
  private String tag;

  private RiotName(@NotNull String name) {
    String tag = null;
    if (name.contains("#")) {
       tag = name.after("#", -1);
      name = name.before("#", -1);
    }
    this.name = name;
    this.tag = tag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final RiotName riotName = (RiotName) o;
    return Objects.equals(name, riotName.getName()) && Objects.equals(tag, riotName.getTag());
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, tag);
  }

  @Override
  @Nullable
  public String toString() {
    return isEmpty() ? null : "%s#%s".formatted(name, tag);
  }

  public boolean isEmpty() {
    return name == null || tag == null || name.isBlank() || tag.isBlank();
  }
}
