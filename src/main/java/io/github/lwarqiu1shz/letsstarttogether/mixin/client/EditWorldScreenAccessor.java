package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 存档编辑界面的开关要知道这个存档的文件夹在哪。 */
@Mixin(EditWorldScreen.class)
public interface EditWorldScreenAccessor {
    @Accessor("levelAccess")
    LevelStorageSource.LevelStorageAccess lets_start_together$levelAccess();
}
