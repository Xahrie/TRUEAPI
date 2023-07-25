package de.xahrie.trues.api.riot.game;

import java.io.Serial;
import java.util.Comparator;
import java.util.List;

import de.xahrie.trues.api.database.connector.Listing;
import de.xahrie.trues.api.database.connector.SQLUtils;
import de.xahrie.trues.api.database.connector.Table;
import de.xahrie.trues.api.database.query.Entity;
import de.xahrie.trues.api.database.query.Query;
import de.xahrie.trues.api.database.query.SQLEnum;
import de.xahrie.trues.api.riot.champion.Champion;
import de.xahrie.trues.api.riot.match.Side;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

@Getter
@Table("selection")
@ExtensionMethod(SQLUtils.class)
public final class Selection implements Entity<Selection>, Comparable<Selection> {
  @Serial
  private static final long serialVersionUID = 1297903505337960151L;

  @Setter
  private int id; // selection_id
  private final int gameId; // game
  private final Side side; // first
  private final SelectionType type; // type
  private final byte selectOrder; // select_order
  private final int championId; // champion

  private Game game;

  public Game getGame() {
    if (game == null) this.game = new Query<>(Game.class).entity(gameId);
    return game;
  }

  private Champion champion;

  public Champion getChampion() {
    if (champion == null) this.champion = new Query<>(Champion.class).entity(championId);
    return champion;
  }

  private Selection(int id, int gameId, Side side, SelectionType type, byte selectOrder, int championId) {
    this.id = id;
    this.gameId = gameId;
    this.side = side;
    this.type = type;
    this.selectOrder = selectOrder;
    this.championId = championId;
  }

  public Selection(Game game, Side side, SelectionType type, byte selectOrder, Integer championId) {
    this.game = game;
    this.gameId = game.getId();
    this.side = side;
    this.type = type;
    this.selectOrder = selectOrder;
    this.championId = championId == null ? 0 : championId;
  }

  public static Selection get(List<Object> objects) {
    return new Selection(
        (int) objects.get(0),
        objects.get(1).intValue(),
        new SQLEnum<>(Side.class).of(objects.get(2)),
        new SQLEnum<>(SelectionType.class).of(objects.get(3)),
        objects.get(4).byteValue(),
        (int) objects.get(5)
    );
  }

  @Override
  public Selection create() {
    return new Query<>(Selection.class).key("game", gameId).key("side", side).key("type", type).key("select_order", selectOrder)
        .col("champion", championId)
        .insert(this, selection -> getGame().addSelection(selection));
  }

  @Override
  public int compareTo(@NotNull Selection o) {
    return Comparator.comparing(Selection::getGame)
        .thenComparing(Selection::getSide)
        .thenComparing(Selection::getType)
        .thenComparing(Selection::getSelectOrder).compare(this, o);
  }

  @Listing(Listing.ListingType.LOWER)
  public enum SelectionType {
    BAN, PICK
  }
}
