package me.money.star.client.manager.player;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.money.star.MoneyStar;
import me.money.star.client.modules.client.AntiCheat;
import me.money.star.client.modules.world.Replenish;
import me.money.star.event.impl.ItemDesyncEvent;
import me.money.star.event.impl.PacketEvent;
import me.money.star.event.impl.TickEvent;
import me.money.star.event.impl.entity.EntityDeathEvent;
import me.money.star.mixin.accessor.AccessorBundlePacket;
import me.money.star.util.math.timer.CacheTimer;
import me.money.star.util.math.timer.Timer;
import me.money.star.util.traits.Util;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
public class InventoryManager implements Util {
    private final List<PreSwapData> swapData = new CopyOnWriteArrayList<>();

    // The serverside selected hotbar slot.
    private int slot;

    /**
     *
     */
    public InventoryManager()
    {
        Util.EVENT_BUS.register(this);
    }

    @Subscribe
    public void onPacketOutBound(final PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet)
        {
            final int packetSlot = packet.getSelectedSlot();
            if (!PlayerInventory.isValidHotbarIndex(packetSlot) || slot == packetSlot)
            {
                event.setCancelled(true);
                return;
            }
            slot = packetSlot;
        }
    }

    @Subscribe
    public void onPacketInbound(final PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket packet)
        {
            slot = packet.slot();
        }

        if (Replenish.getInstance().isInInventoryScreen() || !AntiCheat.getInstance().isGrim())
        {
            return;
        }

        // retarded packets from grim we can ignore
        if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            List<Packet<?>> allowedBundle = new ArrayList<>();
            for (Packet<?> packet1 : packet.getPackets())
            {
                if (packet1 instanceof ScreenHandlerSlotUpdateS2CPacket)
                {
                    continue;
                }
                allowedBundle.add(packet1);
            }
            ((AccessorBundlePacket) packet).setIterable(allowedBundle);
        }

        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket packet)
        {
            int slot = packet.getSlot() - 36;
            if (slot < 0 || slot > 8)
            {
                return;
            }

            if (packet.getStack().isEmpty())
            {
                return;
            }

            for (PreSwapData data : swapData)
            {
                if (data.getSlot() != slot && data.getStarting() != slot)
                {
                    continue;
                }

                ItemStack preStack = data.getPreHolding(slot);
                if (!isEqual(preStack, packet.getStack()))
                {
                    event.cancel();
                    break;
                }
            }
        }
    }

    @Subscribe
    public void onItemDesync(ItemDesyncEvent event)
    {
        if (isDesynced())
        {
            event.cancel();
            event.setStack(getServerItem());
        }
    }

    @Subscribe
    public void onDeath(EntityDeathEvent event)
    {
        if (event.getEntity() == mc.player)
        {
            syncToClient();
        }
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        swapData.removeIf(PreSwapData::isPassedClearTime);
    }

    /**
     * Sets the server slot via a {@link UpdateSelectedSlotC2SPacket}
     *
     * @param barSlot the player hotbar slot 0-8
     * @apiNote Method will not do anything if the slot provided is already the server slot
     * @see InventoryManager#setSlotForced(int)
     */
    public void setSlot(final int barSlot)
    {
        if (slot != barSlot && PlayerInventory.isValidHotbarIndex(barSlot))
        {
            setSlotForced(barSlot);

            final ItemStack[] hotbarCopy = new ItemStack[9];
            for (int i = 0; i < 9; i++)
            {
                hotbarCopy[i] = mc.player.getInventory().getStack(i);
            }
            swapData.add(new PreSwapData(hotbarCopy, slot, barSlot));
        }
    }

    /**
     * Sets the server slot via a click slot
     *
     * @param barSlot the player hotbar slot 0-8
     */
    public void setSlotAlt(final int barSlot)
    {
        if (PlayerInventory.isValidHotbarIndex(barSlot))
        {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    barSlot + 36, slot, SlotActionType.SWAP, mc.player);
        }
    }

    /**
     * Sets the server & client slot
     *
     * @param barSlot the player hotbar slot 0-8
     * @apiNote Method will not do anything if the slot provided is already the server slot
     * @see InventoryManager#setSlotForced(int)
     * @see InventoryManager#setSlot(int)
     */
    public void setClientSlot(final int barSlot)
    {
        if (mc.player.getInventory().selectedSlot != barSlot
                && PlayerInventory.isValidHotbarIndex(barSlot))
        {
            mc.player.getInventory().selectedSlot = barSlot;
            setSlotForced(barSlot);
        }
    }


    public void setSlotForced(final int barSlot)
    {
        MoneyStar.networkManager.sendPacket(new UpdateSelectedSlotC2SPacket(barSlot));
    }

    /**
     * Syncs the server slot to the client slot
     */
    public void syncToClient()
    {
        if (isDesynced())
        {
            setSlotForced(mc.player.getInventory().selectedSlot);

            for (PreSwapData swapData : swapData)
            {
                swapData.beginClear();
            }
        }
    }

    public boolean isDesynced()
    {
        return mc.player.getInventory().selectedSlot != slot;
    }

    //
    public void closeScreen()
    {
        MoneyStar.networkManager.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
    }

    /**
     * @param slot
     */
    public int pickupSlot(final int slot)
    {
        return click(slot, 0, SlotActionType.PICKUP);
    }

    public void quickMove(final int slot)
    {
        click(slot, 0, SlotActionType.QUICK_MOVE);
    }

    /**
     * @param slot
     */
    public void throwSlot(final int slot)
    {
        click(slot, 0, SlotActionType.THROW);
    }

    public int findEmptySlot()
    {
        for (int i = 9; i < 36; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty())
            {
                return i;
            }
        }
        return -999; // throw
    }

    /**
     * @param slot
     * @param button
     * @param type
     */
    public int click(int slot, int button, SlotActionType type)
    {
        if (slot < 0)
        {
            return -1;
        }
        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        DefaultedList<Slot> defaultedList = screenHandler.slots;
        int i = defaultedList.size();
        ArrayList<ItemStack> list = Lists.newArrayListWithCapacity(i);
        for (Slot slot1 : defaultedList)
        {
            list.add(slot1.getStack().copy());
        }
        screenHandler.onSlotClick(slot, button, type, mc.player);
        Int2ObjectOpenHashMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
        for (int j = 0; j < i; ++j)
        {
            ItemStack itemStack2;
            ItemStack itemStack = list.get(j);
            if (ItemStack.areEqual(itemStack, itemStack2 = defaultedList.get(j).getStack())) continue;
            int2ObjectMap.put(j, itemStack2.copy());
        }
        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(screenHandler.syncId, screenHandler.getRevision(), slot, button, type, screenHandler.getCursorStack().copy(), int2ObjectMap));
        return screenHandler.getRevision();
    }

    public int click2(int slot, int button, SlotActionType type)
    {
        if (slot < 0)
        {
            return -1;
        }
        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        DefaultedList<Slot> defaultedList = screenHandler.slots;
        int i = defaultedList.size();
        ArrayList<ItemStack> list = Lists.newArrayListWithCapacity(i);
        for (Slot slot1 : defaultedList)
        {
            list.add(slot1.getStack().copy());
        }
        // screenHandler.onSlotClick(slot, button, type, mc.player);
        Int2ObjectOpenHashMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
        for (int j = 0; j < i; ++j)
        {
            ItemStack itemStack2;
            ItemStack itemStack = list.get(j);
            if (ItemStack.areEqual(itemStack, itemStack2 = defaultedList.get(j).getStack())) continue;
            int2ObjectMap.put(j, itemStack2.copy());
        }
        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(screenHandler.syncId, screenHandler.getRevision(), slot, button, type, screenHandler.getCursorStack().copy(), int2ObjectMap));
        return screenHandler.getRevision();
    }
    public int count(Item item) {
        ItemStack offhandStack = mc.player.getOffHandStack();
        int itemCount = offhandStack.getItem() == item ? offhandStack.getCount() : 0;
        for (int i = 0; i < 36; i++) {
            ItemStack slot = mc.player.getInventory().getStack(i);
            if (slot.getItem() == item) {
                itemCount += slot.getCount();
            }
        }
        return itemCount;
    }


    /**
     * @return
     */
    public int getServerSlot()
    {
        return slot;
    }

    public int getClientSlot()
    {
        return mc.player.getInventory().selectedSlot;
    }

    /**
     * @return
     */
    public ItemStack getServerItem()
    {
        if (mc.player != null && getServerSlot() != -1)
        {
            return mc.player.getInventory().getStack(getServerSlot());
        }
        return null;
    }

    private boolean isEqual(ItemStack stack1, ItemStack stack2)
    {
        return stack1.getItem().equals(stack2.getItem()) && stack1.getName().equals(stack2.getName());
    }

    public static class PreSwapData
    {
        private final ItemStack[] preHotbar;

        private final int starting;
        private final int swapTo;

        private Timer clearTime;

        public PreSwapData(ItemStack[] preHotbar, int start, int swapTo)
        {
            this.preHotbar = preHotbar;
            this.starting = start;
            this.swapTo = swapTo;
        }

        public void beginClear()
        {
            clearTime = new CacheTimer();
            clearTime.reset();
        }

        public boolean isPassedClearTime()
        {
            return clearTime != null && clearTime.passed(300);
        }

        public ItemStack getPreHolding(int i)
        {
            return preHotbar[i];
        }

        public int getStarting()
        {
            return starting;
        }

        public int getSlot()
        {
            return swapTo;
        }
    }
}