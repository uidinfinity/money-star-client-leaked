package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.CombatModule;
import me.money.star.client.manager.RenderManager;
import me.money.star.client.manager.tick.TickSync;
import me.money.star.client.modules.client.Colors;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.RunTickEvent;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.event.impl.render.RenderWorldEvent;
import me.money.star.event.impl.world.AddEntityEvent;
import me.money.star.event.impl.world.RemoveEntityEvent;
import me.money.star.util.EvictingQueue;
import me.money.star.util.math.PerSecondCounter;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.player.*;
import me.money.star.util.render.ColorUtil;
import me.money.star.util.render.Interpolation;
import me.money.star.util.render.RenderBuffers;
import me.money.star.util.render.RenderUtil;
import me.money.star.util.render.animation.Animation;
import me.money.star.util.world.BlastResistantBlocks;
import me.money.star.util.world.EntityUtil;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import java.awt.*;
import java.util.Comparator;
import java.util.stream.Stream;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

public class AutoCrystal extends CombatModule
{
    private static AutoCrystal INSTANCE;

    public Setting<Boolean> whileMining = bool("WhileMining", false);
    public Setting<Float> targetRange = num("EnemyRange", 10.0f, 1f, 13.0f);
    public Setting<Boolean> instant = bool("Instant", false);
    public Setting<Sequential> sequential = mode("Sequential", Sequential.NONE);
    public Setting<Boolean> idPredict = bool("BreakPredict", false);
    public Setting<Boolean> instantCalc = bool("Instant-Calc", false);
    public Setting<Float> instantDamage = num("InstantDamage", 6.0f, 1.0f, 10.0f);
    public Setting<Boolean> instantMax = bool("InstantMax", false);
    public Setting<Boolean>  raytraceS = bool("Raytrace", false);
    public Setting<Boolean>  swing = bool("Swing", true);












    // ROTATE SETTINGS
    public Setting<Boolean> rotate = bool("Rotate", true);
    public Setting<Rotate> strictRotate = mode("YawStep", Rotate.OFF);
    public Setting<Integer> rotateLimit = num("YawStep-Limit", 180, 1, 180);







    // TARGET

    public Setting<Boolean> players = bool("Players", true);
    public Setting<Boolean> monsters = bool("Monsters", false);
    public Setting<Boolean> neutrals = bool("Neutrals", false);
    public Setting<Boolean> animals = bool("Animals", false);
    public Setting<Boolean> shulkers = bool("Shulkers", true);

    // BREAK SETTINGS

    public Setting<Float> breakSpeed = num("BreakSpeed", 20.0f, 0.1f, 20.0f);
    public Setting<Float> attackDelay = num("AttackDelay", 0.0f, 0.0f, 5.0f);
    public Setting<Integer> attackFactorS = num("AttackFactor", 0, 0, 3);
    public Setting<Float> attackLimit = num("AttackLimit", 0.5f, 0.5f, 20.0f);
    public Setting<Boolean> breakDelayS = bool("BreakDelay", false);
    public Setting<Float> breakTimeout = num("BreakTimeout", 3.0f, 0f, 10.0f);
    public Setting<Float> minTimeout = num("MinTimeout", 5.0f, 0f, 20.0f);
    public Setting<Integer> ticksExisted = num("TicksExisted", 0, 0, 10);
    public Setting<Float> breakRangeS = num("BreakRange", 4.0f, 0.1f, 6.0f);
    public Setting<Float> maxYOffset = num("MinTimeout", 5.0f, 0f, 20.0f);
    public Setting<Float> breakWallRangeS = num("BreakWallRange", 4.0f, 0.1f, 6.0f);
    public Setting<Swap>  antiWeakness = mode("AntiWeakness",  Swap.OFF);
    public Setting<Float> swapDelay = num("SwapPenalty", 0.0f, 0.0f, 10.0f);


    // PLACE SETTINGS

    public Setting<Boolean> inhibit = bool("Inhibit", true);
    public Setting<Boolean> place = bool("Place", true);
    public Setting<Float> placeSpeed = num("PlaceSpeed", 20.0f, 0.1f, 20.0f);
    public Setting<Float> placeRangeS = num("PlaceRange", 4.0f, 0.1f, 6.0f);

    public Setting<Float> placeWallRangeS = num("PlaceWallRange", 4.0f, 0.1f, 6.0f);
    public Setting<Boolean> placeRangeEye = bool("PlaceRangeEye", false);
    public Setting<Boolean> placeRangeCenter = bool("PlaceRangeCenter", true);
    public Setting<Swap> autoSwap = mode("Swap", Swap.OFF);
    public Setting<Boolean> antiSurroundS = bool("AntiSurround", false);
    public Setting<ForcePlace> forcePlace = mode("PreventReplace",  ForcePlace.NONE);
    public Setting<Boolean> breakValid = bool("Strict", false);
    public Setting<Boolean> strictDirection = bool("StrictDirection", false);
    public Setting<Placements> placements = mode("Placements",  Placements.NATIVE);

    // Damage settings
    public Setting<Float> minDamage = num("MinDamage", 4.0f, 1.0f, 10.0f);
    public Setting<Float> maxLocalDamage = num("MaxLocalDamage", 12.0f, 4.0f, 20.0f);
    public Setting<Boolean>  assumeArmor = bool("AssumeBestArmor", false);
    public Setting<Boolean> armorBreaker = bool("ArmorBreaker", true);
    public Setting<Float>  armorScale = num("ArmorScale", 5.0f, 1.0f, 20.0f);
    public Setting<Float> lethalMultiplier = num("LethalMultiplier",  1.5f, 0.0f, 4.0f);
    public Setting<Boolean>   antiTotem = bool("Lethal-Totem", false);
    public Setting<Boolean> lethalDamage = bool("Lethal-DamageTick", true);
    public Setting<Boolean>  safety = bool("Safety", false);
    public Setting<Boolean> safetyOverride = bool("SafetyOverride", true);
    public Setting<Boolean>  blockDestruction = bool("BlockDestruction", false);
    public Setting<Boolean> selfExtrapolate = bool("SelfExtrapolate", true);
    public Setting<Integer>  extrapolateTicks = num("ExtrapolationTicks",  0, 0, 10);
    public Setting<Boolean>  disableDeath = bool("DisableOnDeath", false);

    // Render settings
    public Setting<Boolean>  render = bool("SelfExtrapolate", true);
    public Setting<Integer>  fadeTime = num("Fade-Time",  250, 0, 1000);
    public Setting<Boolean>  debug = bool("Debug", true);
    public Setting<Boolean>  debugDamage = bool("Debug-Damage", true);





