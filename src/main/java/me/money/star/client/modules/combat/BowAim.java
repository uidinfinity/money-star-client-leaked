package me.money.star.client.modules.combat;


import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.RotationModule;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.entity.LookDirectionEvent;
import me.money.star.event.impl.network.PlayerUpdateEvent;
import me.money.star.util.world.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;


public class BowAim extends RotationModule {
    public Setting<Boolean> players = bool("Players", true);
    public Setting<Boolean> monsters = bool("Monsters", false);
    public Setting<Boolean> neutrals = bool("Neutrals", false);
    public Setting<Boolean> animals = bool("Animals", false);
    public Setting<Boolean> invisibles = bool("Invisibles", true);
    private Entity aimTarget;
    public BowAim() {
        super("BowAim", "Automatically aims charged bow at nearby entities",
                Category.COMBAT,true,false,false);
    }
    @Subscribe
    public void onPlayerUpdate(PlayerUpdateEvent event)
    {
        if (event.getStage() != Stage.PRE)
        {
            return;
        }
        aimTarget = null;
        if (mc.player.getMainHandStack().getItem() instanceof BowItem
                && mc.player.getItemUseTime() >= 3)
        {
            double minDist = Double.MAX_VALUE;
            for (Entity entity : mc.world.getEntities())
            {
                if (entity == null || entity == mc.player || !entity.isAlive()
                        || !isValidAimTarget(entity)
                        || MoneyStar.friendManager.isFriend(getName()))
                {
                    continue;
                }
                double dist = mc.player.distanceTo(entity);
                if (dist < minDist)
                {
                    minDist = dist;
                    aimTarget = entity;
                }
            }
            if (aimTarget instanceof LivingEntity target)
            {
                float[] rotations = getBowRotationsTo(target);
                setRotationClient(rotations[0], rotations[1]);
            }
        }
    }

    @Subscribe
    public void onLookDirection(LookDirectionEvent event)
    {
        if (aimTarget != null)
        {
            event.cancel();
        }
    }

    private float[] getBowRotationsTo(Entity entity)
    {
        float duration = (float) (mc.player.getActiveItem().getMaxUseTime(mc.player) - mc.player.getItemUseTime()) / 20.0f;
        duration = (duration * duration + duration * 2.0f) / 3.0f;
        if (duration >= 1.0f)
        {
            duration = 1.0f;
        }
        double duration1 = duration * 3.0f;
        double coeff = 0.05000000074505806;
        float pitch = (float) (-Math.toDegrees(calculateArc(entity, duration1, coeff)));
        double ix = entity.getX() - entity.prevX;
        double iz = entity.getZ() - entity.prevZ;
        double d = mc.player.distanceTo(entity);
        d -= d % 2.0;
        ix = d / 2.0 * ix * (mc.player.isSprinting() ? 1.3 : 1.1);
        iz = d / 2.0 * iz * (mc.player.isSprinting() ? 1.3 : 1.1);
        float yaw = (float) Math.toDegrees(Math.atan2(entity.getZ() + iz - mc.player.getZ(), entity.getX() + ix - mc.player.getX())) - 90.0f;
        return new float[]{yaw, pitch};
    }

    private float calculateArc(Entity target, double duration, double coeff)
    {
        double yArc = target.getY() + (double) (target.getStandingEyeHeight() / 2.0f) - (mc.player.getY() + (double) mc.player.getStandingEyeHeight());
        double dX = target.getX() - mc.player.getX();
        double dZ = target.getZ() - mc.player.getZ();
        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);
        return calculateArc(duration, coeff, dirRoot, yArc);
    }

    private float calculateArc(double duration, double coeff, double root, double yArc)
    {
        double dirCoeff = coeff * (root * root);
        yArc = 2.0 * yArc * (duration * duration);
        yArc = coeff * (dirCoeff + yArc);
        yArc = Math.sqrt(duration * duration * duration * duration - yArc);
        duration = duration * duration - yArc;
        yArc = Math.atan2(duration * duration + yArc, coeff * root);
        duration = Math.atan2(duration, coeff * root);
        return (float) Math.min(yArc, duration);
    }

    private boolean isValidAimTarget(Entity entity)
    {
        if (entity.isInvisible() && !invisibles.getValue())
        {
            return false;
        }
        return entity instanceof PlayerEntity && players.getValue()
                || EntityUtil.isMonster(entity) && monsters.getValue()
                || EntityUtil.isNeutral(entity) && neutrals.getValue()
                || EntityUtil.isPassive(entity) && animals.getValue();
    }


}
