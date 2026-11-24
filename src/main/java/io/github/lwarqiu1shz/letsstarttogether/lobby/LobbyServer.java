package io.github.lwarqiu1shz.letsstarttogether.lobby;

import io.github.lwarqiu1shz.letsstarttogether.network.LobbyChatPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyHostLinePayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyKickPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyLinePayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyStatePayload;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelResource;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import org.jspecify.annotations.Nullable;

/**
 * 服务器这边的联机小厅：谁在小厅里、几个位置、开没开放、开没开始。
 *
 * <p>只在单人世界（房主自己电脑上的服务器）里起作用；专用服务器上本模组什么都不做。
 * 所有方法都在服务器线程上调用（配置任务和配置阶段的包都在那里处理）。</p>
 *
 * <p>世界不会走时间：小厅里的人都还没进世界，服务器上一个玩家都没有，单人服务器这时本来就会暂停，
 * 只处理网络连接。</p>
 */
public final class LobbyServer {
    private static final Component LOBBY_FULL = Component.translatable("multiplayer.disconnect.server_full");
    private static final Component KICKED = Component.translatable("lets_start_together.lobby.kicked");
    /** 发给没装本模组的人：他们没有本模组的译文，所以直接写中英两句。 */
    private static final Component NEEDS_MOD =
            Component.literal("这个世界需要安装“联机小厅”模组。\nThis world needs the Let's Start Together! mod.");
    private static final long POLL_INTERVAL_MS = 500L;
    private static final int MAX_HISTORY = 100;

    /** 从“创建多人世界”进来时由客户端设上：下一个启动的单人服务器要标成联机小厅世界。 */
    private static volatile boolean createNextAsLobby;

    /** 这个世界有联机小厅，而且还没开始：新连进来的人先进小厅。 */
    private static boolean active;
    /** 房主点了开始。 */
    private static boolean started;
    private static final List<LobbyTask> MEMBERS = new ArrayList<>();
    /** 小厅聊天记录：后进来的人也能看到之前的聊天和房主发的地址。 */
    private static final List<LobbyLinePayload> HISTORY = new ArrayList<>();
    /** 小厅状态每变一次 +1；每个玩家的任务看到版本变了就把新状态发给自己的客户端。 */
    private static int version;
    private static int maxPlayers = 1;
    private static boolean published;
    private static int port = -1;
    private static long lastPoll;

    private LobbyServer() {
    }

    public static void markNextWorldAsLobby(boolean lobby) {
        createNextAsLobby = lobby;
    }

    public static void onServerStarting(MinecraftServer server) {
        reset();
        boolean markNew = createNextAsLobby;
        createNextAsLobby = false;
        if (server.isDedicatedServer()) {
            return;
        }
        Path root = server.getWorldPath(LevelResource.ROOT);
        if (markNew) {
            LobbyWorlds.setLobby(root, true);
        }
        active = LobbyWorlds.isLobby(root);
    }

    public static void onServerStopped() {
        reset();
    }

    private static void reset() {
        active = false;
        started = false;
        MEMBERS.clear();
        HISTORY.clear();
        version = 0;
        maxPlayers = 1;
        published = false;
        port = -1;
        lastPoll = 0L;
    }

    public static void registerTask(ServerConfigurationPacketListenerImpl listener, MinecraftServer server) {
        if (!active || started) {
            return;
        }
        // 没装本模组的客户端收不到小厅的包，会一直卡在“正在加入世界”，直接告诉它原因。
        if (!ServerConfigurationNetworking.canSend(listener, LobbyStatePayload.TYPE)) {
            listener.disconnect(NEEDS_MOD);
            return;
        }
        listener.addTask(new LobbyTask(server, listener));
    }

