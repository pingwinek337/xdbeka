package pl.hitbalance;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HitBalance extends JavaPlugin {

    private BalanceManager balance;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        balance = new BalanceManager(this);
        balance.load();
        getServer().getPluginManager().registerEvents(new HitListener(balance), this);
        getServer().getPluginManager().registerEvents(new BalanceGui.Listener(balance), this);
        getLogger().info("HitBalance wlaczony.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hitbalance.admin")) {
            sender.sendMessage("§cBrak uprawnien.");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            balance.load();
            sender.sendMessage("§aPrzeladowano konfiguracje HitBalance.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("GUI dostepne tylko dla graczy. Uzyj /hitbalance reload.");
            return true;
        }
        BalanceGui.open(player, balance);
        return true;
    }
}
