package de.xahrie.trues.api.riot.api;

import java.util.Objects;

import de.xahrie.trues.api.util.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import no.stelar7.api.r4j.pojo.shared.RiotAccount;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@ExtensionMethod(StringUtils.class)
public class RiotName {

  public static RiotName of(RiotAccount account) {
    return new RiotName(account.getName(), account.getTag());
  }
  public static RiotName of(String name) {
    return new RiotName(name);
  }

  public static RiotName of(String name, String tag) {
    return new RiotName(name, tag);
  }

  private String name;
  private String tag;

  private RiotName(String name) {
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
  public String toString() {
    return "%s#%s".formatted(name, tag);
  }
}
