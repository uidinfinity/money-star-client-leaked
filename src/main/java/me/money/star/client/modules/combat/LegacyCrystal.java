package me.money.star.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.ObsidianPlacerModule;
import me.money.star.client.gui.modules.RotationModule;
import me.money.star.client.modules.world.AirPlace;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.DisconnectEvent;
import me.money.star.event.impl.network.PacketEvent;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.event.impl.world.AddEntityEvent;
import me.money.star.event.impl.world.RemoveEntityEvent;
import me.money.star.util.math.position.PositionUtil;
import me.money.star.util.player.RotationUtil;
import me.money.star.util.world.BlastResistantBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.util.*;


public class LegacyCrystal extends RotationModule
{
    private static LegacyCrystal INSTANCE;
    public Setting<Boolean> rotate  = bool("Rotate", false);
    public Setting<Boolean> randomRotate  = bool("Rotate-Jitter", true);;
    public Setting<Float> breakDelayButton= num("Spawn-Delay", 0.0f, 0.0f, 20.0f);
    public Setting<Float> randomDelayButton = num("Random-Delay",  0.0f, 0.0f, 5.0f);

    private final Set<BlockPos> placedCrystals = new HashSet<>();
    private final Map<EndCrystalEntity, Long> spawnedCrystals = new LinkedHashMap<>();
    private float randomDelay = -1;

    public LegacyCrystal()
    {
        super("LegacyCrystal", "Automatically breaks placed crystals", Category.COMBAT,true,false,false, 950);
        INSTANCE = this;
    }

    public static LegacyCrystal getInstance()
    {
        return INSTANCE;
    }
    

    @Subscribe
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (spawnedCrystals.isEmpty())
        {
            return;
        }
        Map.Entry<EndCrystalEntity, Long> e = spawnedCrystals.entrySet().iterator().next();
        EndCrystalEntity crystalEntity = e.getKey();
        Long time = e.getValue();
        if (randomDelay == -1)
        {
            randomDelay = randomDelayButton.getValue() == 0.0f ? 0.0f : RANDOM.nextFloat(randomDelayButton.getValue() * 25.0f);
        }
        float breakDelay = breakDelayButton.getValue() * 50.0f + randomDelay;
        double dist = mc.player.getEyePos().squaredDistanceTo(crystalEntity.getPos());
        if (dist <= 12.25 && System.currentTimeMillis() - time >= breakDelay)
        {
            if (rotate.getValue())
            {
                Vec3d rotatePos = crystalEntity.getPos();
                if (randomRotate.getValue())
                {
                    Box bb = crystalEntity.getBoundingBox();
                    rotatePos = new Vec3d(RANDOM.nextDouble(bb.minX, bb.maxX), RANDOM.nextDouble(bb.minY, bb.maxY), RANDOM.nextDouble(bb.minZ, bb.maxZ));
                }
                float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), rotatePos);
                setRotation(rotations[0], rotations[1]);
            }
            MoneyStar.networkManager.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
            mc.player.swingHand(Hand.MAIN_HAND);
            randomDelay = -1;
        }
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && !event.isClientPacket() && mc.player.getStackInHand(packet.getHand()).getItem() instanceof EndCrystalItem)
        {
            placedCrystals.add(packet.getBlockHitResult().getBlockPos());
        }
    }

    @Subscribe
    public void onAddEntity(AddEntityEvent event)
    {
        if (event.getEntity() instanceof EndCrystalEntity crystalEntity)
        {
            BlockPos base = crystalEntity.getBlockPos().down();
            if (placedCrystals.contains(base))
            {
                spawnedCrystals.put(crystalEntity, System.currentTimeMillis());
                placedCrystals.remove(base);
            }
        }
    }

    @Subscribe
    public void onRemoveEntity(RemoveEntityEvent event)
    {
        if (event.getEntity() instanceof EndCrystalEntity crystalEntity)
        {
            spawnedCrystals.remove(crystalEntity);
        }
    }
}