package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import io.github.lwarqiu1shz.letsstarttogether.client.LobbyButtons;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 存档“编辑”界面每次重新排列控件之后，把“开关联机小厅”重新放回“优化世界”和
 * “保存 / 取消”之间的空位里。按钮本身由 {@link LobbyButtons} 在
 * {@code ScreenEvent.Init.Post} 里加入。
 */
@Mixin(EditWorldScreen.class)
public class EditWorldScreenMixin {
    @Inject(method = "repositionElements", at = @At("RETURN"))
    private void lets_start_together$relayoutEditRow(CallbackInfo ci) {
        LobbyButtons.layoutEditRow((Screen) (Object) this);
    }
}
