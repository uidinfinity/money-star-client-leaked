package me.money.star.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.block.BlockSlipperinessEvent;
import me.money.star.event.impl.block.SteppedOnSlimeBlockEvent;
import me.money.star.event.impl.entity.SlowMovementEvent;
import me.money.star.event.impl.entity.VelocityMultiplierEvent;
import me.money.star.event.impl.network.GameJoinEvent;
import me.money.star.event.impl.network.MovementSlowdownEvent;
import me.money.star.event.impl.network.SetCurrentHandEvent;
import me.money.star.mixin.accessor.AccessorKeyBinding;
import me.money.star.util.traits.Util;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;


public class NoSlow extends Module {
    public Setting<Boolean> strict = bool("Strict", false);
    public Setting<Boolean> airStrict = bool("AirStrict", false);
    public Setting<Boolean> grim = bool("Grim", false);
    public Setting<Boolean> inventoryMove = bool("InventoryMove", false);
    public Setting<Boolean> arrowMove = bool("ArrowMove", false);
    public Setting<Boolean> items = bool("Items", false);
    public Setting<Boolean> shields = bool("Shields", false);
    public Setting<Boolean> webs = bool("Webs", false);
    public Setting<Boolean> berryBush = bool("BerryBush", false);
    public Setting<Float> webSpeed = num("WebSpeed ", 3.5f, 0f, 20.0f);
    public Setting<Boolean> soulsand = bool("SoulSand", false);
    public Setting<Boolean> honeyblock = bool("HoneyBlock", false);
    public Setting<Boolean> slimeblock = bool("SlimeBlock", false);












    private boolean sneaking;
    //