    private DamageData<EndCrystalEntity> attackCrystal;
    private DamageData<BlockPos> placeCrystal;
    //
    private BlockPos renderPos;
    private double renderDamage;
    private BlockPos renderSpawnPos;
    //
    private Vec3d crystalRotation;
    private boolean attackRotate;
    private boolean rotated;
    private float[] silentRotations;
    private float calculatePlaceCrystalTime = 0;
    //
    private static final Box FULL_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 2.0, 1.0);
    private static final Box HALF_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private final CacheTimer lastAttackTimer = new CacheTimer();
    private final Timer lastPlaceTimer = new CacheTimer();
    private final Timer lastSwapTimer = new CacheTimer();
    private final Timer autoSwapTimer = new CacheTimer();
    // default NCP 
    // fight.speed: limit: 13
    // shortterm: ticks: 8
    // limitforseconds: half: 8, one: 15, two: 30, four: 60, eight: 100
    private final Deque<Long> attackLatency = new EvictingQueue<>(20);
    private final Map<Integer, Long> attackPackets =
            Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final Map<BlockPos, Long> placePackets =
            Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final PerSecondCounter crystalCounter = new PerSecondCounter();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private long predictId;
    // Antistuck
    private final Map<Integer, Integer> antiStuckCrystals = new HashMap<>();
    private final List<AntiStuckData> stuckCrystals = new CopyOnWriteArrayList<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public AutoCrystal()
    {
        super("AutoCrystal", "Attacks nearby entities", Category.COMBAT,true,false,false, 750);
        INSTANCE = this;
    }

    public static AutoCrystal getInstance()
    {
        return INSTANCE;
    }



    @Override
    public void onDisable()
    {
        renderPos = null;
        attackCrystal = null;
        placeCrystal = null;
        crystalRotation = null;
        silentRotations = null;
        calculatePlaceCrystalTime = 0;
        stuckCrystals.clear();
        attackPackets.clear();
        antiStuckCrystals.clear();
        placePackets.clear();
        attackLatency.clear();
        fadeList.clear();

    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event)
    {
        if (disableDeath.getValue())
        {
            disable();
        }
        else
        {
            onDisable();
        }
    }

    @Subscribe
    public void onPlayerUpdate(PlayerTickEvent event)
    {
        if (mc.player.isSpectator() || isSilentSwap(autoSwap.getValue()) && AutoMine.getInstance().isSilentSwapping()
        )
        {
            return;
        }

        for (AntiStuckData d : stuckCrystals)
        {
            double dist = mc.player.squaredDistanceTo(d.pos());
            double diff = d.stuckDist() - dist;
            if (diff > 0.5)
            {
                stuckCrystals.remove(d);
            }
        }

        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND
                || mc.options.attackKey.isPressed() || PlayerUtil.isHotbarKeysPressed())
        {
            autoSwapTimer.reset();
        }
        renderPos = null;
        ArrayList<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        List<BlockPos> blocks = getSphere(placeRangeEye.getValue() ? mc.player.getEyePos() : mc.player.getPos());
        long timePre = System.nanoTime();
        if (place.getValue())
        {
            placeCrystal = calculatePlaceCrystal(blocks, entities);
        }
        attackCrystal = calculateAttackCrystal(entities);
        if (attackCrystal == null)
        {
            if (placeCrystal != null)
            {
                EndCrystalEntity crystalEntity = intersectingCrystalCheck(placeCrystal.getDamageData());
                if (crystalEntity != null)
                {
                    double self = ExplosionUtil.getDamageTo(mc.player, crystalEntity.getPos(),
                            blockDestruction.getValue(), selfExtrapolate.getValue() ? extrapolateTicks.getValue() : 0, false);
                    if (!safety.getValue() || !playerDamageCheck(self))
                    {
                        attackCrystal = new DamageData<>(crystalEntity, placeCrystal.getAttackTarget(),
                                placeCrystal.getDamage(), self, crystalEntity.getBlockPos().down(), false);
                    }
                }
            }
            calculatePlaceCrystalTime = System.nanoTime() - timePre;
        }

        if (inhibit.getValue() && attackCrystal != null
                && attackPackets.containsKey(attackCrystal.getDamageData().getId()))
        {
            float delay;
            if (attackDelay.getValue() > 0.0)
            {
                float attackFactor = 50.0f / Math.max(1.0f, attackFactorS.getValue());
                delay = attackDelay.getValue() * attackFactor;
            }
            else
            {
                delay = 1000.0f - breakSpeed.getValue() * 50.0f;
            }
            lastAttackTimer.setDelay(delay + 100.0f);
            attackPackets.remove(attackCrystal.getDamageData().getId());
        }

        float breakDelay = getBreakDelay();
        if (breakDelayS.getValue())
        {
            breakDelay = Math.max(minTimeout.getValue() * 50.0f, getBreakMs() + breakTimeout.getValue() * 50.0f);
        }
        attackRotate = attackCrystal != null && attackDelay.getValue() <= 0.0 && lastAttackTimer.passed(breakDelay);
        if (attackCrystal != null)
        {
            crystalRotation = attackCrystal.damageData.getPos();
        }
        else if (placeCrystal != null)
        {
            crystalRotation = placeCrystal.damageData.toCenterPos().add(0.0, 0.5, 0.0);
        }
        if (rotate.getValue() && crystalRotation != null && (placeCrystal == null || canHoldCrystal()))
        {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), crystalRotation);
            if (strictRotate.getValue() == Rotate.FULL || strictRotate.getValue() == Rotate.SEMI && attackRotate)
            {
                float yaw;
                float serverYaw = MoneyStar.rotationManager.getWrappedYaw();
                float diff = serverYaw - rotations[0];
                float diff1 = Math.abs(diff);
                if (diff1 > 180.0f)
                {
                    diff += diff > 0.0f ? -360.0f : 360.0f;
                }
                int dir = diff > 0.0f ? -1 : 1;
                float deltaYaw = dir * rotateLimit.getValue();
                if (diff1 > rotateLimit.getValue())
                {
                    yaw = serverYaw + deltaYaw;
                    rotated = false;
                }
                else
                {
                    yaw = rotations[0];
                    rotated = true;
                    crystalRotation = null;
                }
                rotations[0] = yaw;
            }
            else
            {
                rotated = true;
                crystalRotation = null;
            }
            setRotation(rotations[0], rotations[1]);
        }
        else
        {
            silentRotations = null;
        }
        if (isRotationBlocked() || !rotated && rotate.getValue())
        {
            return;
        }
