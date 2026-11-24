package io.github.lwarqiu1shz.letsstarttogether.client;

import io.github.lwarqiu1shz.letsstarttogether.lobby.LobbyWorlds;
import io.github.lwarqiu1shz.letsstarttogether.mixin.client.EditWorldScreenAccessor;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.world.level.storage.LevelResource;

/**
 * 联机小厅的两个界面按钮，以及它们在原版界面里的摆放位置。
 *
 * <p>“多人游戏”界面（{@link JoinMultiplayerScreen}）底部原本是两行按钮：上面一行三个
 * （加入服务器 / 直接连接 / 添加服务器），下面一行四个（编辑 / 删除 / 刷新 / 返回）。
 * 我们把上面一行改成四个等宽按钮，把“创建多人世界”放在最左边。</p>
 *
 * <p>存档“编辑”界面（{@link EditWorldScreen}）在“优化世界”和“保存 / 取消”之间留了一个
 * 20 格高的空位（{@code SpacerElement(200, 20)}，再加 5 的间距）。我们把“开关联机小厅”
 * 正好放进这个空位，所以整个面板的高度和原版完全一样。</p>
 *
 * <p>按钮本身在 Fabric 的 {@code ScreenEvents.AFTER_INIT} 里加到界面上；之后窗口每变一次大小，
 * 原版界面会重新排列自己的控件，两个 Mixin 会在 {@code repositionElements} 之后把我们的
 * 按钮（以及被我们改过的原版按钮）重新摆好。</p>
 */
public final class LobbyButtons {
    /** 多人游戏界面里新按钮的翻译键。 */
    public static final String CREATE_WORLD_KEY = "lets_start_together.button.create_multiplayer_world";
    /** 存档编辑界面里新按钮的翻译键。 */
    public static final String TOGGLE_LOBBY_KEY = "lets_start_together.button.toggle_lobby";

    /** 原版底部两行按钮的宽度：上一行 100、下一行 74；我们把上一行改成和下一行一样的 74。 */
    private static final int JOIN_ROW_BUTTON_WIDTH = 74;
    /** 原版底部按钮之间的间距。 */
    private static final int JOIN_ROW_SPACING = 4;

    /** 存档编辑界面里按钮的宽度，和原版那一列按钮保持一致。 */
    private static final int EDIT_BUTTON_WIDTH = 200;
    private static final int EDIT_BUTTON_HEIGHT = 20;
    /** 空位高 20，加上布局的 5 格间距，就是“保存”那一行往上 25。 */
    private static final int EDIT_BUTTON_GAP_ABOVE_SAVE = EDIT_BUTTON_HEIGHT + 5;

    private LobbyButtons() {
    }

    static void onScreenInit(Screen screen) {
        if (screen instanceof JoinMultiplayerScreen) {
            // 有的模组会让界面再初始化一次而不清掉旧控件；已经有按钮就别再加。
            if (findButton(screen, CREATE_WORLD_KEY) != null) {
                layoutJoinRow(screen);
                return;
            }
            Screens.getWidgets(screen).add(Button.builder(Component.translatable(CREATE_WORLD_KEY), button -> createLobbyWorld(screen))
                    .width(JOIN_ROW_BUTTON_WIDTH)
                    .tooltip(Tooltip.create(Component.translatable(CREATE_WORLD_KEY + ".tooltip")))
                    .build());
            layoutJoinRow(screen);
        } else if (screen instanceof EditWorldScreen) {
            if (findButton(screen, TOGGLE_LOBBY_KEY) != null) {
                layoutEditRow(screen);
                return;
            }
            Path worldDir = ((EditWorldScreenAccessor) screen).lets_start_together$levelAccess().getLevelPath(LevelResource.ROOT);
            Screens.getWidgets(screen).add(Button.builder(toggleLabel(LobbyWorlds.isLobby(worldDir)), button -> {
                        boolean lobby = !LobbyWorlds.isLobby(worldDir);
                        LobbyWorlds.setLobby(worldDir, lobby);
                        LobbyCrowns.invalidate();
                        button.setMessage(toggleLabel(LobbyWorlds.isLobby(worldDir)));
                    })
                    .width(EDIT_BUTTON_WIDTH)
                    .build());
            layoutEditRow(screen);
        } else if (screen instanceof SelectWorldScreen) {
            // 存档可能在别处被改过（复制、在存档编辑界面里开关），每次打开列表重新读一遍。
            LobbyCrowns.invalidate();
            // 从单人游戏打开“创建新的世界”时，“是否为多人世界”默认“否”。
            LobbyCreateState.openNextAsLobby(false);
        }
    }

