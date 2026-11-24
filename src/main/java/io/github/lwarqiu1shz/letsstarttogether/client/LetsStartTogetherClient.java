package io.github.lwarqiu1shz.letsstarttogether.client;

import io.github.lwarqiu1shz.letsstarttogether.network.LobbyLinePayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

/** 客户端入口：读小厅显示设置，注册小厅的包和界面按钮。 */
public final class LetsStartTogetherClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LobbyConfig.load();
        ClientConfigurationNetworking.registerGlobalReceiver(LobbyStatePayload.TYPE, LobbyClient::handleState);
        ClientConfigurationNetworking.registerGlobalReceiver(LobbyLinePayload.TYPE, LobbyClient::handleLine);
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> LobbyButtons.onScreenInit(screen));
        ClientTickEvents.END_CLIENT_TICK.register(client -> LobbyClient.onClientTick());
        ClientPlayConnectionEvents.INIT.register((handler, client) -> LobbyClient.onLoggingIn());
    }
}
