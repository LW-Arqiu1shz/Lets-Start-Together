package io.github.lwarqiu1shz.letsstarttogether.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

/** 每个玩家自己的小厅显示设置（config/lets_start_together-client.toml）。 */
public final class LobbyConfig {
    public static final int HEAD_SIZE_MIN = 8;
    public static final int HEAD_SIZE_MAX = 256;
    public static final int HEAD_SIZE_DEFAULT = 64;

    /** 头像大小的单位。 */
    public enum SizeUnit implements TranslatableEnum {
        /** 像素：屏幕上真实的像素，是绝对大小，不跟着“界面尺寸”变。 */
        PX,
        /** 磅：相对大小，1 磅 = 原版界面里的 1 个像素，跟着“界面尺寸”一起缩放。 */
        PT;

        @Override
        public Component getTranslatedName() {
            return Component.translatable("config.lets_start_together.head_size_unit." + this.name().toLowerCase());
        }
    }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue HEAD_SIZE;
    public static final ModConfigSpec.EnumValue<SizeUnit> HEAD_SIZE_UNIT;
    public static final ModConfigSpec.BooleanValue SHOW_NAMES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        HEAD_SIZE = builder
                .comment("Size of the player heads in the lobby, 8 to 256, in the unit below. Default 64 pt.")
                .translation("config.lets_start_together.head_size")
                .defineInRange("head_size", HEAD_SIZE_DEFAULT, HEAD_SIZE_MIN, HEAD_SIZE_MAX);
        HEAD_SIZE_UNIT = builder
                .comment("PX = real screen pixels (absolute; ignore GUI Scale), PT = points, relative (1 pt = 1 GUI pixel; scale with GUI Scale).")
                .translation("config.lets_start_together.head_size_unit")
                .defineEnum("head_size_unit", SizeUnit.PT);
        SHOW_NAMES = builder
                .comment("Show player names under the heads. When off, hover a head to see the name.")
                .translation("config.lets_start_together.show_names")
                .define("show_names", true);
        SPEC = builder.build();
    }

    private LobbyConfig() {
    }

    /** 头像大小换算成 GUI 像素（至少 8）。 */
    public static int headSizeGui() {
        int size = HEAD_SIZE.getAsInt();
        if (HEAD_SIZE_UNIT.get() == SizeUnit.PX) {
            double scale = Minecraft.getInstance().getWindow().getGuiScale();
            size = (int) Math.round(size / Math.max(1.0, scale));
        }
        return Math.max(HEAD_SIZE_MIN, size);
    }
}
