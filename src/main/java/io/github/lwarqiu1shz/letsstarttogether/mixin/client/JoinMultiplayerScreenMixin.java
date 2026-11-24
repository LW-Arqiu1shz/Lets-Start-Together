package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import io.github.lwarqiu1shz.letsstarttogether.client.LobbyButtons;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * “多人游戏”界面每次重新排列控件（首次打开、窗口改变大小）之后，把底部上面一行重新摆成
 * 四个 74 宽的按钮。按钮本身由 {@link LobbyButtons} 在 {@code ScreenEvent.Init.Post} 里加入。
 */
@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin {
    @Inject(method = "repositionElements", at = @At("RETURN"))
    private void lets_start_together$relayoutJoinRow(CallbackInfo ci) {
        LobbyButtons.layoutJoinRow((Screen) (Object) this);
    }
}