    /** 按钮文字带着当前状态：“联机小厅：开 / 关”；翻译键不变，所以 findButton 仍然找得到它。 */
    private static Component toggleLabel(boolean lobby) {
        return Component.translatable(TOGGLE_LOBBY_KEY, lobby ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    /**
     * 打开原版的“创建新的世界”界面，“是否为多人世界”默认是“是”；选“是”建好的世界是联机小厅世界，房主先进小厅。
     * 取消时回到多人游戏界面。
     */
    private static void createLobbyWorld(Screen multiplayerScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        // “是否为多人世界”默认选“是”；真正打标记在 CreateWorldScreenMixin 里，按界面上的选择来。
        LobbyCreateState.openNextAsLobby(true);
        CreateWorldScreen.openFresh(minecraft, () -> minecraft.setScreen(multiplayerScreen));
    }

    /**
     * 把“多人游戏”界面底部上面一行摆成四个等宽按钮：创建多人世界 | 加入服务器 | 直接连接 | 添加服务器。
     * 四个 74 宽 + 三个 4 间距正好是 308，和下面一行四个按钮的总宽度一样，所以两行仍然上下对齐。
     */
    public static void layoutJoinRow(Screen screen) {
        Button create = findButton(screen, CREATE_WORLD_KEY);
        Button join = findButton(screen, "selectServer.select");
        Button direct = findButton(screen, "selectServer.direct");
        Button add = findButton(screen, "selectServer.add");
        if (create == null || join == null || direct == null || add == null) {
            return;
        }
        int rowWidth = JOIN_ROW_BUTTON_WIDTH * 4 + JOIN_ROW_SPACING * 3;
        int left = (screen.width - rowWidth) / 2;
        int y = join.getY();
        int step = JOIN_ROW_BUTTON_WIDTH + JOIN_ROW_SPACING;
        place(create, left, y);
        place(join, left + step, y);
        place(direct, left + step * 2, y);
        place(add, left + step * 3, y);
    }

    /**
     * 把“开关联机小厅”放进存档编辑界面里“优化世界”与“保存 / 取消”之间的空位。
     */
    public static void layoutEditRow(Screen screen) {
        Button toggle = findButton(screen, TOGGLE_LOBBY_KEY);
        Button save = findButton(screen, "selectWorld.edit.save");
        if (toggle == null || save == null) {
            return;
        }
        toggle.setWidth(EDIT_BUTTON_WIDTH);
        toggle.setHeight(EDIT_BUTTON_HEIGHT);
        toggle.setX((screen.width - EDIT_BUTTON_WIDTH) / 2);
        toggle.setY(save.getY() - EDIT_BUTTON_GAP_ABOVE_SAVE);
    }

    private static void place(AbstractWidget widget, int x, int y) {
        widget.setX(x);
        widget.setY(y);
        widget.setWidth(JOIN_ROW_BUTTON_WIDTH);
    }

    /** 按翻译键在界面里找按钮；原版按钮的文字都是纯翻译键，不会带参数。 */
    private static Button findButton(Screen screen, String translationKey) {
        for (GuiEventListener listener : screen.children()) {
            if (listener instanceof Button button && hasTranslationKey(button.getMessage(), translationKey)) {
                return button;
            }
        }
        return null;
    }

    private static boolean hasTranslationKey(Component message, String translationKey) {
        return message.getContents() instanceof TranslatableContents contents && contents.getKey().equals(translationKey);
    }
}
