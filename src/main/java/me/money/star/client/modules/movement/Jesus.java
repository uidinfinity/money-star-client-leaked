package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.entity.player.PlayerJumpEvent;
import me.money.star.event.impl.network.PlayerUpdateEvent;
import me.money.star.event.impl.world.BlockCollisionEvent;
import me.money.star.mixin.accessor.AccessorKeyBinding;
import me.money.star.mixin.accessor.AccessorPlayerMoveC2SPacket;
import me.money.star.util.traits.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShapes;


public class Jesus extends Module {
    //
    public Setting<JesusMode> mode = mode("Mode", JesusMode.SOLID);
    public Setting<Boolean> strict = bool("Strict", false);
    //
    private int floatTimer = 1000;
    private boolean fluidState;
    //
    private double floatOffset;

    /**
     *
     */
    public Jesus() {
        super("Jesus", "Allow player to walk on water", Category.MOVEMENT,true,false,false);
    }



    @Override
    public void onDisable() {
        floatOffset = 0.0;
        //
        floatTimer = 1000;
        KeyBinding.setKeyPressed(((AccessorKeyBinding) Util.mc.options.jumpKey).getBoundKey(), false);
    }

    @Subscribe
    public void onBlockCollision(BlockCollisionEvent event) {
        BlockState state = event.getState();
        if (MoneyStar.moduleManager.getModuleByClass(Flight.class).isEnabled()
                || Util.mc.player.isSpectator() || Util.mc.player.isOnFire()
                || state.getFluidState().isEmpty()) {
            return;
        }
        if (mode.getValue() != JesusMode.DOLPHIN
                && ((state.getBlock() == Blocks.WATER
                | state.getFluidState().getFluid() == Fluids.WATER)
                || state.getBlock() == Blocks.LAVA)) {
            event.cancel();
            event.setVoxelShape(VoxelShapes.cuboid(new Box(0.0, 0.0, 0.0, 1.0, 0.99, 1.0)));
            if (Util.mc.player.getVehicle() != null) {
                event.setVoxelShape(VoxelShapes.cuboid(new Box(0.0, 0.0, 0.0, 1.0,
                        0.949999988079071, 1.0)));
            } else if (mode.getValue() == JesusMode.TRAMPOLINE) {
                event.setVoxelShape(VoxelShapes.cuboid(new Box(0.0, 0.0, 0.0, 1.0, 0.96, 1.0)));
            }
        }
    }