//        if (rotateSilent.getValue() && silentRotations != null) {
//            setRotationSilent(silentRotations[0], silentRotations[1]);
//        }
        final Hand hand = getCrystalHand();
        if (attackCrystal != null)
        {
            // ChatUtil.clientSendMessage("yaw: " + rotations[0] + ", pitch: " + rotations[1]);
            if (attackRotate)
            {
                // ChatUtil.clientSendMessage("break range:" + Math.sqrt(mc.player.getEyePos().squaredDistanceTo(attackCrystal.getDamageData().getPos())));
                attackCrystal(attackCrystal.getDamageData(), hand);
                setStage("ATTACKING");
                lastAttackTimer.reset();
            }
        }
        boolean placeRotate = lastPlaceTimer.passed(1000.0f - placeSpeed.getValue() * 50.0f);
        if (placeCrystal != null)
        {
            renderPos = placeCrystal.getDamageData();
            renderDamage = placeCrystal.getDamage();
            if (placeRotate)
            {
                // ChatUtil.clientSendMessage("place range:" + Math.sqrt(mc.player.getEyePos().squaredDistanceTo(placeCrystal.getDamageData().toCenterPos())));
                placeCrystal(placeCrystal.getDamageData(), hand);
                setStage("PLACING");
                lastPlaceTimer.reset();
            }
        }
    }

    @Subscribe
    public void onRunTick(RunTickEvent event)
    {
        if (mc.player == null)
        {
            return;
        }
        final Hand hand = getCrystalHand();
        if (attackDelay.getValue() > 0.0)
        {
            float attackFactor = 50.0f / Math.max(1.0f, attackFactorS.getValue());
            if (attackCrystal != null && lastAttackTimer.passed(attackDelay.getValue() * attackFactor))
            {
                attackCrystal(attackCrystal.getDamageData(), hand);
                lastAttackTimer.reset();
            }
        }
    }

    @Subscribe
    public void onRenderWorld(RenderWorldEvent event)
    {        Color color = new Color(Colors.getInstance().red.getValue(), Colors.getInstance().green.getValue(), Colors.getInstance().blue.getValue(), 255);

        if (render.getValue())
        {
            RenderBuffers.preRender();
            BlockPos renderPos1 = null;
            double factor = 0.0f;
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                if (set.getKey() == renderPos)
                {
                    continue;
                }

                if (set.getValue().getFactor() > factor)
                {
                    renderPos1 = set.getKey();
                    factor = set.getValue().getFactor();
                }

                set.getValue().setState(false);
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (100 * set.getValue().getFactor());
               // Color boxColor = ColorsModule.getInstance().getColor(boxAlpha);
              //  Color lineColor = ColorsModule.getInstance().getColor(lineAlpha);
               // RenderManager.renderBox(event.getMatrices(), set.getKey(), boxColor.getRGB());
             //   RenderManager.renderBoundingBox(event.getMatrices(), set.getKey(), 1.5f, lineColor.getRGB());
            }

            if (debugDamage.getValue() && renderPos1 != null)
            {
                RenderManager.renderSign(String.format("%.1f", renderDamage),
                        renderPos1.toCenterPos(), new Color(255, 255, 255, (int) (255.0f * factor)).getRGB());
            }

            RenderBuffers.postRender();

            fadeList.entrySet().removeIf(e ->
                    e.getValue().getFactor() == 0.0);

            if (renderPos != null && isHoldingCrystal())
            {
                Animation animation = new Animation(true, fadeTime.getValue());
                fadeList.put(renderPos, animation);
            }
        }
    }
    @Subscribe
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                handleServerPackets(packet1);
            }
        }
        else
        {
            handleServerPackets(event.getPacket());
        }
    }

    private void handleServerPackets(Packet<?> serverPacket)
    {
        if (serverPacket instanceof ExplosionS2CPacket packet)
        {
            for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
            {
                if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(packet.center().getX(), packet.center().getY(), packet.center().getZ()) < 144.0)
                {
                    mc.executeSync(() -> mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED));
                    antiStuckCrystals.remove(entity.getId());
                    Long attackTime = attackPackets.remove(entity.getId());
                    if (attackTime != null)
                    {
                        attackLatency.add(System.currentTimeMillis() - attackTime);
                    }
                }
            }
        }

        if (serverPacket instanceof PlaySoundS2CPacket packet)
        {
            if (packet.getSound().value() == SoundEvents.ENTITY_GENERIC_EXPLODE.value() && packet.getCategory() == SoundCategory.BLOCKS)
            {
                for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
                {
                    if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) < 144.0)
                    {
                        mc.executeSync(() -> mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED));
                        antiStuckCrystals.remove(entity.getId());
                        Long attackTime = attackPackets.remove(entity.getId());
                        if (attackTime != null)
                        {
                            attackLatency.add(System.currentTimeMillis() - attackTime);
                        }
                    }
                }
            }
        }

        if (serverPacket instanceof EntitiesDestroyS2CPacket packet)
        {
            for (int id : packet.getEntityIds())
            {
                antiStuckCrystals.remove(id);
                Long attackTime = attackPackets.remove(id);
                if (attackTime != null)
                {
                    attackLatency.add(System.currentTimeMillis() - attackTime);
                }
            }
        }

        if (serverPacket instanceof ExperienceOrbSpawnS2CPacket packet && packet.getEntityId() > predictId)
        {
            predictId = packet.getEntityId();
        }

        if (serverPacket instanceof EntitySpawnS2CPacket packet && packet.getEntityId() > predictId)
        {
            predictId = packet.getEntityId();
        }
    }

    @Subscribe
    public void onAddEntity(AddEntityEvent event)
    {
        if (!(event.getEntity() instanceof EndCrystalEntity crystalEntity))
        {
            return;
        }
        Vec3d crystalPos = crystalEntity.getPos();
        BlockPos blockPos = BlockPos.ofFloored(crystalPos.add(0.0, -1.0, 0.0));
        renderSpawnPos = blockPos;
        Long time = placePackets.remove(blockPos);
        attackRotate = time != null;
        if (attackRotate)
        {
            crystalCounter.updateCounter();
        }
        if (!instant.getValue())
        {
            return;
        }
        if (attackRotate)
        {
            final Hand hand = getCrystalHand();
            attackInternal(crystalEntity, hand);
            setStage("ATTACKING");
            lastAttackTimer.reset();
            if (sequential.getValue() == Sequential.NORMAL)
            {
                placeSequentialCrystal(hand);
            }
        }
        else if (instantCalc.getValue())
        {
            if (attackRangeCheck(crystalPos))
            {
                return;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystalPos,
                    blockDestruction.getValue(), selfExtrapolate.getValue() ? extrapolateTicks.getValue() : 0, false);
            if (playerDamageCheck(selfDamage))
            {
                return;
            }
            for (Entity entity : mc.world.getEntities())
            {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || MoneyStar.friendManager.isFriend(entity.getName().getString()))
                {
                    continue;
                }
                double crystalDist = crystalPos.squaredDistanceTo(entity.getPos());
                if (crystalDist > 144.0f)
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRange.getValue() * targetRange.getValue())
                {
                    continue;
                }

                double damage = ExplosionUtil.getDamageTo(entity, crystalPos, blockDestruction.getValue(),
                        extrapolateTicks.getValue(), assumeArmor.getValue());
                // TODO: Test this
                DamageData<EndCrystalEntity> data = new DamageData<>(crystalEntity,
                        entity, damage, selfDamage, crystalEntity.getBlockPos().down(), false);
                attackRotate = damage > instantDamage.getValue() || attackCrystal != null
                        && damage >= attackCrystal.getDamage() && instantMax.getValue()
                        || entity instanceof LivingEntity entity1 && isCrystalLethalTo(data, entity1);
                if (attackRotate)
                {
                    final Hand hand = getCrystalHand();
                    attackInternal(crystalEntity, hand);
                    setStage("ATTACKING");
                    lastAttackTimer.reset();
                    if (sequential.getValue() == Sequential.NORMAL)
                    {
                        placeSequentialCrystal(hand);
                    }
                    break;
                }
            }
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
            lastSwapTimer.reset();
        }
    }

    public boolean isAttacking()
    {
        return attackCrystal != null;
    }

    public boolean isPlacing()
    {
        return placeCrystal != null && isHoldingCrystal();
    }

    public void attackCrystal(EndCrystalEntity entity, Hand hand)
    {
        if (attackCheckPre(hand))
        {
            return;
        }
        StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);
        if (weakness != null && (strength == null || weakness.getAmplifier() > strength.getAmplifier()))
        {
            int slot = -1;
            for (int i = 0; i < 9; ++i)
            {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && (stack.getItem() instanceof SwordItem
                        || stack.getItem() instanceof AxeItem
                        || stack.getItem() instanceof PickaxeItem))
                {
                    slot = i;
                    break;
                }
            }
            if (slot != -1)
            {
                boolean canSwap = slot != MoneyStar.inventoryManager.getServerSlot() && (antiWeakness.getValue() != Swap.NORMAL || autoSwapTimer.passed(500));
                if (antiWeakness.getValue() != Swap.OFF && canSwap)
                {
                    if (antiWeakness.getValue() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                slot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (antiWeakness.getValue() == Swap.SILENT)
                    {
                        MoneyStar.inventoryManager.setSlot(slot);
                    }
                    else
                    {
                        MoneyStar.inventoryManager.setClientSlot(slot);
                    }
                }
                attackInternal(entity, Hand.MAIN_HAND);
                if (canSwap)
                {
                    if (antiWeakness.getValue() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                slot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (antiWeakness.getValue() == Swap.SILENT)
                    {
                        MoneyStar.inventoryManager.syncToClient();
                    }
                }

                if (sequential.getValue() == Sequential.STRICT)
                {
                    placeSequentialCrystal(hand);
                }
            }
        }
        else
        {
            attackInternal(entity, hand);
            if (sequential.getValue() == Sequential.STRICT)
            {
                placeSequentialCrystal(hand);
            }
        }
    }

    private void attackInternal(EndCrystalEntity crystalEntity, Hand hand)
    {
        attackInternal(crystalEntity.getId(), hand);
    }

    private void attackInternal(int crystalEntity, Hand hand)
    {
        hand = hand != null ? hand : Hand.MAIN_HAND;
        EndCrystalEntity entity2 = new EndCrystalEntity(mc.world, 0.0, 0.0, 0.0);
        entity2.setId(crystalEntity);
        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity2, mc.player.isSneaking());
        MoneyStar.networkManager.sendPacket(packet);
        if (swing.getValue())
        {
            mc.player.swingHand(hand);
        }
        else
        {
            MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(hand));
        }

        attackPackets.put(crystalEntity, System.currentTimeMillis());
        Integer antiStuckCount = antiStuckCrystals.get(crystalEntity);
        if (antiStuckCount != null)
        {
            antiStuckCrystals.replace(crystalEntity, antiStuckCount + 1);
        }
        else
        {
            antiStuckCrystals.put(crystalEntity, 1);
        }
    }

    private void placeSequentialCrystal(Hand hand)
    {
        if (placeCrystal == null)
        {
            return;
        }
       // int latency = FastLatencyModule.getInstance().isEnabled() ? (int)
         //       FastLatencyModule.getInstance().getLatency() : MoneyStar.networkManager.getClientLatency();
       // if (!MoneyStar.networkManager.is2b2t() || latency >= 50)
       // {
       //     placeCrystal(placeCrystal.getBlockPos(), hand);
       // }
    }

    private void placeCrystal(BlockPos blockPos, Hand hand)
    {
        if (isRotationBlocked() || !rotated && rotate.getValue())
        {
            return;
        }

        placeCrystal(blockPos, hand, true);
    }

    public void placeCrystal(BlockPos blockPos, Hand hand, boolean checkPlacement)
    {
        if (checkPlacement && checkCanUseCrystal())
        {
            return;
        }
        Direction sidePlace = getPlaceDirection(blockPos);
        // Vec3d vec3d = mc.player.getCameraPosVec(1.0f);
        // Vec3d vec3d1 = RotationUtil.getRotationVector();
        // Vec3d vec3d3 = vec3d.add(vec3d1.x * placeRange.getValue(),
        //        vec3d1.y * placeRange.getValue(), vec3d1.z * placeRange.getValue());
        // HitResult hitResult = mc.world.raycast(new RaycastContext(vec3d, vec3d3,
        //        RaycastContext.ShapeType.OUTLINE,
        //        RaycastContext.FluidHandling.NONE, mc.player));
        BlockHitResult result = new BlockHitResult(blockPos.toCenterPos(), sidePlace, blockPos, false);
        if (autoSwap.getValue() != Swap.OFF && hand != Hand.OFF_HAND && getCrystalHand() == null)
        {
            if (isSilentSwap(autoSwap.getValue()) && InventoryUtil.count(Items.END_CRYSTAL) == 0)
            {
                return;
            }
            int crystalSlot = getCrystalSlot();
            if (crystalSlot != -1)
            {
                boolean canSwap = crystalSlot != MoneyStar.inventoryManager.getServerSlot() && (autoSwap.getValue() != Swap.NORMAL || autoSwapTimer.passed(500));
                if (canSwap)
                {
                    if (autoSwap.getValue() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                crystalSlot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (autoSwap.getValue() == Swap.SILENT)
                    {
                        MoneyStar.inventoryManager.setSlot(crystalSlot);
                    }
                    else
                    {
                        MoneyStar.inventoryManager.setClientSlot(crystalSlot);
                    }
                }
                placeInternal(result, Hand.MAIN_HAND);
                placePackets.put(blockPos, System.currentTimeMillis());
                if (canSwap)
                {
                    if (autoSwap.getValue() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                crystalSlot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (autoSwap.getValue() == Swap.SILENT)
                    {
                        MoneyStar.inventoryManager.syncToClient();
                    }
                }
            }
        }
        else if (isHoldingCrystal())
        {
            placeInternal(result, hand);
            placePackets.put(blockPos, System.currentTimeMillis());
        }
    }

    private void placeInternal(BlockHitResult result, Hand hand)
    {
        if (hand == null)
        {
            return;
        }
        MoneyStar.networkManager.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        if (swing.getValue())
        {
            mc.player.swingHand(hand);
        }
        else
        {
            MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(hand));
        }

        // Entity ID predict
        if (idPredict.getValue())
        {
            boolean flag = AutoExp.getInstance().isEnabled() || mc.player.isUsingItem() && mc.player.getStackInHand(mc.player.getActiveHand()).getItem() instanceof ExperienceBottleItem;
            int id = (int) (predictId + 1);
            if (flag || attackPackets.containsKey(id))
            {
                return;
            }
            Entity entity = mc.world.getEntityById(id);
            if (entity != null && !(entity instanceof EndCrystalEntity))
            {
                return;
            }
            EndCrystalEntity entity2 = new EndCrystalEntity(mc.world, 0.0, 0.0, 0.0);
            entity2.setId(id);
            PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity2, false);
            MoneyStar.networkManager.sendPacket(packet);
            MoneyStar.networkManager.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            attackPackets.put(id, System.currentTimeMillis());
        }
    }

    private boolean isSilentSwap(Swap swap)
    {
        return swap == Swap.SILENT || swap == Swap.SILENT_ALT;
    }

    private int getCrystalSlot()
    {
        int slot = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof EndCrystalItem)
            {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private Direction getPlaceDirection(BlockPos blockPos)
    {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        if (strictDirection.getValue())
        {
            if (mc.player.getY() >= blockPos.getY())
            {
                return Direction.UP;
            }
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                    mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE, mc.player));
            if (result != null && result.getType() == HitResult.Type.BLOCK)
            {
                return result.getSide();
            }
        }
        else
        {
            if (mc.world.isInBuildLimit(blockPos))
            {
                return Direction.DOWN;
            }
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                    mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE, mc.player));
            if (result != null && result.getType() == HitResult.Type.BLOCK)
            {
                return result.getSide();
            }
        }
        return Direction.UP;
    }

    private DamageData<EndCrystalEntity> calculateAttackCrystal(List<Entity> entities)
    {
        if (entities.isEmpty())
        {
            return null;
        }

        final List<DamageData<EndCrystalEntity>> validData = new ArrayList<>();

        DamageData<EndCrystalEntity> data = null;
        for (Entity crystal : entities)
        {
            if (!(crystal instanceof EndCrystalEntity crystal1) || !crystal.isAlive()
                    || stuckCrystals.stream().anyMatch(d -> d.id() == crystal.getId()))
            {
                continue;
            }
            Long time = attackPackets.get(crystal.getId());
            boolean attacked = time != null && time < getBreakMs();
            if ((crystal.age < ticksExisted.getValue() || attacked) && inhibit.getValue())
            {
                continue;
            }
            if (attackRangeCheck(crystal1))
            {
                continue;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystal.getPos(),
                    blockDestruction.getValue(), selfExtrapolate.getValue() ? extrapolateTicks.getValue() : 0, false);
            boolean unsafeToPlayer = playerDamageCheck(selfDamage);
            if (unsafeToPlayer && !safetyOverride.getValue())
            {
                continue;
            }
            for (Entity entity : entities)
            {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || MoneyStar.friendManager.isFriend(entity.getName().getString()))
                {
                    continue;
                }
                double crystalDist = crystal.squaredDistanceTo(entity);
                if (crystalDist > 144.0f)
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRange.getValue() * targetRange.getValue())
                {
                    continue;
                }

                boolean antiSurround = false;
                if (antiSurroundS.getValue() && entity instanceof PlayerEntity player
                        && !BlastResistantBlocks.isUnbreakable(player.getBlockPos()))
                {
                    Set<BlockPos> miningPositions = new HashSet<>();
                    BlockPos miningBlock = AutoMine.getInstance().getMiningBlock();
                    if (AutoMine.getInstance().isEnabled() && miningBlock != null)
                    {
                        miningPositions.add(miningBlock);
                    }
                    if (MoneyStar.blockManager.getMines(0.75f).contains(player.getBlockPos().up()))
                    {
                        miningPositions.add(player.getBlockPos().up());
                    }
                    for (BlockPos miningBlockPos : miningPositions)
                    {
                        if (!AutoFeetPlace.getInstance().getSurroundNoDown(player).contains(miningBlockPos))
                        {
                            continue;
                        }
                        for (Direction direction : Direction.values())
                        {
                            BlockPos pos1 = miningBlockPos.offset(direction);
                            if (crystal.getBlockPos().equals(pos1.down()))
                            {
                                antiSurround = true;
                            }
                        }
                    }
                }

                double damage = ExplosionUtil.getDamageTo(entity, crystal.getPos(), blockDestruction.getValue(),
                        extrapolateTicks.getValue(), assumeArmor.getValue());
                if (checkOverrideSafety(unsafeToPlayer, damage, entity))
                {
                    continue;
                }

                DamageData<EndCrystalEntity> currentData = new DamageData<>(crystal1, entity,
                        damage, selfDamage, crystal1.getBlockPos().down(), antiSurround);
                validData.add(currentData);
                if (data == null || damage > data.getDamage())
                {
                    data = currentData;
                }
            }
        }
        if (data == null || targetDamageCheck(data))
        {
            if (antiSurroundS.getValue())
            {
                return validData.stream()
                        .filter(DamageData::isAntiSurround)
                        .min(Comparator.comparingDouble(d -> mc.player.squaredDistanceTo(d.getBlockPos().toCenterPos())))
                        .orElse(null);
            }
            return null;
        }
        return data;
    }

    private boolean attackRangeCheck(EndCrystalEntity entity)
    {
        return attackRangeCheck(entity.getPos());
    }

    /**
     * @param entityPos
     * @return
     */
    private boolean attackRangeCheck(Vec3d entityPos)
    {
        double breakRange = breakRangeS.getValue();
        double breakWallRange = breakWallRangeS.getValue();
        Vec3d playerPos = mc.player.getEyePos();
        double dist = playerPos.squaredDistanceTo(entityPos);
        if (dist > breakRange * breakRange)
        {
            return true;
        }
        double yOff = Math.abs(entityPos.getY() - mc.player.getY());
        if (yOff > maxYOffset.getValue())
        {
            return true;
        }
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                playerPos, entityPos, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        return result.getType() != HitResult.Type.MISS && dist > breakWallRange * breakWallRange;
    }

    private DamageData<BlockPos> calculatePlaceCrystal(List<BlockPos> placeBlocks, List<Entity> entities)
    {
        if (placeBlocks.isEmpty() || entities.isEmpty())
        {
            return null;
        }

        final List<DamageData<BlockPos>> validData = new ArrayList<>();

        DamageData<BlockPos> data = null;
        for (BlockPos pos : placeBlocks)
        {
            if (!canUseCrystalOnBlock(pos) || placeRangeCheck(pos) || intersectingAntiStuckCheck(pos))
            {
                continue;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystalDamageVec(pos),
                    blockDestruction.getValue(), selfExtrapolate.getValue() ? extrapolateTicks.getValue() : 0, false);
            boolean unsafeToPlayer = playerDamageCheck(selfDamage);
            if (unsafeToPlayer && !safetyOverride.getValue())
            {
                continue;
            }
            for (Entity entity : entities)
            {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || MoneyStar.friendManager.isFriend(entity.getName().getString()))
                {
                    continue;
                }
                double blockDist = pos.getSquaredDistance(entity.getPos());
                if (blockDist > 144.0f)
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRange.getValue() * targetRange.getValue())
                {
                    continue;
                }

                boolean antiSurround = false;
                if (antiSurroundS.getValue() && entity instanceof PlayerEntity player
                        && !BlastResistantBlocks.isUnbreakable(player.getBlockPos()))
                {
                    Set<BlockPos> miningPositions = new HashSet<>();
                    BlockPos miningBlock = AutoMine.getInstance().getMiningBlock();
                    if (AutoMine.getInstance().isEnabled() && miningBlock != null)
                    {
                        miningPositions.add(miningBlock);
                    }
                    if (MoneyStar.blockManager.getMines(0.75f).contains(player.getBlockPos().up()))
                    {
                        miningPositions.add(player.getBlockPos().up());
                    }
                    for (BlockPos miningBlockPos : miningPositions)
                    {
                        if (!AutoFeetPlace.getInstance().getSurroundNoDown(player).contains(miningBlockPos))
                        {
                            continue;
                        }
                        for (Direction direction : Direction.values())
                        {
                            BlockPos pos1 = miningBlockPos.offset(direction);
                            if (pos.equals(pos1.down()))
                            {
                                antiSurround = true;
                            }
                        }
                    }
                }

                double damage;
                damage = ExplosionUtil.getDamageTo(entity, crystalDamageVec(pos), blockDestruction.getValue(),
                        extrapolateTicks.getValue(), assumeArmor.getValue());
                if (checkOverrideSafety(unsafeToPlayer, damage, entity))
                {
                    continue;
                }

                DamageData<BlockPos> currentData = new DamageData<>(pos, entity,
                        damage, selfDamage, antiSurround);
                validData.add(currentData);
                if (data == null || damage > data.getDamage())
                {
                    data = currentData;
                }
            }
        }
        if (data == null || targetDamageCheck(data))
        {
            if (antiSurroundS.getValue())
            {
                return validData.stream()
                        .filter(DamageData::isAntiSurround)
                        .min(Comparator.comparingDouble(d -> mc.player.squaredDistanceTo(d.getBlockPos().toCenterPos())))
                        .orElse(null);
            }
            return null;
        }
        return data;
    }

    /**
     * @param pos
     * @return
     */
    private boolean placeRangeCheck(BlockPos pos)
    {
        double placeRange = placeRangeS.getValue();
        double placeWallRange = placeWallRangeS.getValue();
        Vec3d player = placeRangeEye.getValue() ? mc.player.getEyePos() : mc.player.getPos();
        double dist = placeRangeCenter.getValue() ?
                player.squaredDistanceTo(pos.toCenterPos()) : pos.getSquaredDistance(player.x, player.y, player.z);
        if (dist > placeRange * placeRange)
        {
            return true;
        }
        Vec3d raytrace = Vec3d.of(pos).add(0.5, 2.70000004768372, 0.5);
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                mc.player.getEyePos(), raytrace,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        float maxDist = breakRangeS.getValue() * breakRangeS.getValue();
        if (result != null && result.getType() == HitResult.Type.BLOCK && !result.getBlockPos().equals(pos))
        {
            maxDist = breakWallRangeS.getValue() * breakWallRangeS.getValue();
            if (!raytraceS.getValue() || dist > placeWallRange * placeWallRange)
            {
                return true;
            }
        }
        return breakValid.getValue() && dist > maxDist;
    }

    public void placeCrystalForTarget(PlayerEntity target, BlockPos blockPos)
    {
        if (target == null || target.isDead() || placeRangeCheck(blockPos) || !canUseCrystalOnBlock(blockPos))
        {
            return;
        }
        double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystalDamageVec(blockPos),
                blockDestruction.getValue(), Set.of(blockPos), selfExtrapolate.getValue() ? extrapolateTicks.getValue() : 0, false);
        if (playerDamageCheck(selfDamage))
        {
            return;
        }
        double damage = ExplosionUtil.getDamageTo(target, crystalDamageVec(blockPos), blockDestruction.getValue(),
                Set.of(blockPos), extrapolateTicks.getValue(), assumeArmor.getValue());
        if (damage < minDamage.getValue() && !isCrystalLethalTo(damage, target)
                || placeCrystal != null && placeCrystal.getDamage() >= damage)
        {
            return;
        }

        float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), blockPos.toCenterPos());
        setRotation(rotations[0], rotations[1]);
        placeCrystal(blockPos, Hand.MAIN_HAND, false);
        fadeList.put(blockPos, new Animation(true, fadeTime.getValue()));
    }

    private boolean checkOverrideSafety(boolean unsafeToPlayer, double damage, Entity entity)
    {
        return safetyOverride.getValue() && unsafeToPlayer && damage < EntityUtil.getHealth(entity) + 0.5;
    }

    private boolean targetDamageCheck(DamageData<?> crystal)
    {
        double minDmg = minDamage.getValue();
        if (crystal.getAttackTarget() instanceof LivingEntity entity && isCrystalLethalTo(crystal, entity))
        {
            minDmg = 2.0f;
        }
        return crystal.getDamage() < minDmg;
    }

    private boolean playerDamageCheck(double playerDamage)
    {
        if (!mc.player.isCreative())
        {
            float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            if (safety.getValue() && playerDamage >= health + 0.5f)
            {
                return true;
            }
            return playerDamage > maxLocalDamage.getValue();
        }
        return false;
    }

    private boolean isFeetSurrounded(LivingEntity entity)
    {
        BlockPos pos1 = entity.getBlockPos();
        if (!mc.world.getBlockState(pos1).isReplaceable())
        {
            return true;
        }
        for (Direction direction : Direction.values())
        {
            if (!direction.getAxis().isHorizontal())
            {
                continue;
            }
            BlockPos pos2 = pos1.offset(direction);
            if (mc.world.getBlockState(pos2).isReplaceable())
            {
                return false;
            }
        }
        return true;
    }

    private boolean checkAntiTotem(double damage, LivingEntity entity)
    {
        if (entity instanceof PlayerEntity p)
        {
            float phealth = EntityUtil.getHealth(p);
            if (phealth <= 2.0f && phealth - damage < 0.5f)
            {
                long time = MoneyStar.totemManager.getLastPopTime(p);
                if (time != -1)
                {
                    return System.currentTimeMillis() - time <= 500;
                }
            }
        }
        return false;
    }

    private boolean isCrystalLethalTo(DamageData<?> crystal, LivingEntity entity)
    {
        return isCrystalLethalTo(crystal.getDamage(), entity);
    }

    private boolean isCrystalLethalTo(double damage, LivingEntity entity)
    {
        if (lethalDamage.getValue() && lastAttackTimer.passed(500))
        {
            return true;
        }

        if (antiTotem.getValue() && checkAntiTotem(damage, entity))
        {
            return true;
        }
        float health = entity.getHealth() + entity.getAbsorptionAmount();
        if (damage * (1.0f + lethalMultiplier.getValue()) >= health + 0.5f)
        {
            return true;
        }
        if (armorBreaker.getValue())
        {
            for (ItemStack armorStack : entity.getArmorItems())
            {
                int n = armorStack.getDamage();
                int n1 = armorStack.getMaxDamage();
                float durability = ((n1 - n) / (float) n1) * 100.0f;
                if (durability < armorScale.getValue())
                {
                    return true;
                }
            }
        }

        // Antiregear
        if (shulkers.getValue() && entity instanceof PlayerEntity)
        {
            for (BlockPos pos : getSphere(3.0f, entity.getPos()))
            {
                BlockState state = mc.world.getBlockState(pos);
                if (state.getBlock() instanceof ShulkerBoxBlock)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attackCheckPre(Hand hand)
    {
        if (!lastSwapTimer.passed(swapDelay.getValue() * 25.0f))
        {
            return true;
        }
        if (hand == Hand.MAIN_HAND)
        {
            return checkCanUseCrystal();
        }
        return false;
    }

    private boolean checkCanUseCrystal()
    {
        return !multitask.getValue() && checkMultitask()
                || !whileMining.getValue() && mc.interactionManager.isBreakingBlock();
    }

    private boolean isHoldingCrystal()
    {
        if (!checkCanUseCrystal() && (autoSwap.getValue() == Swap.SILENT || autoSwap.getValue() == Swap.SILENT_ALT))
        {
            return true;
        }
        return getCrystalHand() != null;
    }

    private Vec3d crystalDamageVec(BlockPos pos)
    {
        return Vec3d.of(pos).add(0.5, 1.0, 0.5);
    }

    /**
     * Returns <tt>true</tt> if the {@link Entity} is a valid enemy to attack.
     *
     * @param e The potential enemy entity
     * @return <tt>true</tt> if the entity is an enemy
     */
    private boolean isValidTarget(Entity e)
    {
        return e instanceof PlayerEntity && players.getValue()
                || EntityUtil.isMonster(e) && monsters.getValue()
                || EntityUtil.isNeutral(e) && neutrals.getValue()
                || EntityUtil.isPassive(e) && animals.getValue();
    }

    /**
     * Returns <tt>true</tt> if an {@link EndCrystalItem} can be used on the
     * param {@link BlockPos}.
     *
     * @param pos The block pos
     * @return Returns <tt>true</tt> if the crystal item can be placed on the
     * block
     */
    public boolean canUseCrystalOnBlock(BlockPos pos)
    {
        BlockState state = mc.world.getBlockState(pos);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))
        {
            return false;
        }
        return isCrystalHitboxClear(pos);
    }

    public boolean isCrystalHitboxClear(BlockPos pos)
    {
        BlockPos p2 = pos.up();
        BlockState state2 = mc.world.getBlockState(p2);
        // ver 1.12.2 and below
        if (placements.getValue() == Placements.PROTOCOL && !mc.world.isAir(p2.up()))
        {
            return false;
        }
        if (!mc.world.isAir(p2) && !state2.isOf(Blocks.FIRE))
        {
            return false;
        }
        else
        {
            final Box bb = MoneyStar.networkManager.isCrystalPvpCC() ? HALF_CRYSTAL_BB : FULL_CRYSTAL_BB;
            double d = p2.getX();
            double e = p2.getY();
            double f = p2.getZ();
            List<Entity> list = getEntitiesBlockingCrystal(new Box(d, e, f,
                    d + bb.maxX, e + bb.maxY, f + bb.maxZ));
            return list.isEmpty();
        }
    }

    private List<Entity> getEntitiesBlockingCrystal(Box box)
    {
        List<Entity> entities = new CopyOnWriteArrayList<>(
                mc.world.getOtherEntities(null, box));
        //
        for (Entity entity : entities)
        {
            if (entity == null || !entity.isAlive()
                    || entity instanceof ExperienceOrbEntity
                    || forcePlace.getValue() != ForcePlace.NONE
                    && entity instanceof ItemEntity && entity.age <= 10)
            {
                entities.remove(entity);
            }
            else if (entity instanceof EndCrystalEntity entity1
                    && entity1.getBoundingBox().intersects(box))
            {
                Integer antiStuckAttacks = antiStuckCrystals.get(entity1.getId());
                if (!attackRangeCheck(entity1) && (antiStuckAttacks == null || antiStuckAttacks <= attackLimit.getValue() * 10.0f))
                {
                    entities.remove(entity);
                }
                else
                {
                    double dist = mc.player.squaredDistanceTo(entity1);
                    stuckCrystals.add(new AntiStuckData(entity1.getId(), entity1.getBlockPos(), entity1.getPos(), dist));
                }
            }
        }
        return entities;
    }

    private boolean intersectingAntiStuckCheck(BlockPos blockPos)
    {
        if (stuckCrystals.isEmpty())
        {
            return false;
        }
        return stuckCrystals.stream().anyMatch(d -> d.blockPos().equals(blockPos.up()));
    }

    private EndCrystalEntity intersectingCrystalCheck(BlockPos pos)
    {
        return (EndCrystalEntity) mc.world.getOtherEntities(null, new Box(pos)).stream()
                .filter(e -> e instanceof EndCrystalEntity).min(Comparator.comparingDouble(e -> mc.player.distanceTo(e))).orElse(null);
    }

    private List<BlockPos> getSphere(Vec3d origin)
    {
        double rad = Math.ceil(placeRangeS.getValue());
        return getSphere(rad, origin);
    }

    private List<BlockPos> getSphere(double rad, Vec3d origin)
    {
        List<BlockPos> sphere = new ArrayList<>();
        for (double x = -rad; x <= rad; ++x)
        {
            for (double y = -rad; y <= rad; ++y)
            {
                for (double z = -rad; z <= rad; ++z)
                {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                            (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }

    private boolean canHoldCrystal()
    {
        return isHoldingCrystal() || autoSwap.getValue() != Swap.OFF && getCrystalSlot() != -1;
    }

    private Hand getCrystalHand()
    {
        final ItemStack offhand = mc.player.getOffHandStack();
        final ItemStack mainhand = mc.player.getMainHandStack();
        if (offhand.getItem() instanceof EndCrystalItem)
        {
            return Hand.OFF_HAND;
        }
        else if (mainhand.getItem() instanceof EndCrystalItem)
        {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    public float getBreakDelay()
    {
        return 1000.0f - breakSpeed.getValue() * 50.0f;
    }

    // Debug info
    public void setStage(String crystalStage)
    {
        // this.crystalStage = crystalStage;
    }

    public int getBreakMs()
    {
        if (attackLatency.isEmpty())
        {
            return 0;
        }
        float avg = 0.0f;
        // fix ConcurrentModificationException
        ArrayList<Long> latencyCopy = Lists.newArrayList(attackLatency);
        if (!latencyCopy.isEmpty())
        {
            for (float t : latencyCopy)
            {
                avg += t;
            }
            avg /= latencyCopy.size();
        }
        return (int) avg;
    }

    public boolean shouldPreForcePlace()
    {
        return forcePlace.getValue() == ForcePlace.PRE;
    }

    public float getPlaceRange()
    {
        return placeRangeS.getValue();
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }

    public enum Sequential
    {
        NORMAL,
        STRICT,
        NONE
    }

    public enum ForcePlace
    {
        PRE,
        POST,
        NONE
    }

    public enum Placements
    {
        NATIVE,
        PROTOCOL
    }

    public enum Rotate
    {
        FULL,
        SEMI,
        OFF
    }

    private record AntiStuckData(int id, BlockPos blockPos, Vec3d pos, double stuckDist) {}

    private static class DamageData<T>
    {
        //
        private final List<String> tags = new ArrayList<>();
        private T damageData;
        private Entity attackTarget;
        private BlockPos blockPos;
        //
        private double damage, selfDamage;
        private boolean antiSurround;

        //
        public DamageData()
        {

        }

        public DamageData(BlockPos damageData, Entity attackTarget, double damage,
                          double selfDamage, boolean antiSurround)
        {
            this.damageData = (T) damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
            this.blockPos = damageData;
            this.antiSurround = antiSurround;
        }

        public DamageData(T damageData, Entity attackTarget, double damage,
                          double selfDamage, BlockPos blockPos, boolean antiSurround)
        {
            this.damageData = damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
            this.blockPos = blockPos;
            this.antiSurround = antiSurround;
        }

        public void setDamageData(T damageData, Entity attackTarget, double damage, double selfDamage)
        {
            this.damageData = damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
        }

        public T getDamageData()
        {
            return damageData;
        }

        public Entity getAttackTarget()
        {
            return attackTarget;
        }

        public double getDamage()
        {
            return damage;
        }

        public double getSelfDamage()
        {
            return selfDamage;
        }

        public BlockPos getBlockPos()
        {
            return blockPos;
        }

        public boolean isAntiSurround()
        {
            return antiSurround;
        }
    }

    private class AttackCrystalTask implements Callable<DamageData<EndCrystalEntity>>
    {
        private final List<Entity> threadSafeEntities;

        public AttackCrystalTask(List<Entity> threadSafeEntities)
        {
            this.threadSafeEntities = threadSafeEntities;
        }

        @Override
        public DamageData<EndCrystalEntity> call() throws Exception
        {
            return calculateAttackCrystal(threadSafeEntities);
        }
    }

    private class PlaceCrystalTask implements Callable<DamageData<BlockPos>>
    {
        private final List<BlockPos> threadSafeBlocks;
        private final List<Entity> threadSafeEntities;

        public PlaceCrystalTask(List<BlockPos> threadSafeBlocks,
                                List<Entity> threadSafeEntities)
        {
            this.threadSafeBlocks = threadSafeBlocks;
            this.threadSafeEntities = threadSafeEntities;
        }

        @Override
        public DamageData<BlockPos> call() throws Exception
        {
            return calculatePlaceCrystal(threadSafeBlocks, threadSafeEntities);
        }
    }
}