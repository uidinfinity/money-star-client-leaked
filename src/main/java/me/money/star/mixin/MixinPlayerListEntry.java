package me.money.star.mixin;

import me.money.star.MoneyStar;
import me.money.star.client.modules.client.Capes;
import me.money.star.util.traits.Util;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Final;
import com.mojang.authlib.GameProfile;



@Mixin(PlayerListEntry.class)
public class MixinPlayerListEntry implements Util {

}
