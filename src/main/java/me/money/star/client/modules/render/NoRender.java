package me.money.star.client.modules.render;

import com.google.common.eventbus.Subscribe;
import me.money.star.client.gui.modules.Module;
import me.money.star.client.settings.Setting;
import me.money.star.event.Stage;
import me.money.star.event.impl.*;
import com.google.common.collect.Lists;
import me.money.star.event.impl.gui.hud.RenderOverlayEvent;
import me.money.star.event.impl.render.HurtCamEvent;
import me.money.star.event.impl.render.block.RenderTileEntityEvent;
import me.money.star.event.impl.render.entity.RenderArmorEvent;
import me.money.star.event.impl.render.entity.RenderFireworkRocketEvent;
import me.money.star.event.impl.render.entity.RenderItemEvent;
import me.money.star.event.impl.render.entity.RenderWitherSkullEvent;
import me.money.star.event.impl.toast.RenderToastEvent;
import me.money.star.mixin.accessor.AccessorFireworkRocketEntity;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.entity.Entity;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
public class NoRender extends Module {
    private static NoRender INSTANCE;
    public Setting<Boolean> hurtCam  = bool("NoHurtCam", false);
    public Setting<Boolean> armor  = bool("Armor", false);
    public Setting<Boolean> fireOverlay  = bool("Overlay-Fire", false);
    public Setting<Boolean> portalOverlay  = bool("Overlay-Portal", false);
    public Setting<Boolean> waterOverlay  = bool("Overlay-Water", false);
    public Setting<Boolean> blockOverlay  = bool("Overlay-Block", false);
    public Setting<Boolean> spyglassOverlay  = bool("Overlay-Spyglass", false);
    public Setting<Boolean> pumpkinOverlay  = bool("Overlay-Pumpkin", false);
    public Setting<Boolean> bossOverlay  = bool("Overlay-BossBar", false);
    public Setting<Boolean> nausea  = bool("Nausea", false);

    public Setting<Boolean> frostbite  = bool("Frostbite", false);
    public Setting<Boolean> skylight  = bool("Skylight", false);
    public Setting<Boolean> witherSkulls  = bool("WitherSkulls", false);
    public Setting<Boolean> itemFrames  = bool("ItemFrames", false);
    public Setting<ItemRender> items = mode("Mode", ItemRender.OFF);
    public Setting<Boolean> tileEntities  = bool("TileEntities", false);
    public Setting<Boolean> fireEntity  = bool("FireEntities", false);
    public Setting<Boolean> fireworks  = bool("Fireworks", false);
    public Setting<Boolean> totem  = bool("Totems", false);
    public Setting<Boolean> worldBorder  = bool("WorldBorder", false);
    public Setting<Boolean> guiToast  = bool("GuiToast", false);
    public Setting<Boolean> terrainScreen  = bool("TerrainScreen", false);

    public NoRender() {
        super("NoRender", "Draws box at the block that you are looking at", Category.RENDER, true, false, false);
        INSTANCE = this;

    }
    public static NoRender getInstance()
    {
        return INSTANCE;
    }

    @Subscribe
    public void onTick(TickEvent event)
    {
        if (event.getStage() != Stage.PRE)
        {
            return;
        }

        if (items.getValue() == ItemRender.REMOVE)
        {
            for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
            {
                if (entity instanceof ItemEntity)
                {
                    mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
                }
            }
        }

        if (terrainScreen.getValue() && mc.currentScreen instanceof DownloadingTerrainScreen)
        {
            mc.currentScreen = null;
        }
    }

    @Subscribe
    public void onHurtCam(HurtCamEvent event)
    {
        if (hurtCam.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderArmor(RenderArmorEvent event)
    {
        if (armor.getValue() && event.getEntity() instanceof PlayerEntity)
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderOverlayFire(RenderOverlayEvent.Fire event)
    {
        if (fireOverlay.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderOverlayPortal(RenderOverlayEvent.Portal event)
    {
        if (portalOverlay.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderOverlayWater(RenderOverlayEvent.Water event)
    {
        if (waterOverlay.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderOverlayBlock(RenderOverlayEvent.Block event)
    {
        if (blockOverlay.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderOverlaySpyglass(RenderOverlayEvent.Spyglass event)
    {
        if (spyglassOverlay.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderOverlayPumpkin(RenderOverlayEvent.Pumpkin event)
    {
        if (pumpkinOverlay.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderOverlayBossBar(RenderOverlayEvent.BossBar event)
    {
        if (bossOverlay.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderOverlayFrostbite(RenderOverlayEvent.Frostbite event)
    {
        if (frostbite.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderNausea(RenderNauseaEvent event)
    {
        if (nausea.getValue())
        {
            event.cancel();
        }
    }



    @Subscribe
    public void onRenderSkylight(RenderSkylightEvent event)
    {
        if (skylight.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderWitherSkull(RenderWitherSkullEvent event)
    {
        if (witherSkulls.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderItemFrame(RenderItemFrameEvent event)
    {
        if (itemFrames.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderEnchantingTableBook(RenderTileEntityEvent.EnchantingTableBook event)
    {
        if (tileEntities.getValue())
        {
            event.cancel();
        }
    }


    @Subscribe
    public void onRenderFireworkRocket(RenderFireworkRocketEvent event)
    {
        if (fireworks.getValue())
        {
            event.cancel();
        }
    }


    @Subscribe
    public void onRenderFloatingItem(RenderFloatingItemEvent event)
    {
        if (totem.getValue() && event.getFloatingItem() == Items.TOTEM_OF_UNDYING)
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderWorldBorder(RenderWorldBorderEvent event)
    {
        if (worldBorder.getValue())
        {
            event.cancel();
        }
    }



    @Subscribe
    public void onRenderItem(RenderItemEvent event)
    {
        if (items.getValue() == ItemRender.HIDE)
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderToast(RenderToastEvent event)
    {
        if (guiToast.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onRenderFireEntity(RenderFireEntityEvent event)
    {
        if (fireEntity.getValue())
        {
            event.cancel();
        }
    }

    @Subscribe
    public void onItemTick(ItemTickEvent event)
    {
        if (items.getValue() != ItemRender.OFF)
        {
            event.cancel();
        }
    }



    public enum FogRender
    {
        CLEAR,
        LIQUID_VISION,
        OFF
    }

    public enum ItemRender
    {
        REMOVE,
        HIDE,
        OFF
    }
}


