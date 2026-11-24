package io.github.lwarqiu1shz.letsstarttogether.client;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.neoforgespi.language.IModInfo;

/**
 * 模组列表里的双语名称（和金源工艺系列、三叉戟强无敌同一套做法）。
 *
 * <p>这一版 NeoForge 的模组列表会按 {@code fml.menu.mods.info.description.<modid>} 翻译简介，
 * 却只显示 neoforge.mods.toml 里写死的 displayName。{@code mixin.client.ModList*} 在读名称的地方
 * 调用这里：只对本模组、且当前语言有 {@value #NAME_KEY} 译文时换成翻译名。</p>
 */
public final class LstModListNames {
    public static final String NAME_KEY = "fml.menu.mods.info.displayname." + LetsStartTogether.MOD_ID;

    private LstModListNames() {
    }

    public static String displayName(IModInfo info, String original) {
        if (LetsStartTogether.MOD_ID.equals(info.getModId()) && I18n.exists(NAME_KEY)) {
            return I18n.get(NAME_KEY);
        }
        return original;
    }

    /** 搜索用：翻译名 + 原名，中文界面下搜 "Let's Start" 或 "联机小厅" 都能找到。 */
    public static String searchName(IModInfo info, String original) {
        String shown = displayName(info, original);
        return shown.equals(original) ? original : shown + " " + original;
    }
}