    /**
     *
     */
    public NoSlow() {
        super("NoSlow", "Prevents items from slowing down player", Category.MOVEMENT,true,false,false);
    }
    @Override
    public void onDisable() {
        if (airStrict.getValue() && sneaking) {
            MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(Util.mc.player,
                    ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
        sneaking = false;
        MoneyStar.tickManager.setClientTick(1.0f);
    }

    @Subscribe
    public void onGameJoin(GameJoinEvent event) {
        onEnable();
    }

    @Subscribe
    public void onSetCurrentHand(SetCurrentHandEvent event) {
        if (airStrict.getValue() && !sneaking && checkSlowed()) {
            sneaking = true;
            MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(Util.mc.player,
                    ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }
/*
    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (event.getStage() == EventStage.PRE && grimConfig.getValue()
                && mc.player.isUsingItem() && !mc.player.isSneaking() && itemsConfig.getValue()) {
            ItemStack offHandStack = mc.player.getOffHandStack();
            if (mc.player.getActiveHand() == Hand.OFF_HAND) {
                Managers.INVENTORY.setSlotForced(mc.player.getInventory().selectedSlot % 8 + 1);
                Managers.INVENTORY.syncToClient();
            } else if (!offHandStack.() && offHandStack.getItem() != Items.BOW && offHandStack.getItem() != Items.CROSSBOW && offHandStack.getItem() != Items.SHIELD) {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(id, Hand.OFF_HAND));
            }
        }
    }


 */
    @Subscribe
    public void onTick(TickEvent event) {
        if (event.getStage() == Stage.PRE) {
            if (airStrict.getValue() && sneaking
                    && !Util.mc.player.isUsingItem()) {
                sneaking = false;
                MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(Util.mc.player,
                        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }

            if (inventoryMove.getValue() && checkScreen()) {
                final long handle = Util.mc.getWindow().getHandle();
                KeyBinding[] keys = new KeyBinding[]{Util.mc.options.jumpKey, Util.mc.options.forwardKey, Util.mc.options.backKey, Util.mc.options.rightKey, Util.mc.options.leftKey};
                for (KeyBinding binding : keys) {
                    binding.setPressed(InputUtil.isKeyPressed(handle, ((AccessorKeyBinding) binding).getBoundKey().getCode()));
                }
                if (arrowMove.getValue()) {
                    float yaw = Util.mc.player.getYaw();
                    float pitch = Util.mc.player.getPitch();
                    if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_UP)) {
                        pitch -= 3.0f;
                    } else if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_DOWN)) {
                        pitch += 3.0f;
                    } else if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT)) {
                        yaw -= 3.0f;
                    } else if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT)) {
                        yaw += 3.0f;
                    }
                    Util.mc.player.setYaw(yaw);
                    Util.mc.player.setPitch(MathHelper.clamp(pitch, -90.0f, 90.0f));
                }
            }
            if (grim.getValue() && (webs.getValue() || berryBush.getValue())) {
                for (BlockPos pos : getIntersectingWebs()) {
                    MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.DOWN));
                    MoneyStar.networkManager.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
                }
            }
        }
    }

    @Subscribe
    public void onSlowMovement(SlowMovementEvent event) {
        Block block = event.getState().getBlock();
        if (block instanceof CobwebBlock && webs.getValue()
                || block instanceof SweetBerryBushBlock && berryBush.getValue()) {
            if (grim.getValue()) {
                event.cancel();
            } else if (Util.mc.player.isOnGround()) {
                MoneyStar.tickManager.setClientTick(1.0f);
            } else {
                MoneyStar.tickManager.setClientTick(webSpeed.getValue() / 2.0f);
            }
        }
    }

    @Subscribe
    public void onMovementSlowdown(MovementSlowdownEvent event) {
        if (checkSlowed()) {
            event.input.movementForward *= 5.0f;
            event.input.movementSideways *= 5.0f;
        }
    }

    @Subscribe
    public void onVelocityMultiplier(VelocityMultiplierEvent event) {
        if (event.getBlock() == Blocks.SOUL_SAND && soulsand.getValue()
                || event.getBlock() == Blocks.HONEY_BLOCK && honeyblock.getValue()) {
            event.cancel();
        }
    }

    @Subscribe
    public void onSteppedOnSlimeBlock(SteppedOnSlimeBlockEvent event) {
        if (slimeblock.getValue()) {
            event.cancel();
        }
    }

    @Subscribe
    public void onBlockSlipperiness(BlockSlipperinessEvent event) {
        if (event.getBlock() == Blocks.SLIME_BLOCK
                && slimeblock.getValue()) {
            event.cancel();
            event.setSlipperiness(0.6f);
        }
    }

    @Subscribe
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (Util.mc.player == null || Util.mc.world == null || Util.mc.isInSingleplayer()) {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesPosition()
                && strict.getValue() && checkSlowed()) {
            // Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(0));
            // Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id));
            MoneyStar.inventoryManager.setSlotForced(Util.mc.player.getInventory().selectedSlot);
        } else if (event.getPacket() instanceof ClickSlotC2SPacket && strict.getValue()) {
            if (Util.mc.player.isUsingItem()) {
                Util.mc.player.stopUsingItem();
            }
            if (sneaking || MoneyStar.newPositionManager.isSneaking()) {
                MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(Util.mc.player,
                        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            if (MoneyStar.newPositionManager.isSprinting()) {
                MoneyStar.networkManager.sendPacket(new ClientCommandC2SPacket(Util.mc.player,
                        ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    public boolean checkSlowed() {
//        ItemStack offHandStack = mc.player.getOffHandStack();
//        if ((offHandStack.isFood() || offHandStack.getItem() == Items.BOW || offHandStack.getItem() == Items.CROSSBOW || offHandStack.getItem() == Items.SHIELD) && grimConfig.getValue()) {
//            return false;
//        }
        return !Util.mc.player.isRiding() && !Util.mc.player.isSneaking() && (Util.mc.player.isUsingItem() && items.getValue() || Util.mc.player.isBlocking() && shields.getValue() && !grim.getValue());
    }

    public boolean checkScreen() {
        return Util.mc.currentScreen != null && !(Util.mc.currentScreen instanceof ChatScreen
                || Util.mc.currentScreen instanceof SignEditScreen || Util.mc.currentScreen instanceof DeathScreen);
    }

    public List<BlockPos> getIntersectingWebs() {
        int radius = 5;
        final List<BlockPos> blocks = new ArrayList<>();
        for (int x = radius; x > -radius; --x) {
            for (int y = radius; y > -radius; --y) {
                for (int z = radius; z > -radius; --z) {
                    BlockPos blockPos = BlockPos.ofFloored(Util.mc.player.getX() + x,
                            Util.mc.player.getY() + y, Util.mc.player.getZ() + z);
                    BlockState state = Util.mc.world.getBlockState(blockPos);
                    if (state.getBlock() instanceof CobwebBlock && webs.getValue()
                            || state.getBlock() instanceof SweetBerryBushBlock && berryBush.getValue()) {
                        blocks.add(blockPos);
                    }
                }
            }
        }
        return blocks;
    }


}
