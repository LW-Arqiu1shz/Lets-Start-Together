package io.github.lwarqiu1shz.letsstarttogether.lobby;

import com.mojang.authlib.GameProfile;
import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import org.jspecify.annotations.Nullable;

/**
 * 一个玩家在联机小厅里等着的这一步。
 *
 * <p>它是连接“配置阶段”里的一个任务，排在原版“准备出生点 / 进入世界”之前。任务没结束，
 * 玩家就一直停在配置阶段：服务器上没有这个玩家的实体，也不会触发任何“玩家登录”事件。
 * 房主点了开始以后，{@link #tick()} 返回 true，所有人的任务在同一刻结束。</p>
 */
public final class LobbyTask implements ICustomConfigurationTask {
    public static final Type TYPE = new Type(Identifier.fromNamespaceAndPath(LetsStartTogether.MOD_ID, "lobby"));

    final MinecraftServer server;
    final ServerConfigurationPacketListenerImpl listener;
    final GameProfile profile;
    final boolean host;
    @Nullable Consumer<CustomPacketPayload> sender;
    /** 这个玩家已经收到的小厅状态版本。 */
    int sentVersion = -1;
    boolean joined;

    LobbyTask(MinecraftServer server, ServerConfigurationPacketListenerImpl listener) {
        this.server = server;
        this.listener = listener;
        this.profile = listener.getOwner();
        this.host = server.isSingleplayerOwner(new NameAndId(this.profile));
    }

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        this.sender = sender;
        this.joined = LobbyServer.join(this);
    }

    @Override
    public boolean tick() {
        return this.joined && LobbyServer.tickMember(this);
    }

    boolean connected() {
        return this.listener.getConnection().isConnected();
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
