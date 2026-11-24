package io.github.lwarqiu1shz.letsstarttogether;

import io.github.lwarqiu1shz.letsstarttogether.lobby.LobbyServer;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyChatPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyHostLinePayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyKickPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyLinePayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyStartPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyStatePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * 联机小厅（Let's Start Together!）。
 *
 * <p>做法：联机小厅世界里，每个连进来的玩家（包括房主自己）都停在连接的“配置阶段”，
 * 也就是还没有生成玩家、没有进入世界的那一步（见 {@link io.github.lwarqiu1shz.letsstarttogether.lobby.LobbyTask}）。
 * 服务器上一个玩家都没有，单人服务器会自己暂停，所以世界不走时间。房主点“开始游戏”后，
 * 所有人的这一步同时结束，大家一起进入世界；之后各个模组的“玩家登录”事件（剧情开场等）才会触发。</p>
 */
@Mod(LetsStartTogether.MOD_ID)
public final class LetsStartTogether {
    public static final String MOD_ID = "lets_start_together";

    public LetsStartTogether(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(LetsStartTogether::registerPayloads);
        modEventBus.addListener(LetsStartTogether::registerConfigurationTasks);
        NeoForge.EVENT_BUS.register(LobbyServer.class);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // 这些包都是必需的：没装本模组的客户端连不进联机小厅世界。
        // 客户端处理器在 LobbyClient 里注册（RegisterClientPayloadHandlersEvent）。
        event.registrar("2")
                .configurationToClient(LobbyStatePayload.TYPE, LobbyStatePayload.CODEC)
                .configurationToClient(LobbyLinePayload.TYPE, LobbyLinePayload.CODEC)
                .configurationToServer(LobbyStartPayload.TYPE, LobbyStartPayload.CODEC, LobbyServer::handleStart)
                .configurationToServer(LobbyChatPayload.TYPE, LobbyChatPayload.CODEC, LobbyServer::handleChat)
                .configurationToServer(LobbyHostLinePayload.TYPE, LobbyHostLinePayload.CODEC, LobbyServer::handleHostLine)
                .configurationToServer(LobbyKickPayload.TYPE, LobbyKickPayload.CODEC, LobbyServer::handleKick);
    }

    private static void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
        LobbyServer.registerTask(event);
    }
}