    @Subscribe
    public void onPlayerJump(PlayerJumpEvent event) {
        if (!isInFluid() && isOnFluid()) {
            event.cancel();
        }
    }

    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() == Stage.PRE) {
            if (MoneyStar.moduleManager.getModuleByClass(Flight.class).isEnabled()){
                return;
            }
            if (mode.getValue() == JesusMode.SOLID) {
                if (isInFluid() || Util.mc.player.fallDistance > 3.0f
                        || Util.mc.player.isSneaking()) {
                    // floatOffset = 0.0;
                }
                if (!Util.mc.options.sneakKey.isPressed() && !Util.mc.options.jumpKey.isPressed()) {
                    if (isInFluid()) {
                        floatTimer = 0;
                       MoneyStar.movementManager.setMotionY(0.11);
                        return;
                    }
                    if (floatTimer == 0) {
                        MoneyStar.movementManager.setMotionY(0.30);
                    } else if (floatTimer == 1) {
                        MoneyStar.movementManager.setMotionY(0.0);
                    }
                    floatTimer++;
                }
            } else if (mode.getValue() == JesusMode.DOLPHIN && isInFluid()
                    && !Util.mc.options.sneakKey.isPressed() && !Util.mc.options.jumpKey.isPressed()) {
                KeyBinding.setKeyPressed(((AccessorKeyBinding) Util.mc.options.jumpKey).getBoundKey(), true);
            }
        }
    }

    @Subscribe
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (MoneyStar.moduleManager.getModuleByClass(Flight.class).isEnabled() ) {
            return;
        }
        if (event.getStage() == Stage.PRE
                && mode.getValue() == JesusMode.TRAMPOLINE) {
            boolean inFluid = getFluidBlockInBB(Util.mc.player.getBoundingBox()) != null;
            if (inFluid && !Util.mc.player.isSneaking()) {
                Util.mc.player.setOnGround(false);
            }
            Block block = Util.mc.world.getBlockState(new BlockPos((int) Math.floor(Util.mc.player.getX()),
                    (int) Math.floor(Util.mc.player.getY()),
                    (int) Math.floor(Util.mc.player.getZ()))).getBlock();
            if (fluidState && !Util.mc.player.getAbilities().flying && !Util.mc.player.isTouchingWater()) {
                if (Util.mc.player.getVelocity().y < -0.3 || Util.mc.player.isOnGround()
                        || Util.mc.player.isHoldingOntoLadder()) {
                    fluidState = false;
                    return;
                }
                MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y / 0.9800000190734863 + 0.08);
                MoneyStar.movementManager.setMotionY(Util.mc.player.getVelocity().y - 0.03120000000005);
            }
            if (isInFluid()) {
                MoneyStar.movementManager.setMotionY(0.1);
                fluidState = false;
                return;
            }
            if (!isInFluid() && block instanceof FluidBlock
                    && Util.mc.player.getVelocity().y < 0.2) {
                MoneyStar.movementManager.setMotionY(strict.getValue() ? 0.184 : 0.5);
                fluidState = true;
            }
        }
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (event.isClientPacket() || Util.mc.player == null || Util.mc.getNetworkHandler() == null
                || Util.mc.player.age <= 20 || MoneyStar.moduleManager.getModuleByClass(Flight.class).isEnabled()) {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet
                && packet.changesPosition()
                && mode.getValue() == JesusMode.SOLID && !isInFluid()
                && isOnFluid() && Util.mc.player.fallDistance <= 3.0f) {
            double y = packet.getY(Util.mc.player.getY());
            if (!strict.getValue()) {
                floatOffset = Util.mc.player.age % 2 == 0 ? 0.0 : 0.05;
            }
            ((AccessorPlayerMoveC2SPacket) packet).hookSetY(y - floatOffset);
            if (strict.getValue()) {
                floatOffset += 0.12;
                if (floatOffset > 0.4) {
                    floatOffset = 0.2;
                }
            }
        }
    }

    public boolean isInFluid() {
        return Util.mc.player.isTouchingWater() || Util.mc.player.isInLava();
    }

    public BlockState getFluidBlockInBB(Box box) {
        return getFluidBlockInBB(MathHelper.floor(box.minY - 0.2));
    }

    public BlockState getFluidBlockInBB(int minY) {
        for (int i = MathHelper.floor(Util.mc.player.getBoundingBox().minX); i < MathHelper.ceil(Util.mc.player.getBoundingBox().maxX); i++) {
            for (int j = MathHelper.floor(Util.mc.player.getBoundingBox().minZ); j < MathHelper.ceil(Util.mc.player.getBoundingBox().maxZ); j++) {
                BlockState state = Util.mc.world.getBlockState(new BlockPos(i, minY, j));
                if (state.getBlock() instanceof FluidBlock) {
                    return state;
                }
            }
        }
        return null;
    }

    public boolean isOnFluid() {
        if (Util.mc.player.fallDistance >= 3.0f) {
            return false;
        }
        final Box bb = Util.mc.player.getVehicle() != null ?
                Util.mc.player.getVehicle().getBoundingBox().contract(0.0, 0.0, 0.0)
                        .offset(0.0, -0.05000000074505806, 0.0) :
                Util.mc.player.getBoundingBox().contract(0.0, 0.0, 0.0).offset(0.0,
                        -0.05000000074505806, 0.0);
        boolean onLiquid = false;
        int y = (int) bb.minY;
        for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0); x++) {
            for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0); z++) {
                final Block block = Util.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != Blocks.AIR) {
                    if (!(block instanceof FluidBlock)) {
                        return false;
                    }
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }

    public enum JesusMode {
        SOLID,
        DOLPHIN,
        TRAMPOLINE
    }
}
