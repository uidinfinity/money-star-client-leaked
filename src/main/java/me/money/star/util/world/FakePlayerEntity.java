package me.money.star.util.world;

import com.mojang.authlib.GameProfile;
import me.money.star.util.traits.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author linus
 * @since 1.0
 */
public class FakePlayerEntity extends OtherClientPlayerEntity implements Util {
    //
    public static final AtomicInteger CURRENT_ID = new AtomicInteger(1000000);
    //
    private final PlayerEntity player;

    /**
     * @param player
     * @param name
     */
    public FakePlayerEntity(PlayerEntity player, String name) {
        super(MinecraftClient.getInstance().world,
                new GameProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"), name));
        this.player = player;
        copyPositionAndRotation(player);
        prevYaw = getYaw();
        prevPitch = getPitch();
        headYaw = player.headYaw;
        prevHeadYaw = headYaw;
        bodyYaw = player.bodyYaw;
        prevBodyYaw = bodyYaw;
        Byte playerModel = player.getDataTracker()
                .get(PlayerEntity.PLAYER_MODEL_PARTS);
        dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);
        getAttributes().setFrom(player.getAttributes());
        setPose(player.getPose());
        setHealth(player.getHealth());
        setAbsorptionAmount(player.getAbsorptionAmount());
        // setBoundingBox(player.getBoundingBox());
        getInventory().clone(player.getInventory());
        setId(CURRENT_ID.incrementAndGet());
        this.age = 100;
    }

    /**
     * @param player
     * @param profile
     */
    public FakePlayerEntity(PlayerEntity player, GameProfile profile) {
        super(MinecraftClient.getInstance().world, profile);
        this.player = player;
        copyPositionAndRotation(player);
        prevYaw = getYaw();
        prevPitch = getPitch();
        headYaw = player.headYaw;
        prevHeadYaw = headYaw;
        bodyYaw = player.bodyYaw;
        prevBodyYaw = bodyYaw;
        Byte playerModel = player.getDataTracker()
                .get(PlayerEntity.PLAYER_MODEL_PARTS);
        dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);
        getAttributes().setFrom(player.getAttributes());
        setPose(player.getPose());
        setHealth(player.getHealth());
        setAbsorptionAmount(player.getAbsorptionAmount());
        // setBoundingBox(player.getBoundingBox());
        getInventory().clone(player.getInventory());
        setId(CURRENT_ID.incrementAndGet());
        this.age = 100;
    }

    /**
     * @param player
     */
    public FakePlayerEntity(PlayerEntity player) {
        this(player, player.getName().getString());
    }

    /**
     *
     */
    public void spawnPlayer() {
        if (mc.world != null) {
            unsetRemoved();
            mc.world.addEntity(this);
        }
    }

    /**
     *
     */
    public void despawnPlayer() {
        if (mc.world != null) {
            mc.world.removeEntity(getId(), RemovalReason.DISCARDED);
            setRemoved(RemovalReason.DISCARDED);
        }
    }

    /**
     * @return
     */
    @Override
    public boolean isDead() {
        return false;
    }

    /**
     * @return
     */
    public PlayerEntity getPlayer() {
        return player;
    }
}
