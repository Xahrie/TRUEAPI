package de.xahrie.trues.api.minecraft.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

@Builder
public class ItemFetcher {
  private Material material;
  private String name;
  private int amount = 1;
  private Short durability;
  private OfflinePlayer player;
  private String playerName;
  private Map<Enchantment, Integer> enchantments;
  private List<String> description;
  private String title;
  private String author;
  private List<String> pages;
  // private DyeColor color;

  public ItemStack getItem() {
    final ItemStack item = new ItemStack(material, Math.max(amount, 1));
    final ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      handleItemMeta(itemMeta);
      item.setItemMeta(itemMeta);
    }
    if (enchantments != null) {
      item.addUnsafeEnchantments(enchantments);
    }

    return item;
  }

  private void handleItemMeta(ItemMeta itemMeta) {
    if (name != null) {
      itemMeta.setDisplayName(name);
    }

    if (material.equals(Material.SKELETON_SKULL) && itemMeta instanceof SkullMeta) {
      if (player == null  && playerName != null) {
        UUID uuid = UUIDFetcher.getUUID(playerName);
        player = Bukkit.getOfflinePlayer(uuid);
      }
      ((SkullMeta) itemMeta).setOwningPlayer(player);
    }

    if (durability != null) {
      if (durability == Short.MAX_VALUE) {
        itemMeta.setUnbreakable(true);
      } else {
        ((Damageable) itemMeta).setDamage(durability);
      }
    }

    if (description != null) {
      itemMeta.setLore(description);
    }

    if (material.equals(Material.WRITTEN_BOOK)) {
      final BookMeta bookMeta = (BookMeta) itemMeta;
      if (author != null) {
        bookMeta.setAuthor(author);
      }
      if (title != null) {
        bookMeta.setTitle(title);
      }
      if (pages != null) {
        bookMeta.setPages(pages);
      }
    }
  }
}
