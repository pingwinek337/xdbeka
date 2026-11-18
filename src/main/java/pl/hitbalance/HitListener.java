package pl.hitbalance;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HitListener implements Listener {

    private final BalanceManager balance;

    public HitListener(BalanceManager balance) {
        this.balance = balance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!balance.isEnabled()) {
            return;
        }
        // tylko bezposrednie uderzenia mieczem/bronia, bez lukow itp.
        if (event.getDamager() instanceof Projectile || !(event.getDamager() instanceof Player attacker)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (balance.isPlayersOnly() && !(victim instanceof Player)) {
            return;
        }

        int sharpness = balance.sharpnessOf(attacker.getInventory().getItemInMainHand());
        int protection = balance.protectionOf(victim.getEquipment());

        double mult = balance.multiplier(sharpness, protection);
        if (mult != 1.0) {
            event.setDamage(event.getDamage() * mult);
        }
    }
}
