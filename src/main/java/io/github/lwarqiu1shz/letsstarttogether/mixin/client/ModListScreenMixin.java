package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.lwarqiu1shz.letsstarttogether.client.LstModListNames;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** 模组列表界面：右侧标题、列表宽度用翻译名；搜索同时匹配翻译名与原名。 */
@Mixin(ModListScreen.class)
public abstract class ModListScreenMixin {
    @WrapOperation(method = {"init", "updateCache"},
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforgespi/language/IModInfo;getDisplayName()Ljava/lang/String;"))
    private String lets_start_together$translatedName(IModInfo info, Operation<String> original) {
        return LstModListNames.displayName(info, original.call(info));
    }

    /** require = 0：其它 NeoForge 小版本的 lambda 编号可能不同，挂不上时只少了双名搜索。 */
    @WrapOperation(method = "lambda$reloadMods$0", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforgespi/language/IModInfo;getDisplayName()Ljava/lang/String;"))
    private String lets_start_together$searchBothNames(IModInfo info, Operation<String> original) {
        return LstModListNames.searchName(info, original.call(info));
    }
}
