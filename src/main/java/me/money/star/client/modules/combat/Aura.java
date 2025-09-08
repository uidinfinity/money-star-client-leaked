package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.CombatModule;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.event.impl.render.RenderWorldEvent;
import me.money.star.event.impl.world.RemoveEntityEvent;
import me.money.star.client.manager.tick.TickSync;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.player.EnchantmentUtil;
import me.money.star.util.player.PlayerUtil;
import me.money.star.util.player.RotationUtil;
import me.money.star.util.render.ColorUtil;
import me.money.star.util.render.Interpolation;
import me.money.star.util.render.RenderUtil;
import me.money.star.util.world.EntityUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.awt.*;
import java.util.Comparator;
import java.util.stream.Stream;


public class Aura extends CombatModule
{
    private static Aura INSTANCE;

    public Setting<Boolean> swing = bool("Swing", true);
    public Setting<TargetMode> mode = mode("Mode", TargetMode.SWITCH);
    public Setting<Priority> priority = mode("Priority", Priority.HEALTH);
    public Setting<Float> searchRange = num("EnemyRange", 5.0f, 1.0f, 10.0f);

    public Setting<Float> range = num("Range", 4.0f, 0f, 6.0f);
    public Setting<Float> wallRange = num("WallRange", 4.0f, 0f, 6.0f);

    public Setting<Boolean> vanillaRange = bool("VanillaRange", false);
    public Setting<Float> fov = num("FOV", 180.0f, 1f, 180.0f);
    public Setting<Boolean> attackDelay = bool("AttackDelay", true);
    public Setting<Float> attackSpeed = num("AttackSpeed", 20.0f, 1f, 20.0f);
    public Setting<Float> randomSpeed = num("RandomSpeed", 0.0f, 0f, 10.0f);
    public Setting<Float> swapDelay = num("SwapPenalty", 0.0f, 0f, 10.0f);


    public Setting<TickSync> tpsSync = mode("TPS-Sync", TickSync.NONE);
    public Setting<SwapMode> autoSwap = mode("AutoSwap", SwapMode.OFF);
    public Setting<Boolean> swordCheck = bool("Sword-Check", true);
    // ROTATE
    public Setting<Vector> hitVector = mode("HitVector", Vector.FEET);
    public Setting<Boolean> rotate = bool("Rotate", true);
    public Setting<Boolean> silentRotate = bool("RotateSilent", false);
    public Setting<Boolean> strictRotate = bool("YawStep", false);
    public Setting<Integer> rotateLimit = num("YawStep-Limit", 180, 1, 180);
    public Setting<Integer> ticksExisted = num("TicksExisted",  0, 0, 200);
    public Setting<Boolean> armorCheck = bool("ArmorCheck", true);
    public Setting<Boolean> stopSprint = bool("StopSprint", false);
    public Setting<Boolean> stopShield = bool("StopShield", false);
    public Setting<Boolean> maceBreach = bool("MaceBreach", false);
//target

    public Setting<Boolean> players = bool("Players", true);
    public Setting<Boolean> monsters = bool("Monsters", false);
    public Setting<Boolean> neutrals = bool("Neutrals", false);
    public Setting<Boolean> animals = bool("Animals", false);
    public Setting<Boolean> invisibles = bool("Invisibles", true);
    public Setting<Boolean> disableDeath = bool("DisableOnDeath", true);



    public Setting<Boolean> render = bool("render", true);
    public enum RenderMode {
        Fill,Outline
    }
    public Setting<RenderMode> renderMode = mode("Mode", RenderMode.Outline);
    public Setting<Double> line = num("LineWidth", 2.0,0.0,5.0);

    private Entity entityTarget;
    private long randomDelay = -1;

    private boolean shielding;
    private boolean sneaking;
    private boolean sprinting;

    private long lastAttackTime;
    private final Timer critTimer = new CacheTimer();
    private final Timer autoSwapTimer = new CacheTimer();
    private final Timer switchTimer = new CacheTimer();
    private boolean rotated;

