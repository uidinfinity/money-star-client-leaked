package me.money.star.client.modules.world;

import com.google.common.eventbus.Subscribe;
import me.money.star.MoneyStar;
import me.money.star.client.gui.modules.BlockPlacerModule;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.util.math.position.PositionUtil;
import me.money.star.util.player.MovementUtil;
import me.money.star.util.player.RotationUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class Scaffold extends BlockPlacerModule
{

    public Setting<BlockPicker> picker = mode("Mode", BlockPicker.NORMAL);
    public Setting<Boolean> rotateHold = bool("rotateHold", false);
    public Setting<Boolean> grim = bool("grim", false);
    public Setting<Boolean> grimNew = bool("grimNew", false);
    public Setting<Boolean>  keepY = bool("keepY", false);
    public Setting<Boolean>  tower = bool("tower", false);


    private BlockData blockData;
    private BlockData renderData;
    private float[] lastAngles;
    private int groundPosY;
    public Scaffold()
    {
        super("Scaffold", "Automatically places blocks below you.", Category.WORLD,true,false,false);
    }

    @Override
    public void onDisable()
    {
        if (mc.player != null)
        {
            MoneyStar.inventoryManager.syncToClient();
        }
        groundPosY = -1;
        lastAngles = null;
        blockData = null;
        renderData = null;
       // fadeList.clear();
    }

    @Subscribe
    public void onPlayerTick(final PlayerTickEvent event)
    {

        if (!multitask.getValue() && checkMultitask())
        {
            blockData = null;
            renderData = null;
            return;
        }

        int slot = getBlockSlot();
        if (slot == -1)
        {
            blockData = null;
            renderData = null;
            return;
        }
        renderData = getBlockData(false);
        blockData = getBlockData(rotateHold.getValue());
        if (blockData == null)
        {
            if (grimNew.getValue() && rotate.getValue())
            {
                float yaw = mc.player.getYaw();
                if (mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed())
                {
                    if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
                    {
                        yaw -= 45.0f;
                    }
                    else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
                    {
                        yaw += 45.0f;
                    }
                    // Forward movement - no change to yaw
                }
                else if (mc.options.backKey.isPressed() && !mc.options.forwardKey.isPressed())
                {
                    yaw += 180.0f;
                    if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
                    {
                        yaw += 45.0f;
                    }
                    else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
                    {
                        yaw -= 45.0f;
                    }
                }
                else if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
                {
                    yaw -= 90.0f;
                }
                else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
                {
                    yaw += 90.0f;
                }
                setRotation(MathHelper.wrapDegrees(yaw), 90.0f);
            }
            return;
        }

        calcRotations(blockData);
        if (blockData.getAngles() == null)
        {
            if (!isGrim() && rotate.getValue() && lastAngles != null)
            {
                setRotation(lastAngles[0], lastAngles[1]);
            }
            return;
        }

        if (!isGrim() && MoneyStar.inventoryManager.getServerSlot() != slot)
        {
            MoneyStar.inventoryManager.setSlot(slot);
        }
        boolean result = MoneyStar.interactionManager.placeBlock(blockData.getBlockPos(), slot, false, false, false, (state, angles) ->
        {
            if (rotate.getValue())
            {
                final float[] rotations = blockData.getAngles();
                if (rotations == null)
                {
                    return;
                }
                lastAngles = rotations;
                if (state)
                {
                    if (grim.getValue())
                    {
                        MoneyStar.rotationManager.setRotationSilent(rotations[0], rotations[1]);
                    }
                    else
                    {
                        setRotation(rotations[0], rotations[1]);
                    }
                }
                else
                {
                    if (grim.getValue())
                    {
                        MoneyStar.rotationManager.setRotationSilentSync();
                    }
                }
            }
        });
        if (result)
        {
            if (!isGrim() && tower.getValue() && mc.options.jumpKey.isPressed())
            {
                final Vec3d velocity = mc.player.getVelocity();
                final double velocityY = velocity.y;
                if ((mc.player.isOnGround() || velocityY < 0.1) || velocityY <= 0.16477328182606651)
                {
                    mc.player.setVelocity(velocity.x, 0.42f, velocity.z);
                }
            }
        }
    }



    private void calcRotations(final BlockData blockData)
    {
        final BlockPos pos = blockData.getHitResult().getBlockPos();
        final Direction side = blockData.getHitResult().getSide();
        final Vec3d basicHitVec = pos.toCenterPos()
                .add(side.getOffsetX() * 0.5f, side.getOffsetY() * 0.5f, side.getOffsetZ() * 0.5f);
        blockData.setAngles(RotationUtil.getRotationsTo(mc.player.getEyePos(), basicHitVec));
        blockData.setHitResult(new BlockHitResult(basicHitVec, side, pos, false));
    }

    private BlockData getBlockData(boolean hold)
    {
        int posY = (int) Math.round(mc.player.getY()) - 1;
        if (keepY.getValue() && MovementUtil.isInputtingMovement())
        {
            if (mc.player.isOnGround() || groundPosY == -1)
            {
                groundPosY = (int) Math.floor(mc.player.getY()) - 1;
            }
            posY = groundPosY;
        }
        final BlockPos pos = PositionUtil.getRoundedBlockPos(
                mc.player.getX(), posY, mc.player.getZ());
        if (!hold && !mc.world.getBlockState(pos).isReplaceable())
        {
            return null;
        }
        for (final Direction direction : Direction.values())
        {
            final BlockPos neighbor = pos.offset(direction);
            if (!mc.world.getBlockState(neighbor).isReplaceable())
            {
                return BlockData.basic(neighbor, direction.getOpposite());
            }
        }
        for (final Direction direction : Direction.values())
        {
            final BlockPos neighbor = pos.offset(direction);
            if (mc.world.getBlockState(neighbor).isReplaceable())
            {
                for (final Direction direction1 : Direction.values())
                {
                    final BlockPos neighbor1 = neighbor.offset(direction1);
                    if (!mc.world.getBlockState(neighbor1).isReplaceable())
                    {
                        return BlockData.basic(neighbor1, direction1.getOpposite());
                    }
                }
            }
        }
        return null;
    }

    private int getBlockSlot()
    {
        final ItemStack serverStack = MoneyStar.inventoryManager.getServerItem();
        if (!serverStack.isEmpty() && serverStack.getItem() instanceof BlockItem blockItem )
        {
            return MoneyStar.inventoryManager.getServerSlot();
        }

        int blockSlot = -1;
        int count = 0;
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem blockItem)
            {
                if (picker.getValue() == BlockPicker.NORMAL)
                {
                    return i;
                }

                if (blockSlot == -1 || itemStack.getCount() > count)
                {
                    blockSlot = i;
                    count = itemStack.getCount();
                }
            }
        }

        return blockSlot;
    }



    private static class BlockData
    {
        private BlockHitResult hitResult;
        private float[] angles;

        public BlockData(final BlockHitResult hitResult, final float[] angles)
        {
            this.hitResult = hitResult;
            this.angles = angles;
        }

        public BlockHitResult getHitResult()
        {
            return hitResult;
        }

        public BlockPos getBlockPos()
        {
            return hitResult.getBlockPos().offset(hitResult.getSide());
        }

        public void setHitResult(BlockHitResult hitResult)
        {
            this.hitResult = hitResult;
        }

        public float[] getAngles()
        {
            return angles;
        }

        public void setAngles(float[] angles)
        {
            this.angles = angles;
        }

        public static BlockData basic(final BlockPos pos, final Direction direction)
        {
            return new BlockData(new BlockHitResult(pos.toCenterPos(), direction, pos, false), null);
        }
    }

    public boolean isGrim()
    {
        return grim.getValue() || grimNew.getValue();
    }

    private enum BlockPicker
    {
        NORMAL,
        GREATEST
    }

}