    static boolean join(LobbyTask task) {
        pruneDisconnected();
        poll(task.server, true);
        if (!task.host && MEMBERS.size() >= maxPlayers) {
            task.listener.disconnect(LOBBY_FULL);
            return false;
        }
        if (task.host) {
            MEMBERS.addFirst(task);
        } else {
            MEMBERS.add(task);
        }
        version++;
        if (task.sender != null) {
            HISTORY.forEach(task.sender);
        }
        return true;
    }

    /** 每个玩家的任务每刻调用一次；返回 true 表示这个玩家可以进世界了。 */
    static boolean tickMember(LobbyTask task) {
        pruneDisconnected();
        poll(task.server, false);
        if (task.sentVersion != version && task.sender != null) {
            task.sentVersion = version;
            task.sender.accept(state());
        }
        if (started) {
            MEMBERS.remove(task);
            return true;
        }
        return false;
    }

    private static void pruneDisconnected() {
        if (MEMBERS.removeIf(member -> !member.connected())) {
            version++;
        }
    }

    /** 最大人数和开放状态都在 mcwifipnp 的界面里改，这里每半秒看一次有没有变。 */
    private static void poll(MinecraftServer server, boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastPoll < POLL_INTERVAL_MS) {
            return;
        }
        lastPoll = now;
        int newMax = Math.max(1, server.getMaxPlayers());
        boolean newPublished = server.isPublished();
        int newPort = newPublished ? server.getPort() : -1;
        if (newMax != maxPlayers || newPublished != published || newPort != port) {
            maxPlayers = newMax;
            published = newPublished;
            port = newPort;
            version++;
        }
    }

    private static LobbyStatePayload state() {
        return new LobbyStatePayload(MEMBERS.stream().map(member -> member.profile).toList(),
                maxPlayers, published, port, started);
    }

    /** 房主点了“开始游戏”：所有人一起进世界，之后再连进来的人直接进世界。 */
    public static void handleStart(MinecraftServer server, ServerConfigurationPacketListenerImpl listener) {
        if (!active || started || !server.isSingleplayerOwner(new NameAndId(listener.getOwner()))) {
            return;
        }
        started = true;
        active = false;
        version++;
    }

    private static @Nullable LobbyTask member(ServerConfigurationPacketListenerImpl listener) {
        for (LobbyTask member : MEMBERS) {
            if (member.listener == listener) {
                return member;
            }
        }
        return null;
    }

    private static void broadcast(LobbyLinePayload line) {
        HISTORY.add(line);
        while (HISTORY.size() > MAX_HISTORY) {
            HISTORY.removeFirst();
        }
        for (LobbyTask member : MEMBERS) {
            if (member.sender != null && member.connected()) {
                member.sender.accept(line);
            }
        }
    }

    /** 小厅聊天：发给小厅里所有人，并记下来给后进来的人看。 */
    public static void handleChat(LobbyChatPayload payload, ServerConfigurationPacketListenerImpl listener) {
        LobbyTask from = member(listener);
        String text = StringUtil.trimChatMessage(StringUtil.filterText(payload.text())).strip();
        if (from == null || text.isEmpty()) {
            return;
        }
        broadcast(new LobbyLinePayload(Component.translatable("chat.type.text", from.profile.name(), text), false));
    }

    /** 房主聊天栏里的联机提示（IP、e4mc 地址…），转给所有人。只收房主发来的。 */
    public static void handleHostLine(LobbyHostLinePayload payload, ServerConfigurationPacketListenerImpl listener) {
        LobbyTask from = member(listener);
        if (from != null && from.host) {
            broadcast(new LobbyLinePayload(payload.line(), true));
        }
    }

    /** 房主把某人请出小厅。 */
    public static void handleKick(LobbyKickPayload payload, ServerConfigurationPacketListenerImpl listener) {
        LobbyTask from = member(listener);
        if (from == null || !from.host) {
            return;
        }
        for (LobbyTask member : List.copyOf(MEMBERS)) {
            if (!member.host && member.profile.id().equals(payload.player())) {
                member.listener.disconnect(KICKED);
            }
        }
        pruneDisconnected();
    }
}
