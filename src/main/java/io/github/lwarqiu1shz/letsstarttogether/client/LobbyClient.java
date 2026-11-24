package io.github.lwarqiu1shz.letsstarttogether.client;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyChatPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyHostLinePayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyKickPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyLinePayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyStartPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyStatePayload;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;

/**
 * 客户端这边的联机小厅：收到服务器发来的小厅状态就打开 {@link LobbyScreen}，
 * 一直到真正进入世界（或断开）为止。
 */
@EventBusSubscriber(modid = LetsStartTogether.MOD_ID, value = Dist.CLIENT)
public final class LobbyClient {
    private static final int MAX_LOG = 100;

    private static @Nullable LobbyStatePayload state;
    private static @Nullable Connection connection;
    private static @Nullable LobbyScreen screen;
    /**
     * 小厅里显示的聊天记录，由服务器统一发来，所有人看到的一样（玩家发言、房主那边 mcwifipnp / e4mc 给的地址等）。
     * 它们同时也进原版聊天栏，进世界以后还能翻到。
     */
    private static final List<Component> LOG = new CopyOnWriteArrayList<>();
    private static volatile int logVersion;
    /** 正在把小厅消息放进原版聊天栏：这时 ChatComponentMixin 不用再抄一份。 */
    private static boolean addingToChat;

    private LobbyClient() {
    }

    @SubscribeEvent
    public static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(LobbyStatePayload.TYPE, LobbyClient::handleState);
        event.register(LobbyLinePayload.TYPE, LobbyClient::handleLine);
    }

    private static void handleLine(LobbyLinePayload payload, IPayloadContext context) {
        if (connection != context.connection()) {
            return;
        }
        addToLog(payload.line());
        // 房主那边的提示本来就在房主自己的聊天栏里了。
        if (!(payload.fromHost() && isHost())) {
            addingToChat = true;
            try {
                Minecraft.getInstance().gui.getChat().addClientSystemMessage(payload.line());
            } finally {
                addingToChat = false;
            }
        }
    }

    public static void sendChat(String text) {
        String trimmed = text.strip();
        if (!trimmed.isEmpty() && connection != null && connection.isConnected()) {
            connection.send(new ServerboundCustomPayloadPacket(new LobbyChatPayload(
                    trimmed.length() > LobbyChatPayload.MAX_LENGTH ? trimmed.substring(0, LobbyChatPayload.MAX_LENGTH) : trimmed)));
        }
    }

    public static void sendKick(UUID player) {
        if (connection != null && connection.isConnected()) {
            connection.send(new ServerboundCustomPayloadPacket(new LobbyKickPayload(player)));
        }
    }

    private static void handleState(LobbyStatePayload payload, IPayloadContext context) {
        Connection from = context.connection();
        if (connection != from) {
            reset();
            connection = from;
        }
        state = payload;
        if (screen == null) {
            screen = new LobbyScreen();
            Minecraft.getInstance().setScreen(screen);
        }
    }

    public static boolean inLobby() {
        return connection != null && state != null;
    }

    public static @Nullable LobbyStatePayload state() {
        return state;
    }

    /** 这台电脑就是房主（世界开在本机上）。 */
    public static boolean isHost() {
        return connection != null && connection.isMemoryConnection();
    }

    /**
     * 聊天栏收到一条消息。在小厅里时：房主把它发给服务器，让小厅里每个人都看到（IP、e4mc 地址…）；
     * 其他人只在自己的小厅里显示。
     */
    public static void onChatMessage(Component message) {
        Connection current = connection;
        if (current == null || addingToChat) {
            return;
        }
        if (current.isMemoryConnection()) {
            if (current.isConnected()) {
                current.send(new ServerboundCustomPayloadPacket(new LobbyHostLinePayload(message)));
            }
        } else {
            Minecraft.getInstance().execute(() -> addToLog(message));
        }
    }

    private static void addToLog(Component message) {
        LOG.add(message);
        while (LOG.size() > MAX_LOG) {
            LOG.removeFirst();
        }
        logVersion++;
    }

    /** mcwifipnp 想发给房主、但房主还没进世界（服务器上没有这个玩家）时，改发进聊天栏。 */
    public static void showToHost(Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.gui.getChat().addClientSystemMessage(message));
    }

    public static List<Component> log() {
        return LOG;
    }

    public static int logVersion() {
        return logVersion;
    }

    public static void sendStart() {
        if (connection != null && connection.isConnected()) {
            connection.send(new ServerboundCustomPayloadPacket(LobbyStartPayload.INSTANCE));
        }
    }

    /** 离开小厅：房主就是关掉这个世界，其他人就是断开连接。 */
    public static void leave() {
        Minecraft minecraft = Minecraft.getInstance();
        Connection leaving = connection;
        boolean host = isHost();
        reset();
        if (host) {
            minecraft.disconnect(new TitleScreen(), false);
        } else {
            if (leaving != null) {
                leaving.disconnect(Component.translatable("multiplayer.status.quitting"));
            }
            minecraft.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
        }
    }

    private static void reset() {
        state = null;
        connection = null;
        screen = null;
        LOG.clear();
        logVersion++;
    }

    /**
     * mcwifipnp 的设置界面点“完成”后会 {@code setScreen(null)}；还没进世界时那会变成标题界面，
     * 这里把它换回小厅。
     */
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (connection == null) {
            return;
        }
        if (!connection.isConnected()) {
            reset();
            return;
        }
        if (screen != null && event.getNewScreen() instanceof TitleScreen) {
            event.setNewScreen(screen);
        }
    }

    /**
     * 连到别人的世界时，原版靠“正在连接”界面来驱动连接；我们把那个界面换成了小厅，所以这里接着驱动它。
     * 房主自己的连接由游戏本身驱动，不用管。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Connection current = connection;
        if (current == null || current.isMemoryConnection()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null || minecraft.screen instanceof ConnectScreen) {
            return;
        }
        if (current.isConnected()) {
            current.tick();
        } else {
            reset();
            current.handleDisconnection();
        }
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        reset();
    }
}
