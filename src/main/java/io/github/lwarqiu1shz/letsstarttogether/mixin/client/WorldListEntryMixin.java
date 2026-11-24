package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import io.github.lwarqiu1shz.letsstarttogether.client.LobbyCrowns;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 存档列表里，联机小厅世界的图标顶上戴一顶像素小皇冠。 */
@Mixin(WorldSelectionList.WorldListEntry.class)
public class WorldListEntryMixin {
    @Shadow @Final private LevelSummary summary;

    @Inject(method = "extractContent", at = @At("RETURN"))
    private void lets_start_together$crown(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a,
                                         CallbackInfo ci) {
        if (LobbyCrowns.isLobby(this.summary.getLevelId())) {
            WorldSelectionList.Entry self = (WorldSelectionList.Entry) (Object) this;
            LobbyCrowns.extract(graphics, self.getContentX(), self.getContentY());
        }
    }
}
