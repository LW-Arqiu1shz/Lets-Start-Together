package io.github.lwarqiu1shz.letsstarttogether.client;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** 只在客户端加载：注册小厅显示设置，并把配置界面挂到模组列表的“配置”按钮上。 */
@Mod(value = LetsStartTogether.MOD_ID, dist = Dist.CLIENT)
public final class LetsStartTogetherClient {
    public LetsStartTogetherClient(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, LobbyConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
