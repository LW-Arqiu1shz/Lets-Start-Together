package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.lwarqiu1shz.letsstarttogether.client.LobbyCreateState;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 在“创建新的世界”的“游戏”页最下面加一个“是否为多人世界：是 / 否”。 */
@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$GameTab")
public class CreateWorldGameTabMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void lets_start_together$addLobbyToggle(CreateWorldScreen screen, CallbackInfo ci, @Local GridLayout.RowHelper helper) {
        helper.addChild(CycleButton.booleanBuilder(CommonComponents.GUI_YES, CommonComponents.GUI_NO, LobbyCreateState.isLobby(screen))
                .withTooltip(value -> Tooltip.create(Component.translatable("lets_start_together.create.multiplayer.tooltip")))
                .create(0, 0, 210, 20, Component.translatable("lets_start_together.create.multiplayer"),
                        (button, value) -> LobbyCreateState.setLobby(screen, value)));
    }
}
