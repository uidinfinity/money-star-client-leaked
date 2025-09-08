package me.money.star.client.modules.combat;

import com.google.common.collect.Lists;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.impl.network.PlayerTickEvent;
import me.money.star.util.player.InventoryUtil;
import me.money.star.util.player.PlayerUtil;
import me.money.star.util.world.EndCrystalUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;


public final class Offhand extends Module
{
    private static final int INVENTORY_SYNC_ID = 0;
    private static final List<Item> HOTBAR_ITEMS = List.of(Items.TOTEM_OF_UNDYING,
            Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);


    public Setting<OffhandItem> mode = mode("Mode", OffhandItem.TOTEM);
    public Setting<Float> healthPlayer = num("Health", 14.0f,0.0f , 36.0f);
    public Setting<Boolean> gapple = bool("Offhand-Gapple", false);
    public Setting<Boolean> crapple = bool("Crapple", false);
    public Setting<Boolean> lethal = bool("Lethal", false);
    public Setting<Boolean> fast = bool("Fast-Swap", false);
    private int lastSlot;

    public Offhand()
    {
        super("Offhand", "Automatically replenishes the totem in your offhand", Category.COMBAT,true,false,false);
    }

    @Override
    public void onEnable()
    {
        lastSlot = -1;
    }

    @Subscribe
    public void onPlayerTick(final PlayerTickEvent event)
    {
        if (mc.currentScreen != null)
        {
            return;
        }
        // Get the item to wield in our offhand, and make sure we are already not holding the item
        final Item itemToWield = getItemToWield();
        if (PlayerUtil.isHolding(itemToWield))
        {
            return;
        }
        // Find the item in our inventory
        final int itemSlot = getSlotFor(itemToWield);
        if (itemSlot != -1)
        {
            if (itemSlot < 9) {
                lastSlot = itemSlot;
            }
            // Do another quick swap (equivalent to hovering over an item & pressing F)
            if (fast.getValue()) {
                mc.interactionManager.clickSlot(INVENTORY_SYNC_ID,
                        itemSlot < 9 ? itemSlot + 36 : itemSlot, 40, SlotActionType.SWAP, mc.player);
            } else {
                mc.interactionManager.clickSlot(INVENTORY_SYNC_ID, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(INVENTORY_SYNC_ID, 45, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(INVENTORY_SYNC_ID,
                            itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, mc.player);
                }
            }
        }
    }

    private int getSlotFor(final Item item)
    {
        if (lastSlot != -1 && item.equals(mc.player.getInventory().getStack(lastSlot).getItem())) {
            int slot = lastSlot;
            lastSlot = -1;
            return slot;
        }
        // Only take totems from the hotbar
        final int startSlot = HOTBAR_ITEMS.contains(item) ? 0 : 9;
        // Search through our inventory
        for (int slot = 35; slot >= startSlot; slot--)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(slot);
            if (!itemStack.isEmpty() && itemStack.getItem().equals(item))
            {
                return slot;
            }
        }
        return -1;
    }

    private Item getItemToWield()
    {
        // If the player's health (+absorption) falls below the "safe" amount, equip a totem
        final float health = PlayerUtil.getLocalPlayerHealth();
        if (health <= healthPlayer.getValue())
        {
            return Items.TOTEM_OF_UNDYING;
        }
        // Check fall damage
        if (PlayerUtil.computeFallDamage(mc.player.fallDistance, 1.0f) + 0.5f > mc.player.getHealth())
        {
            return Items.TOTEM_OF_UNDYING;
        }
        if (lethal.getValue())
        {
            final List<Entity> entities = Lists.newArrayList(mc.world.getEntities());
            for (Entity e : entities)
            {
                if (e == null || !e.isAlive() || !(e instanceof EndCrystalEntity crystal))
                {
                    continue;
                }
                if (mc.player.squaredDistanceTo(e) > 144.0)
                {
                    continue;
                }
                double potential = EndCrystalUtil.getDamageTo(mc.player, crystal.getPos());
                if (health + 0.5 > potential)
                {
                    continue;
                }
                return Items.TOTEM_OF_UNDYING;
            }
        }
        // If offhand gap is enabled & the use key is pressed down, equip a golden apple.
        if (gapple.getValue() && mc.options.useKey.isPressed() && (mc.player.getMainHandStack().getItem() instanceof SwordItem
                || mc.player.getMainHandStack().getItem() instanceof TridentItem || mc.player.getMainHandStack().getItem() instanceof AxeItem))
        {
            return getGoldenAppleType();
        }
        return mode.getValue().getItem();
    }

    private Item getGoldenAppleType()
    {
        if (crapple.getValue()
                && mc.player.hasStatusEffect(StatusEffects.ABSORPTION)
                && InventoryUtil.hasItemInInventory(Items.GOLDEN_APPLE, true))
        {
            return Items.GOLDEN_APPLE;
        }
        return Items.ENCHANTED_GOLDEN_APPLE;
    }

    private enum OffhandItem
    {
        TOTEM(Items.TOTEM_OF_UNDYING),
        GAPPLE(Items.ENCHANTED_GOLDEN_APPLE),
        CRYSTAL(Items.END_CRYSTAL);

        private final Item item;

        OffhandItem(Item item)
        {
            this.item = item;
        }

        public Item getItem()
        {
            return item;
        }
    }
}