    private float[] silentRotations;

    public Aura()
    {
        super("Aura", "Attacks nearby entities", Category.COMBAT,true,false,false, 700);
        INSTANCE = this;
    }

    public static Aura getInstance()
    {
        return INSTANCE;
    }



    @Override
    public void onDisable()
    {
        entityTarget = null;
        silentRotations = null;
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event)
    {
        if (disableDeath.getValue())
        {
            disable();
        }
    }

    @Subscribe
    public void onRemoveEntity(RemoveEntityEvent event)
    {
        if (disableDeath.getValue() && event.getEntity() == mc.player)
        {
            disable();
        }
    }

    @Subscribe
    public void onPlayerUpdate(PlayerTickEvent event)
    {
        //if (AutoCrystalModule.getInstance().isAttacking()
//|| AutoCrystalModule.getInstance().isPlacing()
     //           || autoSwap.getValue() == SwapMode.SILENT //&&// AutoMineModule.getInstance().isSilentSwapping()
//|| mc.player.isSpectator())
      //  {
      //      return;
///}

        if (!multitask.getValue() && checkMultitask(true))
        {
            return;
        }

        final Vec3d eyepos = MoneyStar.newPositionManager.getEyePos();
        entityTarget = switch (mode.getValue())
        {
            case SWITCH -> getAttackTarget(eyepos);
            case SINGLE ->
            {
                if (entityTarget == null || !entityTarget.isAlive()
                        || !isInAttackRange(eyepos, entityTarget))
                {
                    yield getAttackTarget(eyepos);
                }
                yield entityTarget;
            }
        };
        if (entityTarget == null || !switchTimer.passed(swapDelay.getValue() * 25.0f))
        {
            silentRotations = null;
            return;
        }
        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND
                || mc.options.attackKey.isPressed() || PlayerUtil.isHotbarKeysPressed())
        {
            autoSwapTimer.reset();
        }

