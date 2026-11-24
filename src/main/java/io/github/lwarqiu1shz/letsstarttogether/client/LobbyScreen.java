package io.github.lwarqiu1shz.letsstarttogether.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.authlib.GameProfile;
import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyChatPayload;
import io.github.lwarqiu1shz.letsstarttogether.network.LobbyStatePayload;
import io.github.satxm.mcwifipnp.client.ShareToLanScreenNew;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.Nullable;

/**
 * 联机小厅界面。背景就是原版标题界面的全景图（跟着游戏版本和材质包走）。
 *
 * <p>中间是大家的头像：第一个位置是房主（金边 + 小皇冠）；开放联机后，按 mcwifipnp 里设的人数画出空位
 * （深灰底、白边），别人按加入顺序坐进去。1~4 人一行；5~10 人分两行（多的那行在上）；再多就挑头像能画得最大的行数。
 * 每行都居中。头像默认 64（GUI 像素），放不下才缩小：最宽的那行两边都要留出一个头像宽的空。</p>
 *
 * <p>左下是原版样式的聊天（黑条），所有人看到的一样，可以滚动。房主右键别人的头像可以把人请出去。</p>
 */
public final class LobbyScreen extends Screen {
    private static final Identifier CROWN = Identifier.fromNamespaceAndPath(LetsStartTogether.MOD_ID, "crown");
    private static final int BUTTON_WIDTH = 98;
    private static final int BUTTON_GAP = 4;
    private static final int CHAT_WIDTH = 320;
    private static final int CHAT_LINES = 7;
    private static final int LINE_HEIGHT = 9;
    private static final int SCROLL_STEP = 3;
    /** 位置数的上限（防止人数设得离谱时卡死）；画面上靠翻页显示。 */
    private static final int MAX_SLOTS = 10000;
    private static final int PAGE_BUTTON = 20;
    private static final int MIN_HEAD = 8;
    private static final int SLOT_BACKGROUND = 0xFF3A3A3A;
    private static final int SLOT_BORDER = 0xFFFFFFFF;
    private static final int HOST_BORDER = 0xFFFFC21F;
    private static final int HOST_NAME = 0xFFFFD866;
    private static final int STATUS_COLOR = 0xFFC0C0C0;
    private static final int BUBBLE_BACKGROUND = 0xF0101010;
    private static final int BUBBLE_WIDTH_MIN = 96;
    private static final int BUBBLE_BUTTON = 44;

    private final Map<UUID, Supplier<PlayerSkin>> skins = new HashMap<>();
    /** 上一帧画出来的头像位置，用来判断右键点到了谁。 */
    private final List<Slot> slots = new ArrayList<>();
    private @Nullable EditBox chatBox;
    private @Nullable Button startButton;
    private String draft = "";

    private final List<FormattedCharSequence> chatLines = new ArrayList<>();
    private int wrappedVersion = -1;
    private int wrappedWidth = -1;
    /** 往上翻了几行（0 = 最新）。 */
    private int chatScroll;

    private @Nullable GameProfile kickTarget;
    private int bubbleX;
    private int bubbleY;
    private @Nullable Button kickButton;
    private @Nullable Button cancelButton;
    private boolean closeBubbleLater;

    /** 头像怎么排：每页几行、每行几个，头像多大，一页几个，共几页。 */
    private record Layout(int[] rows, int size, int perPage, int pages, boolean paged) {
    }

    private @Nullable Layout layout;
    private int layoutKey;
    private int page;
    private @Nullable Button prevPage;
    private @Nullable Button nextPage;

