package io.github.lwarqiu1shz.letsstarttogether.client;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

/**
 * “创建新的世界”界面里“是否为多人世界”的选择。
 * 从“多人游戏”里的“创建多人世界”打开时默认“是”，从单人游戏打开时默认“否”。
 */
public final class LobbyCreateState {
    /** 下一个打开的“创建新的世界”界面默认选“是”。 */
    private static boolean nextIsLobby;
    private static final Map<CreateWorldScreen, Boolean> LOBBY = new WeakHashMap<>();

    private LobbyCreateState() {
    }

    public static void openNextAsLobby(boolean lobby) {
        nextIsLobby = lobby;
    }

    /** 界面刚建好时调用：记下它的默认值。 */
    public static void claim(CreateWorldScreen screen) {
        LOBBY.put(screen, nextIsLobby);
        nextIsLobby = false;
    }

    public static boolean isLobby(CreateWorldScreen screen) {
        return LOBBY.getOrDefault(screen, false);
    }

    public static void setLobby(CreateWorldScreen screen, boolean lobby) {
        LOBBY.put(screen, lobby);
    }
}