        int slot = getSwordSlot();
        // END PRE
        boolean silentSwapped = false;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem) && slot != -1)
        {
            switch (autoSwap.getValue())
            {
                case NORMAL ->
                {
                    if (autoSwapTimer.passed(500))
                    {
                        MoneyStar.inventoryManager.setClientSlot(slot);
                    }
                }
                case SILENT ->
                {
                    MoneyStar.inventoryManager.setSlot(slot);
                    silentSwapped = true;
                }
            }
        }
        if (!isHoldingSword() && autoSwap.getValue() != SwapMode.SILENT)
        {
            return;
        }
        if (rotate.getValue())
        {
            float[] rotation = RotationUtil.getRotationsTo(mc.player.getEyePos(),
                    getAttackRotateVec(entityTarget));
            if (!silentRotate.getValue() && strictRotate.getValue())
            {
                float serverYaw = MoneyStar.rotationManager.getWrappedYaw();
                float diff = serverYaw - rotation[0];
                float diff1 = Math.abs(diff);
                if (diff1 > 180.0f)
                {
                    diff += diff > 0.0f ? -360.0f : 360.0f;
                }
                int dir = diff > 0.0f ? -1 : 1;
                float deltaYaw = dir * rotateLimit.getValue();
                float yaw;
                if (diff1 > rotateLimit.getValue())
                {
                    yaw = serverYaw + deltaYaw;
                    rotated = false;
                }
                else
                {
                    yaw = rotation[0];
                    rotated = true;
                }
                rotation[0] = yaw;
            }
            else
            {
                rotated = true;
            }
            // what what you cannot hop in my car
            // bentley coupe ridin with stars
            if (silentRotate.getValue())
            {
                silentRotations = rotation;
            }
            else
            {
                setRotation(rotation[0], rotation[1]);
            }
        }
        if (isRotationBlocked() || !rotated && rotate.getValue() || !isInAttackRange(eyepos, entityTarget))
        {
            MoneyStar.inventoryManager.syncToClient();
            return;
        }
        if (attackDelay.getValue())
        {
            PlayerInventory inventory = mc.player.getInventory();
            ItemStack itemStack = inventory.getStack((slot == -1 || !swordCheck.getValue()) ? mc.player.getInventory().selectedSlot : slot);

            MutableDouble attackSpeed = new MutableDouble(
                    mc.player.getAttributeBaseValue(EntityAttributes.ATTACK_SPEED));

            AttributeModifiersComponent attributeModifiers =
                    itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (attributeModifiers != null)
            {
                attributeModifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) ->
                {
                    if (entry == EntityAttributes.ATTACK_SPEED)
                    {
                        attackSpeed.add(modifier.value());
                    }
                });
            }

            double attackCooldownTicks = 1.0 / attackSpeed.getValue() * 20.0;

            int breachSlot = getBreachMaceSlot();
            if (autoSwap.getValue() != SwapMode.SILENT && maceBreach.getValue() && breachSlot != -1)
            {
                MoneyStar.inventoryManager.setSlot(breachSlot);
            }

            float ticks = 20.0f - MoneyStar.tickManager.getTickSync(tpsSync.getValue());
            float currentTime = (System.currentTimeMillis() - lastAttackTime) + (ticks * 50.0f);
            if ((currentTime / 50.0f) >= attackCooldownTicks && attackTarget(entityTarget))
            {
                lastAttackTime = System.currentTimeMillis();
            }

            if (autoSwap.getValue() != SwapMode.SILENT && maceBreach.getValue() && breachSlot != -1)
            {
                MoneyStar.inventoryManager.syncToClient();
            }
        }
        else
        {
            if (randomDelay < 0)
            {
                randomDelay = (long) RANDOM.nextFloat((randomSpeed.getValue() * 10.0f) + 1.0f);
            }
            float delay = (attackSpeed.getValue() * 50.0f) + randomDelay;

            int breachSlot = getBreachMaceSlot();
            if (autoSwap.getValue() != SwapMode.SILENT && maceBreach.getValue() && breachSlot != -1)
            {
                MoneyStar.inventoryManager.setSlot(breachSlot);
            }

            long currentTime = System.currentTimeMillis() - lastAttackTime;
            if (currentTime >= 1000.0f - delay && attackTarget(entityTarget))
            {
                randomDelay = -1;
                lastAttackTime = System.currentTimeMillis();
            }

            if (autoSwap.getValue() != SwapMode.SILENT && maceBreach.getValue() && breachSlot != -1)
            {
                MoneyStar.inventoryManager.syncToClient();
            }
        }

        if (autoSwap.getValue() == SwapMode.SILENT && silentSwapped)
        {
            MoneyStar.inventoryManager.syncToClient();
        }
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player == null)
        {
            return;
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket)
        {
            switchTimer.reset();
        }
    }

    @Subscribe
    public void onRenderWorld(RenderWorldEvent event)
    {
        //  if (AutoCrystalModule.getInstance().isAttacking()
       //         || AutoCrystalModule.getInstance().isPlacing() || mc.player.isSpectator())
      //  {
       //     return;
       // }
        if (entityTarget != null && render.getValue() && (isHoldingSword() || autoSwap.getValue() == SwapMode.SILENT)) {
            long currentTime = System.currentTimeMillis() - lastAttackTime;
            float animFactor = 1.0f - MathHelper.clamp(currentTime / 1000f, 0.0f, 1.0f);
            int attackDelay = (int) (70.0 * animFactor);
            Color color = new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 255);

            if(renderMode.getValue()== RenderMode.Outline) {
                RenderUtil.drawBox(event.getMatrices(), Interpolation.getInterpolatedEntityBox(entityTarget), Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) :color, line.getValue());
            }
            if(renderMode.getValue()== RenderMode.Fill) {
                RenderUtil.drawBoxFilled(event.getMatrices(), Interpolation.getInterpolatedEntityBox(entityTarget),Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) :new Color( Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(),75));
                RenderUtil.drawBox(event.getMatrices(), Interpolation.getInterpolatedEntityBox(entityTarget),Colors.getInstance().rainbow.getValue() ? ColorUtil.rainbow(Colors.getInstance().rainbowHue.getValue()) : color, line.getValue());
            }

        }
    }

    private boolean attackTarget(Entity entity)
    {

        preAttackTarget();

        if (silentRotate.getValue() && silentRotations != null)
        {
            setRotationSilent(silentRotations[0], silentRotations[1]);
        }

        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking());
        MoneyStar.networkManager.sendPacket(packet);
        if (swing.getValue())
        {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        else
        {
            MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        postAttackTarget(entity);

        if (silentRotate.getValue())
        {
            MoneyStar.rotationManager.setRotationSilentSync();
        }
        return true;
    }

    private int getSwordSlot()
    {
        float sharp = 0.0f;
        int slot = -1;
        // Maximize item attack damage
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem swordItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                        Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = swordItem.getRecipeRemainder().getDamage()+ sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
            else if (stack.getItem() instanceof AxeItem axeItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                        Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = axeItem.getRecipeRemainder().getDamage() + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
            else if (stack.getItem() instanceof TridentItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                        Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = TridentItem.ATTACK_DAMAGE + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
            else if (stack.getItem() instanceof MaceItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                        Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = 5.0f + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
        }
        return slot;
    }

    private int getBreachMaceSlot()
    {
        int slot = -1;
        int maxBreach = 0;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof MaceItem))
            {
                continue;
            }
            int breach = EnchantmentUtil.getLevel(stack, Enchantments.BREACH);
            if (breach > maxBreach)
            {
                slot = i;
                maxBreach = breach;
            }
        }
        return slot;
    }

    private void preAttackTarget()
    {
        final ItemStack offhand = mc.player.getOffHandStack();
        // Shield state
        shielding = false;
        if (stopShield.getValue())
        {
            shielding = offhand.getItem() == Items.SHIELD && mc.player.isBlocking();
            if (shielding)
            {
                MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                        MoneyStar.newPositionManager.getBlockPos(), Direction.getFacing(mc.player.getX(),
                        mc.player.getY(), mc.player.getZ())));
            }
        }
        sneaking = false;
        sprinting = false;
        if (stopSprint.getValue())
        {
            sneaking = MoneyStar.newPositionManager.isSneaking();
            if (sneaking)
            {
                MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            sprinting = MoneyStar.newPositionManager.isSprinting();
            if (sprinting)
            {
                MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    // RELEASE
    private void postAttackTarget(Entity entity)
    {
        if (shielding)
        {
            MoneyStar.networkManager.sendSequencedPacket(s ->
                    new PlayerInteractItemC2SPacket(Hand.OFF_HAND, s, mc.player.getYaw(), mc.player.getPitch()));
        }
        if (sneaking)
        {
            MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
        if (sprinting)
        {
            MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.START_SPRINTING));
        }

    }

    private Entity getAttackTarget(Vec3d pos)
    {
        double min = Double.MAX_VALUE;
        Entity attackTarget = null;
        for (Entity entity : mc.world.getEntities())
        {
            if (entity == null || entity == mc.player
                    || !entity.isAlive() || !isEnemy(entity)
                    || MoneyStar.friendManager.isFriend(getName())
                    || entity instanceof EndCrystalEntity
                    || entity instanceof ItemEntity
                    || entity instanceof ArrowEntity
                    || entity instanceof ExperienceBottleEntity)
            {
                continue;
            }
            if (armorCheck.getValue()
                    && entity instanceof LivingEntity livingEntity
                    && !livingEntity.getArmorItems().iterator().hasNext())
            {
                continue;
            }
            double dist = pos.distanceTo(entity.getPos());
            if (dist <= searchRange.getValue())
            {
                if (entity.age < ticksExisted.getValue())
                {
                    continue;
                }
                switch (priority.getValue())
                {
                    case DISTANCE ->
                    {
                        if (dist < min)
                        {
                            min = dist;
                            attackTarget = entity;
                        }
                    }
                    case HEALTH ->
                    {
                        if (entity instanceof LivingEntity e)
                        {
                            float health = e.getHealth() + e.getAbsorptionAmount();
                            if (health < min)
                            {
                                min = health;
                                attackTarget = entity;
                            }
                        }
                    }
                    case ARMOR ->
                    {
                        if (entity instanceof LivingEntity e)
                        {
                            float armor = getArmorDurability(e);
                            if (armor < min)
                            {
                                min = armor;
                                attackTarget = entity;
                            }
                        }
                    }
                }
            }
        }
        return attackTarget;
    }

    private float getArmorDurability(LivingEntity e)
    {
        float edmg = 0.0f;
        float emax = 0.0f;
        for (ItemStack armor : e.getArmorItems())
        {
            if (armor != null && !armor.isEmpty())
            {
                edmg += armor.getDamage();
                emax += armor.getMaxDamage();
            }
        }
        return 100.0f - edmg / emax;
    }

    public boolean isInAttackRange(Vec3d pos, Entity entity)
    {
        final Vec3d entityPos = getAttackRotateVec(entity);
        double dist = pos.distanceTo(entityPos);
        return isInAttackRange(dist, pos, entityPos);
    }

    /**
     * @param dist
     * @param pos
     * @return
     */
    public boolean isInAttackRange(double dist, Vec3d pos, Vec3d entityPos)
    {
        if (vanillaRange.getValue() && dist > 3.0f)
        {
            return false;
        }
        if (dist > range.getValue())
        {
            return false;
        }
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                pos, entityPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        if (result != null && !result.getBlockPos().equals(BlockPos.ofFloored(entityPos)) && dist > wallRange.getValue())
        {
            return false;
        }
        if (fov.getValue() != 180.0f)
        {
            float[] rots = RotationUtil.getRotationsTo(pos, entityPos);
            float diff = MathHelper.wrapDegrees(mc.player.getYaw()) - rots[0];
            float magnitude = Math.abs(diff);
            return magnitude <= fov.getValue();
        }
        return true;
    }

    public boolean isHoldingSword()
    {
        return !swordCheck.getValue() || mc.player.getMainHandStack().getItem() instanceof SwordItem
                || mc.player.getMainHandStack().getItem() instanceof AxeItem
                || mc.player.getMainHandStack().getItem() instanceof TridentItem
                || mc.player.getMainHandStack().getItem() instanceof MaceItem;
    }

    private Vec3d getAttackRotateVec(Entity entity)
    {
        Vec3d feetPos = entity.getPos();
        return switch (hitVector.getValue())
        {
            case FEET -> feetPos;
            case TORSO -> feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
            case EYES -> entity.getEyePos();
            case AUTO ->
            {
                Vec3d torsoPos = feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
                Vec3d eyesPos = entity.getEyePos();
                yield Stream.of(feetPos, torsoPos, eyesPos).min(Comparator.comparing(b -> mc.player.getEyePos().squaredDistanceTo(b))).orElse(eyesPos);
            }
        };
    }

    /**
     * Returns <tt>true</tt> if the {@link Entity} is a valid enemy to attack.
     *
     * @param e The potential enemy entity
     * @return <tt>true</tt> if the entity is an enemy
     * @see me.money.star.util.world.EntityUtil
     */
    private boolean isEnemy(Entity e)
    {
        return (!e.isInvisible() || invisibles.getValue())
                && e instanceof PlayerEntity && players.getValue()
                || EntityUtil.isMonster(e) && monsters.getValue()
                || EntityUtil.isNeutral(e) && neutrals.getValue()
                || EntityUtil.isPassive(e) && animals.getValue();
    }

    public Entity getEntityTarget()
    {
        return entityTarget;
    }

    public enum TargetMode
    {
        SWITCH,
        SINGLE
    }

    public enum SwapMode
    {
        NORMAL,
        SILENT,
        OFF
    }

    public enum Vector
    {
        EYES,
        TORSO,
        FEET,
        AUTO
    }

    public enum Priority
    {
        HEALTH,
        DISTANCE,
        ARMOR
    }
}