package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.satxm.mcwifipnp.client.WorldOptionsScreenNew;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 小厅里的 mcwifipnp 设置界面。房主还没进世界，有些设置改不了，界面会把它们灰掉。
 * 这里只换灰按钮的提示，告诉房主进了世界再改；“行为限制”按钮小厅里点了没反应，也一起灰掉。
 */
@Mixin(value = WorldOptionsScreenNew.class, remap = false)
public abstract class WorldOptionsLobbyMixin {
    @Inject(method = "updateButton", at = @At("TAIL"), require = 0)
    private void lets_start_together$needWorld(@Nullable AbstractWidget widget, @Nullable IntegratedServer server,
                                             @Nullable Tooltip tooltip, Tooltip disabledTooltip, Tooltip hardcoreTooltip,
                                             CallbackInfo ci) {
        if (widget != null && !widget.active && Minecraft.getInstance().player == null
                && (server == null || !server.isHardcore())) {
            widget.setTooltip(Tooltip.create(Component.translatable("lets_start_together.world_options.need_world")));
        }
    }

    /** 界面里的选项装在可滚动的面板里，所以在建按钮的时候就把「限制」按钮挑出来灰掉。 */
    @ModifyExpressionValue(method = "generalOptions", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;"))
    private Button lets_start_together$greyRestrictions(Button button) {
        if (Minecraft.getInstance().player == null && button.getMessage().getContents() instanceof TranslatableContents t
                && t.getKey().equals("restrictions_screen.button")) {
            button.active = false;
            button.setTooltip(Tooltip.create(Component.translatable("lets_start_together.world_options.need_world")));
        }
        return button;
    }
}
