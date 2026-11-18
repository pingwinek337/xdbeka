package pl.hitbalance;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EntityEquipment;

public class BalanceManager {

    public enum Mode {
        AVERAGE("Srednia ze wszystkich czesci zbroi (puste = 0)"),
        MAX("Najwyzszy poziom Protection na zbroi"),
        MIN("Najnizszy poziom Protection na zbroi");

        public final String description;

        Mode(String description) {
            this.description = description;
        }

        public Mode next() {
            Mode[] v = values();
            return v[(ordinal() + 1) % v.length];
        }
    }

    private final HitBalance plugin;

    private boolean enabled;
    private double step;
    private double minMultiplier;
    private double maxMultiplier;
    private Mode mode;
    private boolean playersOnly;

    public BalanceManager(HitBalance plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration c = plugin.getConfig();
        enabled = c.getBoolean("enabled", true);
        step = c.getDouble("step-per-level", 0.05);
        minMultiplier = c.getDouble("min-multiplier", 0.10);
        maxMultiplier = c.getDouble("max-multiplier", 3.00);
        playersOnly = c.getBoolean("players-only", false);
        try {
            mode = Mode.valueOf(c.getString("protection-mode", "AVERAGE").toUpperCase());
        } catch (IllegalArgumentException e) {
            mode = Mode.AVERAGE;
        }
    }

    public void save() {
        FileConfiguration c = plugin.getConfig();
        c.set("enabled", enabled);
        c.set("step-per-level", round(step));
        c.set("min-multiplier", round(minMultiplier));
        c.set("max-multiplier", round(maxMultiplier));
        c.set("protection-mode", mode.name());
        c.set("players-only", playersOnly);
        plugin.saveConfig();
    }

    /** Mnoznik obrazen: rowne poziomy = 1.0, wyzszy Sharpness = wiecej, wyzszy Protection = mniej. */
    public double multiplier(int sharpness, int protection) {
        if (sharpness <= 0 && protection <= 0) {
            return 1.0;
        }
        double m = 1.0 + (sharpness - protection) * step;
        return Math.max(minMultiplier, Math.min(maxMultiplier, m));
    }

    public int sharpnessOf(ItemStack weapon) {
        if (weapon == null || weapon.getType().isAir()) {
            return 0;
        }
        return weapon.getEnchantmentLevel(Enchantment.SHARPNESS);
    }

    public int protectionOf(EntityEquipment eq) {
        if (eq == null) {
            return 0;
        }
        ItemStack[] pieces = {eq.getHelmet(), eq.getChestplate(), eq.getLeggings(), eq.getBoots()};
        int sum = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;
        for (ItemStack p : pieces) {
            int lvl = (p == null || p.getType().isAir()) ? 0 : p.getEnchantmentLevel(Enchantment.PROTECTION);
            sum += lvl;
            max = Math.max(max, lvl);
            min = Math.min(min, lvl);
        }
        return switch (mode) {
            case AVERAGE -> Math.round(sum / 4f);
            case MAX -> max;
            case MIN -> min == Integer.MAX_VALUE ? 0 : min;
        };
    }

    public static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public double getStep() { return step; }
    public void setStep(double step) { this.step = Math.max(0.0, Math.min(1.0, round(step))); }

    public double getMinMultiplier() { return minMultiplier; }
    public void setMinMultiplier(double v) { this.minMultiplier = Math.max(0.0, Math.min(maxMultiplier, round(v))); }

    public double getMaxMultiplier() { return maxMultiplier; }
    public void setMaxMultiplier(double v) { this.maxMultiplier = Math.max(minMultiplier, Math.min(20.0, round(v))); }

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }

    public boolean isPlayersOnly() { return playersOnly; }
    public void setPlayersOnly(boolean playersOnly) { this.playersOnly = playersOnly; }
}
