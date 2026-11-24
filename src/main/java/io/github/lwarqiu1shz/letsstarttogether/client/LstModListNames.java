package io.github.lwarqiu1shz.letsstarttogether.client;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

/**
 * 模组列表里的双语名称（和金源工艺系列、三叉戟强无敌同一套做法）。
 *
 * <p>26.3 的 NeoForge 模组列表按 {@code neoforge.screen.mods.info.description.<modid>} 翻译简介；
 * 名称由 LetsStartTogetherClient 注册的 ModDisplayInfo 按 {@value #NAME_KEY} 翻译。
 * 列表的搜索只匹配显示的名称，ModListSearchMixin 在那里调用 {@link #searchName}，
 * 中文界面下搜 "Let's Start" 或 "联机小厅" 都能找到。</p>
 */
public final class LstModListNames {
    public static final String NAME_KEY = "neoforge.screen.mods.info.displayname." + LetsStartTogether.MOD_ID;

    private LstModListNames() {
    }

    public static Component searchName(String modId, Component shown) {
        if (!LetsStartTogether.MOD_ID.equals(modId)) {
            return shown;
        }
        String original = ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse("");
        String translated = shown.getString();
        return original.isEmpty() || translated.equals(original) ? shown : Component.literal(translated + " " + original);
    }
}
