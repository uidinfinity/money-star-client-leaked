package me.money.star.client.gui.modules;

import me.money.star.client.gui.modules.BlockPlacerModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ObsidianPlacerModule extends BlockPlacerModule {
    protected static final BlockState DEFAULT_OBSIDIAN_STATE = Blocks.OBSIDIAN.getDefaultState();
    // Blocks that can prevent explosion damage
    private static final List<Block> RESISTANT_BLOCKS = new LinkedList<>()
    {{
        add(Blocks.OBSIDIAN);
        add(Blocks.CRYING_OBSIDIAN);
        add(Blocks.ENDER_CHEST);
    }};

    public ObsidianPlacerModule(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening)
    {
        super(name, description, category,hasListener,hidden,alwaysListening);
    }

    public ObsidianPlacerModule(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening, int rotationPriority)
    {
        super(name, description, category,hasListener,hidden,alwaysListening, rotationPriority);
    }

    /**
     * @return
     */
    protected int getResistantBlockItem()
    {
        final Set<BlockSlot> blockSlots = new HashSet<>();
        for (final Block type : RESISTANT_BLOCKS)
        {
            final int slot = getBlockItemSlot(type);
            if (slot != -1)
            {
                blockSlots.add(new BlockSlot(type, slot));
            }
        }

        // Prioritize
        BlockSlot slot = blockSlots.stream().filter(b -> b.block() == Blocks.OBSIDIAN).findFirst().orElse(null);
        if (slot != null)
        {
            return slot.slot();
        }
        BlockSlot slot1 = blockSlots.stream().filter(b -> b.block() == Blocks.CRYING_OBSIDIAN).findFirst().orElse(null);
        if (slot1 != null)
        {
            return slot1.slot();
        }
        BlockSlot slot2 = blockSlots.stream().filter(b -> b.block() == Blocks.ENDER_CHEST).findFirst().orElse(null);
        if (slot2 != null)
        {
            return slot2.slot();
        }
        return -1;
    }

    public record BlockSlot(Block block, int slot)
    {
        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof BlockSlot b && b.block() == block;
        }
    }
}
