package io.github.lwarqiu1shz.letsstarttogether.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Mod Menu 模组列表里的“配置”按钮；没装 Mod Menu 时不会加载这个类。 */
public final class LobbyModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LobbyConfigScreen::new;
    }
}