    private record Slot(int x, int y, int size, int index, @Nullable GameProfile profile) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.size && mouseY >= this.y && mouseY < this.y + this.size;
        }
    }

    public LobbyScreen() {
        super(Component.translatable("lets_start_together.lobby.title"));
    }

    @Override
    protected void init() {
        this.kickButton = null;
        this.cancelButton = null;
        this.kickTarget = null;
        boolean host = LobbyClient.isHost();
        int buttonY = this.buttonY();
        int buttons = host ? 3 : 1;
        int rowWidth = buttons * BUTTON_WIDTH + (buttons - 1) * BUTTON_GAP;
        int x = (this.width - rowWidth) / 2;
        if (host) {
            this.addRenderableWidget(Button.builder(Component.translatable("lets_start_together.lobby.settings"), button -> this.openSettings())
                    .bounds(x, buttonY, BUTTON_WIDTH, 20).build());
            x += BUTTON_WIDTH + BUTTON_GAP;
            this.startButton = this.addRenderableWidget(Button.builder(Component.translatable("lets_start_together.lobby.start"), button -> {
                        button.active = false;
                        LobbyClient.sendStart();
                    })
                    .bounds(x, buttonY, BUTTON_WIDTH, 20).build());
            x += BUTTON_WIDTH + BUTTON_GAP;
        }
        this.addRenderableWidget(Button.builder(Component.translatable("lets_start_together.lobby.leave"), button -> LobbyClient.leave())
                .bounds(x, buttonY, BUTTON_WIDTH, 20).build());

        // 原版聊天输入栏那样：没有边框，底下一条半透明黑条（黑条在 extractChat 里画）。
        this.chatBox = new EditBox(this.font, this.chatX() + 2, this.chatBarY() + 2, this.chatWidth() - 4, 12,
                Component.translatable("lets_start_together.lobby.chat"));
        this.chatBox.setBordered(false);
        this.chatBox.setMaxLength(LobbyChatPayload.MAX_LENGTH);
        this.chatBox.setHint(Component.translatable("lets_start_together.lobby.chat"));
        this.chatBox.setValue(this.draft);
        this.chatBox.setResponder(value -> this.draft = value);
        this.addRenderableWidget(this.chatBox);
        this.setInitialFocus(this.chatBox);
        this.prevPage = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.page--)
                .bounds(0, 0, PAGE_BUTTON, PAGE_BUTTON).build());
        this.nextPage = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.page++)
                .bounds(0, 0, PAGE_BUTTON, PAGE_BUTTON).build());
        this.prevPage.visible = false;
        this.nextPage.visible = false;
        this.layout = null;
        this.wrappedVersion = -1;
    }

    private int buttonY() {
        return this.height - 28;
    }

    private int chatX() {
        return 8;
    }

    private int chatWidth() {
        return Math.min(CHAT_WIDTH, this.width - 16);
    }

    /** 输入栏黑条的上边。 */
    private int chatBarY() {
        return this.buttonY() - 18;
    }

    /** 聊天记录最底下一行的下边。 */
    private int chatLogBottom() {
        return this.chatBarY() - 2;
    }

    /** 聊天记录最多占到这里（头像只能画在这上面）。 */
    private int chatLogTop() {
        return this.chatLogBottom() - CHAT_LINES * LINE_HEIGHT;
    }

    private void openSettings() {
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server != null) {
            this.minecraft.setScreen(new ShareToLanScreenNew(this, server.isPublished()));
        }
    }

    @Override
    public void tick() {
        if (this.closeBubbleLater) {
            this.closeBubble();
        }
        LobbyStatePayload state = LobbyClient.state();
        if (this.startButton != null && state != null && state.starting()) {
            this.startButton.active = false;
        }
        // 要踢的人已经走了，气泡也收起来。
        if (this.kickTarget != null && (state == null || state.members().stream().noneMatch(p -> p.id().equals(this.kickTarget.id())))) {
            this.closeBubble();
        }
    }

    // ---------------------------------------------------------------- 输入

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.kickTarget != null && event.isEscape()) {
            this.closeBubble();
            return true;
        }
        if (this.chatBox != null && this.chatBox.isFocused() && event.isConfirmation()) {
            LobbyClient.sendChat(this.chatBox.getValue());
            this.chatBox.setValue("");
            this.chatScroll = 0;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && LobbyClient.isHost()) {
            for (Slot slot : this.slots) {
                if (slot.index() > 0 && slot.profile() != null && slot.contains(event.x(), event.y())) {
                    this.openBubble(slot);
                    return true;
                }
            }
        }
        if (this.kickTarget != null) {
            boolean onBubbleButton = (this.kickButton != null && this.kickButton.isMouseOver(event.x(), event.y()))
                    || (this.cancelButton != null && this.cancelButton.isMouseOver(event.x(), event.y()));
            if (!onBubbleButton) {
                this.closeBubble();
            }
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            Style style = this.chatStyleAt(event.x(), event.y());
            if (style != null && style.getClickEvent() != null) {
                defaultHandleClickEvent(style.getClickEvent(), this.minecraft, this);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (x >= this.chatX() && x < this.chatX() + this.chatWidth() && y >= this.chatLogTop() && y < this.chatBarY() + 12) {
            this.chatScroll += (int) Math.signum(scrollY) * SCROLL_STEP;
            this.clampScroll();
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------------------------------------------------------------- 踢人气泡

    private void openBubble(Slot slot) {
        this.closeBubble();
        GameProfile target = slot.profile();
        if (target == null) {
            return;
        }
        this.kickTarget = target;
        int width = this.bubbleWidth();
        int height = this.bubbleHeight();
        this.bubbleX = Math.clamp(slot.x() + slot.size() / 2 - width / 2, 2, this.width - width - 2);
        int below = slot.y() + slot.size() + 6;
        this.bubbleY = below + height <= this.height - 2 ? below : Math.max(2, slot.y() - height - 6);
        int buttonsX = this.bubbleX + (width - (BUBBLE_BUTTON * 2 + 4)) / 2;
        int buttonsY = this.bubbleY + 6 + LINE_HEIGHT + 4;
        UUID id = target.id();
        this.kickButton = this.addRenderableWidget(Button.builder(Component.translatable("lets_start_together.lobby.kick"), button -> {
                    if (this.kickTarget != null) {
                        LobbyClient.sendKick(id);
                    }
                    this.closeBubbleLater = true;
                })
                .bounds(buttonsX, buttonsY, BUBBLE_BUTTON, 20).build());
        this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.closeBubbleLater = true)
                .bounds(buttonsX + BUBBLE_BUTTON + 4, buttonsY, BUBBLE_BUTTON, 20).build());
    }

    private Component bubbleText() {
        return Component.translatable("lets_start_together.lobby.kick_confirm", this.kickTarget == null ? "" : this.kickTarget.name());
    }

    private int bubbleWidth() {
        return Math.max(BUBBLE_WIDTH_MIN, this.font.width(this.bubbleText()) + 12);
    }

    private int bubbleHeight() {
        return 6 + LINE_HEIGHT + 4 + 20 + 6;
    }

    private void closeBubble() {
        this.closeBubbleLater = false;
        this.kickTarget = null;
        if (this.kickButton != null) {
            this.removeWidget(this.kickButton);
            this.kickButton = null;
        }
        if (this.cancelButton != null) {
            this.removeWidget(this.cancelButton);
            this.cancelButton = null;
        }
        if (this.getFocused() == null && this.chatBox != null) {
            this.setFocused(this.chatBox);
        }
    }

    // ---------------------------------------------------------------- 画

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.closeBubbleLater) {
            this.closeBubble();
        }
        LobbyStatePayload state = LobbyClient.state();
        this.slots.clear();
        if (state != null) {
            this.extractSlots(graphics, state, mouseX, mouseY);
        }
        this.extractChat(graphics);
        if (this.kickTarget != null) {
            this.extractBubble(graphics);
        }
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    /** 1~4 人一行；5~10 人两行（多的在上）。更多人时返回 null，按屏幕一行排满再排下一行。 */
    static int @Nullable [] fixedRows(int count) {
        if (count <= 4) {
            return new int[]{count};
        }
        if (count <= 10) {
            return new int[]{(count + 1) / 2, count / 2};
        }
        return null;
    }

    /** 一行排满 columns 个再排下一行，最后一行放剩下的。 */
    static int[] fillRows(int count, int columns) {
        int[] rows = new int[(count + columns - 1) / columns];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = Math.min(columns, count - i * columns);
        }
        return rows;
    }

    /** 这样排时头像最大能多大（放不下就是 MIN_HEAD）。 */
    private int fitSize(int[] rows, int nameHeight, int statusHeight, int availableHeight) {
        int widest = 0;
        for (int row : rows) {
            widest = Math.max(widest, row);
        }
        // 从设置的大小往下缩，直到放得下：宽——最宽那行两边各留一个头像宽；高——头像、名字、皇冠、状态文字都在聊天上面。
        int size = LobbyConfig.headSizeGui();
        for (; size > MIN_HEAD; size--) {
            int gap = gapFor(size);
            int border = borderFor(size);
            int rowWidth = widest * size + (widest - 1) * gap + 2 * border;
            int totalHeight = crownHeightFor(size) * 2 / 3 + border
                    + rows.length * (size + 2 * border + nameHeight) + (rows.length - 1) * gap + statusHeight;
            if (rowWidth + 2 * size <= this.width && totalHeight <= availableHeight) {
                break;
            }
        }
        return size;
    }

    private static int gapFor(int size) {
        return Math.max(3, size / 4);
    }

    private static int borderFor(int size) {
        return Math.max(1, size / 32);
    }

    private static int crownHeightFor(int size) {
        return Math.max(5, size / 2 * 10 / 16);
    }

    /** 这个大小一行能放几个头像（两边各留一个头像宽）。 */
    private int columnsFor(int size) {
        return Math.max(1, (this.width - 2 * size - 2 * borderFor(size) + gapFor(size)) / (size + gapFor(size)));
    }

    /** 这个大小能排几行（头像、名字、皇冠、状态文字都在聊天上面）。 */
    private static int rowsFor(int size, int nameHeight, int statusHeight, int availableHeight) {
        int gap = gapFor(size);
        int border = borderFor(size);
        int crown = crownHeightFor(size) * 2 / 3 + border;
        return Math.max(1, (availableHeight - crown - statusHeight + gap) / (size + 2 * border + nameHeight + gap));
    }

    /**
     * 算头像怎么排（结果缓存起来，人数和窗口不变就不重算）。
     * 10 人以内照固定排法；更多人时一行排满再排下一行，头像最多缩到设置大小的一半来挤进一页，
     * 还放不下就按一半大小翻页，每页是能放下的最大长方形。
     */
    private Layout layoutFor(int count, int nameHeight, int statusHeight, int pagedStatusHeight, int availableHeight) {
        int key = Objects.hash(count, nameHeight, statusHeight, availableHeight, this.width, LobbyConfig.headSizeGui());
        if (this.layout != null && this.layoutKey == key) {
            return this.layout;
        }
        Layout result;
        int[] fixed = fixedRows(count);
        if (fixed != null) {
            result = new Layout(fixed, this.fitSize(fixed, nameHeight, statusHeight, availableHeight), count, 1, false);
        } else {
            // 从设置的大小往下缩，一页放得下就不翻页；缩到一半还放不下，就按一半大小翻页。
            int full = LobbyConfig.headSizeGui();
            int half = Math.max(MIN_HEAD, full / 2);
            result = null;
            for (int size = full; size >= half && result == null; size--) {
                int columns = this.columnsFor(size);
                if (count <= columns * rowsFor(size, nameHeight, statusHeight, availableHeight)) {
                    result = new Layout(fillRows(count, columns), size, count, 1, false);
                }
            }
            if (result == null) {
                // 每页都是排满的长方形：columns 列 × rowCount 行。
                int columns = this.columnsFor(half);
                int rowCount = rowsFor(half, nameHeight, pagedStatusHeight, availableHeight);
                int[] pageRows = new int[rowCount];
                Arrays.fill(pageRows, columns);
                int perPage = columns * rowCount;
                result = new Layout(pageRows, half, perPage, (count + perPage - 1) / perPage, true);
            }
        }
        this.layout = result;
        this.layoutKey = key;
        return result;
    }

    private void extractSlots(GuiGraphicsExtractor graphics, LobbyStatePayload state, int mouseX, int mouseY) {
        List<GameProfile> members = state.members();
        int count = state.published() ? Math.max(state.maxPlayers(), members.size()) : Math.max(1, members.size());
        count = Math.min(count, MAX_SLOTS);
        boolean names = LobbyConfig.SHOW_NAMES.getAsBoolean();
        int nameHeight = names ? LINE_HEIGHT + 3 : 0;
        int waitingHeight = LobbyClient.isHost() || state.starting() ? 0 : 3 + LINE_HEIGHT;
        int statusHeight = 4 + LINE_HEIGHT + waitingHeight;
        int pagedStatusHeight = 4 + PAGE_BUTTON + waitingHeight;
        int areaTop = 6;
        int areaBottom = this.chatLogTop() - 4;
        Layout layout = this.layoutFor(count, nameHeight, statusHeight, pagedStatusHeight, areaBottom - areaTop);
        this.page = Math.clamp(this.page, 0, layout.pages() - 1);
        int first = this.page * layout.perPage();
        int onPage = Math.min(layout.perPage(), count - first);

        // 这一页的每行：翻页时一行排满再排下一行（最后一页的最后一行少几个，照样居中）。
        int[] rows;
        if (layout.paged()) {
            int columns = layout.rows()[0];
            rows = new int[(onPage + columns - 1) / columns];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = Math.min(columns, onPage - i * columns);
            }
        } else {
            rows = layout.rows();
        }
        int size = layout.size();
        int gap = gapFor(size);
        int border = borderFor(size);
        int crownHeight = crownHeightFor(size);
        int rowStep = size + 2 * border + nameHeight + gap;
        // 翻页时按整页的高度定位置，翻到最后一页时格子和按钮不会跳。
        int fullRows = layout.rows().length;
        int gridHeight = crownHeight * 2 / 3 + border + fullRows * (size + 2 * border + nameHeight) + (fullRows - 1) * gap;
        int usedStatusHeight = layout.paged() ? pagedStatusHeight : statusHeight;
        int top = Math.max(areaTop, areaTop + (areaBottom - areaTop - gridHeight - usedStatusHeight) / 2) + crownHeight * 2 / 3 + border;

        int index = first;
        Component tooltip = null;
        for (int row = 0; row < rows.length; row++) {
            int inRow = rows[row];
            int rowWidth = inRow * size + (inRow - 1) * gap;
            int left = (this.width - rowWidth) / 2;
            int y = top + row * rowStep;
            for (int column = 0; column < inRow; column++, index++) {
                int x = left + column * (size + gap);
                GameProfile profile = index < members.size() ? members.get(index) : null;
                boolean host = index == 0;
                graphics.fill(x - border, y - border, x + size + border, y + size + border, host ? HOST_BORDER : SLOT_BORDER);
                graphics.fill(x, y, x + size, y + size, SLOT_BACKGROUND);
                if (profile != null) {
                    int inset = Math.max(1, size / 16);
                    PlayerFaceExtractor.extractRenderState(graphics, this.skin(profile), x + inset, y + inset, size - inset * 2);
                }
                if (host) {
                    int crownWidth = crownHeight * 16 / 10;
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CROWN, x + (size - crownWidth) / 2,
                            y - border - crownHeight * 2 / 3, crownWidth, crownHeight);
                }
                if (profile != null && names) {
                    int maxWidth = size + gap;
                    String name = profile.name();
                    if (this.font.width(name) > maxWidth) {
                        name = this.font.plainSubstrByWidth(name, maxWidth - this.font.width("…")) + "…";
                    }
                    graphics.centeredText(this.font, name, x + size / 2, y + size + border + 3, host ? HOST_NAME : 0xFFFFFFFF);
                }
                Slot slot = new Slot(x, y, size, index, profile);
                this.slots.add(slot);
                if (profile != null && slot.contains(mouseX, mouseY) && this.kickTarget == null) {
                    boolean kickable = !host && LobbyClient.isHost();
                    if (!names) {
                        // 不显示名字时，鼠标放上去看名字。
                        MutableComponent text = Component.literal(profile.name());
                        tooltip = kickable ? text.append(" · ").append(Component.translatable("lets_start_together.lobby.kick_hint")) : text;
                    } else if (kickable) {
                        tooltip = Component.translatable("lets_start_together.lobby.kick_hint");
                    }
                }
            }
        }
        if (tooltip != null) {
            graphics.setTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }

        Component status;
        if (state.starting()) {
            status = Component.translatable("lets_start_together.lobby.starting");
        } else if (state.published()) {
            status = Component.translatable("lets_start_together.lobby.open", state.port(), members.size(), state.maxPlayers());
        } else if (LobbyClient.isHost()) {
            status = Component.translatable("lets_start_together.lobby.not_open");
        } else {
            status = CommonComponents.EMPTY;
        }
        int gridBottom = top + fullRows * rowStep - gap;
        int statusY;
        if (layout.paged()) {
            // 状态文字后面接页码，两边是翻页按钮。
            status = Component.empty().append(status).append("  ·  ")
                    .append(Component.translatable("lets_start_together.lobby.page", this.page + 1, layout.pages()));
            int buttonY = gridBottom + 4;
            int half = this.font.width(status) / 2;
            this.placePageButtons(this.width / 2 - half - 8 - PAGE_BUTTON, this.width / 2 + half + 8, buttonY, layout.pages());
            graphics.centeredText(this.font, status, this.width / 2, buttonY + (PAGE_BUTTON - LINE_HEIGHT) / 2 + 1, STATUS_COLOR);
            statusY = buttonY + PAGE_BUTTON - LINE_HEIGHT;
        } else {
            this.placePageButtons(0, 0, 0, 1);
            statusY = gridBottom + 4;
            graphics.centeredText(this.font, status, this.width / 2, statusY, STATUS_COLOR);
        }
        if (!LobbyClient.isHost() && !state.starting()) {
            graphics.centeredText(this.font, Component.translatable("lets_start_together.lobby.waiting"), this.width / 2,
                    statusY + LINE_HEIGHT + 3, STATUS_COLOR);
        }
    }

    private void placePageButtons(int prevX, int nextX, int y, int pages) {
        if (this.prevPage == null || this.nextPage == null) {
            return;
        }
        boolean paged = pages > 1;
        this.prevPage.visible = paged;
        this.nextPage.visible = paged;
        this.prevPage.setPosition(prevX, y);
        this.nextPage.setPosition(nextX, y);
        this.prevPage.active = this.page > 0;
        this.nextPage.active = this.page < pages - 1;
    }

    /** 聊天记录有变（或者宽度变了）就重新折行；往上翻着的时候，新消息来了不把视线拽下去。 */
    private void rewrapChat() {
        int width = this.chatWidth() - 4;
        int version = LobbyClient.logVersion();
        if (version == this.wrappedVersion && width == this.wrappedWidth) {
            return;
        }
        int before = this.chatLines.size();
        boolean sameWidth = width == this.wrappedWidth;
        this.wrappedVersion = version;
        this.wrappedWidth = width;
        this.chatLines.clear();
        for (Component line : LobbyClient.log()) {
            this.chatLines.addAll(this.font.split(line, width));
        }
        if (this.chatScroll > 0 && sameWidth && this.chatLines.size() > before) {
            this.chatScroll += this.chatLines.size() - before;
        }
        this.clampScroll();
    }

    private void clampScroll() {
        this.chatScroll = Math.clamp(this.chatScroll, 0, Math.max(0, this.chatLines.size() - CHAT_LINES));
    }

    private void extractChat(GuiGraphicsExtractor graphics) {
        this.rewrapChat();
        int x = this.chatX();
        int width = this.chatWidth();
        int bottom = this.chatLogBottom();
        float opacity = this.minecraft.options.textBackgroundOpacity().get().floatValue();
        graphics.fill(x, this.chatBarY(), x + width, this.chatBarY() + 12, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));

        int visible = Math.min(CHAT_LINES, this.chatLines.size());
        if (visible == 0) {
            return;
        }
        int top = bottom - visible * LINE_HEIGHT;
        graphics.fill(x, top, x + width, bottom, ARGB.black(opacity));
        ActiveTextCollector text = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
        this.visitChat(text);

        // 滚动条（和原版聊天一样在右边一条细线）。
        int total = this.chatLines.size();
        if (total > visible) {
            int height = visible * LINE_HEIGHT;
            int barHeight = Math.max(4, height * visible / total);
            int barBottom = bottom - (height - barHeight) * this.chatScroll / (total - visible);
            graphics.fill(x + width - 2, barBottom - barHeight, x + width, barBottom, 0xFFCCCCCC);
        }
    }

    private void visitChat(ActiveTextCollector output) {
        int visible = Math.min(CHAT_LINES, this.chatLines.size());
        int bottom = this.chatLogBottom();
        for (int k = 0; k < visible; k++) {
            int line = this.chatLines.size() - 1 - this.chatScroll - k;
            if (line < 0) {
                break;
            }
            output.accept(this.chatX() + 2, bottom - (k + 1) * LINE_HEIGHT + 1, this.chatLines.get(line));
        }
    }

    private @Nullable Style chatStyleAt(double mouseX, double mouseY) {
        ActiveTextCollector.ClickableStyleFinder finder = new ActiveTextCollector.ClickableStyleFinder(this.font, (int) mouseX, (int) mouseY);
        this.visitChat(finder);
        return finder.result();
    }

    private void extractBubble(GuiGraphicsExtractor graphics) {
        int width = this.bubbleWidth();
        int height = this.bubbleHeight();
        int x = this.bubbleX;
        int y = this.bubbleY;
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, SLOT_BORDER);
        graphics.fill(x, y, x + width, y + height, BUBBLE_BACKGROUND);
        graphics.centeredText(this.font, this.bubbleText(), x + width / 2, y + 6, 0xFFFFFFFF);
    }

    private PlayerSkin skin(GameProfile profile) {
        return this.skins.computeIfAbsent(profile.id(), id -> this.minecraft.getSkinManager().createLookup(profile, false)).get();
    }
}
