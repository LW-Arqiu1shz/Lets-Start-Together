package io.github.lwarqiu1shz.letsstarttogether.client;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import io.github.lwarqiu1shz.letsstarttogether.lobby.LobbyWorlds;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** 存档列表的小皇冠：哪些存档是联机小厅世界（缓存起来，不每帧读文件），以及皇冠画在哪。 */
public final class LobbyCrowns {
    private static final Identifier CROWN = Identifier.fromNamespaceAndPath(LetsStartTogether.MOD_ID, "crown");
    private static final int WIDTH = 16;
    private static final int HEIGHT = 10;
    private static final int ICON_SIZE = 32;
    private static final Map<String, Boolean> CACHE = new HashMap<>();

    private LobbyCrowns() {
    }

    public static boolean isLobby(String levelId) {
        return CACHE.computeIfAbsent(levelId,
                id -> LobbyWorlds.isLobby(Minecraft.getInstance().getLevelSource().getBaseDir().resolve(id)));
    }

    public static void invalidate() {
        CACHE.clear();
    }

    /** 皇冠居中压在图标上边缘，往上探出 3 像素。 */
    public static void extract(GuiGraphicsExtractor graphics, int iconX, int iconY) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CROWN, iconX + (ICON_SIZE - WIDTH) / 2, iconY - 3, WIDTH, HEIGHT);
    }
}
