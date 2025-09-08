package me.money.star.client.gui.modules;

import me.money.star.MoneyStar;
import me.money.star.client.settings.Setting;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import java.util.Comparator;
import java.util.function.Predicate;
public class CombatModule extends RotationModule {

    protected Setting<Boolean> multitask = bool("MultiTask", false);
    public CombatModule(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening)
    {
        super(name, description, category,hasListener,hidden,alwaysListening);
    }

    public CombatModule(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening, int rotationPriority)
    {
        super(name, description, category,hasListener,hidden,alwaysListening, rotationPriority);
    }

    public PlayerEntity getClosestPlayer(double range)
    {
        return mc.world.getPlayers().stream().filter(e -> !(e instanceof ClientPlayerEntity) && !e.isSpectator())
                .filter(e -> mc.player.squaredDistanceTo(e) <= range * range)
                .filter(e -> !MoneyStar.friendManager.isFriend(e.getName().getString()))
                .min(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e))).orElse(null);
    }

    public PlayerEntity getClosestPlayer(Predicate<AbstractClientPlayerEntity> entityPredicate, double range)
    {
        return mc.world.getPlayers().stream().filter(e -> !(e instanceof ClientPlayerEntity) && !e.isSpectator())
                .filter(entityPredicate)
                .filter(e -> mc.player.squaredDistanceTo(e) <= range * range)
                .filter(e -> !MoneyStar.friendManager.isFriend(e.getName().getString()))
                .min(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e))).orElse(null);
    }

    public boolean checkMultitask()
    {
        return checkMultitask(false);
    }

    public boolean checkMultitask(boolean checkOffhand)
    {
        if (checkOffhand && mc.player.getActiveHand() != Hand.MAIN_HAND)
        {
            return false;
        }
        return mc.player.isUsingItem();
    }
}
