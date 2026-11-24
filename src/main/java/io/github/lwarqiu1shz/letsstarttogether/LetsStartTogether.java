package io.github.lwarqiu1shz.letsstarttogether;

import io.github.lwarqiu1shz.letsstarttogether.lobby.LobbyServer;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyChatPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyHostLinePayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyKickPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyLinePayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyStartPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyStatePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;

/**
 * 联机小厅（Let's Start Together!）。
 *
 * <p>做法：联机小厅世界里，每个连进来的玩家（包括房主自己）都停在连接的“配置阶段”，
 * 也就是还没有生成玩家、没有进入世界的那一步（见 {@link io.github.lwarqiu1shz.letsstarttogether.lobby.LobbyTask}）。
 * 服务器上一个玩家都没有，单人服务器会自己暂停，所以世界不走时间。房主点“开始游戏”后，
 * 所有人的这一步同时结束，大家一起进入世界；之后各个模组的“玩家登录”事件（剧情开场等）才会触发。</p>
 */
public final class LetsStartTogether implements ModInitializer {
    public static final String MOD_ID = "lets_start_together";

    @Override
    public void onInitialize() {
        // 客户端处理器在 LetsStartTogetherClient 里注册。没装本模组的客户端由 LobbyServer.registerTask 请出去。
        PayloadTypeRegistry.clientboundConfiguration().register(LobbyStatePayload.TYPE, LobbyStatePayload.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(LobbyLinePayload.TYPE, LobbyLinePayload.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(LobbyStartPayload.TYPE, LobbyStartPayload.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(LobbyChatPayload.TYPE, LobbyChatPayload.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(LobbyHostLinePayload.TYPE, LobbyHostLinePayload.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(LobbyKickPayload.TYPE, LobbyKickPayload.CODEC);

        ServerConfigurationNetworking.registerGlobalReceiver(LobbyStartPayload.TYPE,
                (payload, context) -> LobbyServer.handleStart(context.server(), context.packetListener()));
        ServerConfigurationNetworking.registerGlobalReceiver(LobbyChatPayload.TYPE,
                (payload, context) -> LobbyServer.handleChat(payload, context.packetListener()));
        ServerConfigurationNetworking.registerGlobalReceiver(LobbyHostLinePayload.TYPE,
                (payload, context) -> LobbyServer.handleHostLine(payload, context.packetListener()));
        ServerConfigurationNetworking.registerGlobalReceiver(LobbyKickPayload.TYPE,
                (payload, context) -> LobbyServer.handleKick(payload, context.packetListener()));

        ServerConfigurationConnectionEvents.CONFIGURE.register(LobbyServer::registerTask);
        ServerLifecycleEvents.SERVER_STARTING.register(LobbyServer::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> LobbyServer.onServerStopped());
    }
}
