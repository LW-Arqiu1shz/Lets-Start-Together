package io.github.lwarqiu1shz.letsstarttogether.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/** 小厅显示设置：头像大小、单位、显示名字。点“完成”或按 Esc 时保存。 */
public final class LobbyConfigScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int LABEL_WIDTH = 150;
    private static final int CONTROL_WIDTH = 150;

    private final @Nullable Screen parent;
    private int headSize = LobbyConfig.headSize();
    private LobbyConfig.SizeUnit unit = LobbyConfig.headSizeUnit();
    private boolean showNames = LobbyConfig.showNames();

    public LobbyConfigScreen(@Nullable Screen parent) {
        super(Component.translatable("lets_start_together.configuration.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - (LABEL_WIDTH + CONTROL_WIDTH + 10) / 2;
        int controlX = left + LABEL_WIDTH + 10;
        int y = 40;

        this.addRenderableWidget(new StringWidget(0, 15, this.width, 9, this.title, this.font));

        this.addLabel("config.lets_start_together.head_size", left, y);
        EditBox size = new EditBox(this.font, controlX, y, CONTROL_WIDTH, 20, Component.translatable("config.lets_start_together.head_size"));
        size.setValue(Integer.toString(this.headSize));
        size.setMaxLength(3);
        size.setResponder(text -> {
            try {
                this.headSize = Integer.parseInt(text.strip());
            } catch (NumberFormatException ignored) {
                // 没填完或不是数字：保留上一个数。
            }
        });
        size.setTooltip(Tooltip.create(Component.translatable("config.lets_start_together.head_size.tooltip")));
        this.addRenderableWidget(size);
        y += ROW_HEIGHT;

        this.addLabel("config.lets_start_together.head_size_unit", left, y);
        this.addRenderableWidget(CycleButton.<LobbyConfig.SizeUnit>builder(LobbyConfig.SizeUnit::label, this.unit)
                .withValues(LobbyConfig.SizeUnit.values())
                .withTooltip(value -> Tooltip.create(Component.translatable("config.lets_start_together.head_size_unit.tooltip")))
                .displayOnlyValue()
                .create(controlX, y, CONTROL_WIDTH, 20, Component.translatable("config.lets_start_together.head_size_unit"),
                        (button, value) -> this.unit = value));
        y += ROW_HEIGHT;

        this.addLabel("config.lets_start_together.show_names", left, y);
        this.addRenderableWidget(CycleButton.onOffBuilder(this.showNames)
                .withTooltip(value -> Tooltip.create(Component.translatable("config.lets_start_together.show_names.tooltip")))
                .displayOnlyValue()
                .create(controlX, y, CONTROL_WIDTH, 20, Component.translatable("config.lets_start_together.show_names"),
                        (button, value) -> this.showNames = value));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    private void addLabel(String key, int x, int y) {
        StringWidget label = new StringWidget(x, y, LABEL_WIDTH, 20, Component.translatable(key), this.font);
        this.addRenderableWidget(label);
    }

    @Override
    public void onClose() {
        LobbyConfig.set(this.headSize, this.unit, this.showNames);
        this.minecraft.gui.setScreen(this.parent);
    }
}
