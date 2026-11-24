package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 小厅里难度改不了，按钮是灰的。这里只换提示：难度锁了或是极限模式就照原样说，否则告诉房主进了世界再改。 */
@Mixin(targets = "io.github.satxm.mcwifipnp.client.WorldOptionsScreenNew$DifficultyButtons", remap = false)
public abstract class WorldOptionsDifficultyMixin {
    @Shadow
    @Final
    private static Tooltip DIFFICULTY_DISABLED_HARDCORE_TOOLTIP;
    @Shadow
    @Final
    private static Tooltip DIFFICULTY_DISABLED_LOCKED_TOOLTIP;

    @Inject(method = "updateDifficultyButtonsState", at = @At("TAIL"), require = 0)
    private static void lets_start_together$needWorld(Minecraft minecraft, Level level, CycleButton<Difficulty> difficultyButton,
                                                    LockIconButton lockButton, CallbackInfo ci) {
        if (minecraft.player != null || difficultyButton.active) {
            return;
        }
        LevelData data = level.getLevelData();
        difficultyButton.setTooltip(data.isDifficultyLocked() ? DIFFICULTY_DISABLED_LOCKED_TOOLTIP
                : data.isHardcore() ? DIFFICULTY_DISABLED_HARDCORE_TOOLTIP
                : Tooltip.create(Component.translatable("lets_start_together.world_options.need_world")));
    }
}
