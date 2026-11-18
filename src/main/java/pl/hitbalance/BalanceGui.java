package pl.hitbalance;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BalanceGui implements InventoryHolder {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final int SLOT_PREVIEW = 4;
    private static final int SLOT_ENABLED = 10;
    private static final int SLOT_STEP = 11;
    private static final int SLOT_MIN = 12;
    private static final int SLOT_MAX = 13;
    private static final int SLOT_MODE = 14;
    private static final int SLOT_PLAYERS = 15;
    private static final int SLOT_RELOAD = 16;
    private static final int SLOT_CLOSE = 22;

    private final BalanceManager balance;
    private final Inventory inventory;

    private BalanceGui(BalanceManager balance) {
        this.balance = balance;
        this.inventory = org.bukkit.Bukkit.createInventory(this, 27,
                MM.deserialize("<gradient:#ff5555:#ffaa00><bold>HitBalance</bold></gradient> <dark_gray>» <gray>Balans hitow"));
    }

    public static void open(Player player, BalanceManager balance) {
        BalanceGui gui = new BalanceGui(balance);
        gui.render();
        player.openInventory(gui.inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private void render() {
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, "<gray> ", List.of());
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(SLOT_PREVIEW, previewItem());

        inventory.setItem(SLOT_ENABLED, item(
                balance.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE,
                balance.isEnabled() ? "<green><bold>Balans: WLACZONY" : "<red><bold>Balans: WYLACZONY",
                List.of("<gray>Wlacza / wylacza caly plugin.", "", "<yellow>Kliknij aby przelaczyc")));

        inventory.setItem(SLOT_STEP, item(Material.DIAMOND_SWORD,
                "<gold><bold>Krok na poziom: <white>" + pct(balance.getStep()),
                List.of("<gray>O ile % zmienia sie obrazenia",
                        "<gray>za kazdy poziom roznicy",
                        "<gray>Sharpness vs Protection.", "",
                        "<yellow>LPM <gray>+0.01   <yellow>PPM <gray>-0.01",
                        "<yellow>Shift <gray>= x5")));

        inventory.setItem(SLOT_MIN, item(Material.IRON_CHESTPLATE,
                "<aqua><bold>Min. mnoznik: <white>x" + fmt(balance.getMinMultiplier()),
                List.of("<gray>Dolna granica obrazen", "",
                        "<yellow>LPM <gray>+0.05   <yellow>PPM <gray>-0.05",
                        "<yellow>Shift <gray>= x5")));

        inventory.setItem(SLOT_MAX, item(Material.NETHERITE_SWORD,
                "<red><bold>Max. mnoznik: <white>x" + fmt(balance.getMaxMultiplier()),
                List.of("<gray>Gorna granica obrazen", "",
                        "<yellow>LPM <gray>+0.05   <yellow>PPM <gray>-0.05",
                        "<yellow>Shift <gray>= x5")));

        inventory.setItem(SLOT_MODE, item(Material.ENCHANTED_BOOK,
                "<light_purple><bold>Tryb Protection: <white>" + balance.getMode().name(),
                List.of("<gray>" + balance.getMode().description, "",
                        "<yellow>Kliknij aby zmienic tryb")));

        inventory.setItem(SLOT_PLAYERS, item(
                balance.isPlayersOnly() ? Material.PLAYER_HEAD : Material.ZOMBIE_HEAD,
                "<green><bold>Tylko gracze: <white>" + (balance.isPlayersOnly() ? "TAK" : "NIE"),
                List.of("<gray>TAK = balans tylko PvP,", "<gray>NIE = rowniez moby.", "",
                        "<yellow>Kliknij aby przelaczyc")));

        inventory.setItem(SLOT_RELOAD, item(Material.REDSTONE,
                "<red><bold>Przeladuj z config.yml",
                List.of("<gray>Wczytuje wartosci z pliku", "<gray>(nadpisuje niezapisane zmiany).")));

        inventory.setItem(SLOT_CLOSE, item(Material.BARRIER, "<red>Zamknij", List.of()));
    }

    private ItemStack previewItem() {
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Przyklady (Sharpness vs Protection):");
        lore.add("");
        int[][] samples = {{32, 32}, {34, 32}, {32, 34}, {40, 32}, {32, 40}, {10, 10}, {5, 0}};
        for (int[] s : samples) {
            double m = balance.multiplier(s[0], s[1]);
            String color = m > 1.0001 ? "<green>" : (m < 0.9999 ? "<red>" : "<yellow>");
            lore.add("<white>Sharp " + s[0] + " <dark_gray>vs <white>Prot " + s[1] + " <dark_gray>» " + color + "x" + fmt(m));
        }
        return item(Material.BOOK, "<gold><bold>Podglad mnoznikow", lore);
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(MM.deserialize(name).decoration(TextDecoration.ITALIC, false));
        List<Component> lines = new ArrayList<>();
        for (String l : lore) {
            lines.add(MM.deserialize(l).decoration(TextDecoration.ITALIC, false)
                    .colorIfAbsent(NamedTextColor.GRAY));
        }
        meta.lore(lines);
        stack.setItemMeta(meta);
        return stack;
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private static String pct(double v) {
        return String.format(Locale.US, "%.0f%%", v * 100.0);
    }

    // ------------------------------------------------------------------

    public static final class Listener implements org.bukkit.event.Listener {

        private final BalanceManager balance;

        public Listener(BalanceManager balance) {
            this.balance = balance;
        }

        @EventHandler
        public void onDrag(InventoryDragEvent event) {
            if (event.getInventory().getHolder() instanceof BalanceGui) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof BalanceGui gui)) {
                return;
            }
            event.setCancelled(true);
            if (event.getClickedInventory() == null || event.getClickedInventory() != gui.inventory) {
                return;
            }
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }
            if (!player.hasPermission("hitbalance.admin")) {
                player.closeInventory();
                return;
            }

            ClickType click = event.getClick();
            double dir = click.isRightClick() ? -1 : 1;
            double mult = click.isShiftClick() ? 5 : 1;

            switch (event.getSlot()) {
                case SLOT_ENABLED -> balance.setEnabled(!balance.isEnabled());
                case SLOT_STEP -> balance.setStep(balance.getStep() + dir * mult * 0.01);
                case SLOT_MIN -> balance.setMinMultiplier(balance.getMinMultiplier() + dir * mult * 0.05);
                case SLOT_MAX -> balance.setMaxMultiplier(balance.getMaxMultiplier() + dir * mult * 0.05);
                case SLOT_MODE -> balance.setMode(balance.getMode().next());
                case SLOT_PLAYERS -> balance.setPlayersOnly(!balance.isPlayersOnly());
                case SLOT_RELOAD -> {
                    org.bukkit.plugin.java.JavaPlugin.getPlugin(HitBalance.class).reloadConfig();
                    balance.load();
                    gui.render();
                    return;
                }
                case SLOT_CLOSE -> {
                    player.closeInventory();
                    return;
                }
                default -> {
                    return;
                }
            }
            balance.save();
            gui.render();
        }
    }
}
