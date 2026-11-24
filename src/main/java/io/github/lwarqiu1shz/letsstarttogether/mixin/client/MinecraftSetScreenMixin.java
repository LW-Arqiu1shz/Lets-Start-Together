package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import io.github.lwarqiu1shz.letsstarttogether.client.LobbyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** 还在小厅里时，要打开标题界面就换回小厅（见 {@link LobbyClient#onScreenOpening}）。 */
@Mixin(Minecraft.class)
public class MinecraftSetScreenMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private @Nullable Screen lets_start_together$keepLobby(@Nullable Screen screen) {
        return LobbyClient.onScreenOpening(screen);
    }
}
