package me.money.star.event.impl.world;

import me.money.star.event.StageEvent;
import net.minecraft.util.math.ChunkPos;


/**
 * @author linus
 * @since 1.0
 */
public class ChunkLoadEvent extends StageEvent {
    // Chunk position. Needs scaling
    private final ChunkPos pos;

    /**
     * @param pos
     */
    public ChunkLoadEvent(ChunkPos pos) {
        this.pos = pos;
    }

    /**
     * @return
     */
    public ChunkPos getPos() {
        return pos;
    }
}
