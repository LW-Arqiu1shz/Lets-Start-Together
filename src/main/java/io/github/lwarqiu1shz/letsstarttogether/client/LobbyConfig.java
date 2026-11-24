package io.github.lwarqiu1shz.letsstarttogether.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

/** 每个玩家自己的小厅显示设置（config/lets_start_together-client.json）。 */
public final class LobbyConfig {
    public static final int HEAD_SIZE_MIN = 8;
    public static final int HEAD_SIZE_MAX = 256;
    public static final int HEAD_SIZE_DEFAULT = 64;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("lets_start_together-client.json");

    /** 头像大小的单位。 */
    public enum SizeUnit {
        /** 像素：屏幕上真实的像素，是绝对大小，不跟着“界面尺寸”变。 */
        PX,
        /** 磅：相对大小，1 磅 = 原版界面里的 1 个像素，跟着“界面尺寸”一起缩放。 */
        PT;

        public Component label() {
            return Component.translatable("config.lets_start_together.head_size_unit." + this.name().toLowerCase(Locale.ROOT));
        }
    }

    private static int headSize = HEAD_SIZE_DEFAULT;
    private static SizeUnit headSizeUnit = SizeUnit.PT;
    private static boolean showNames = true;

    private LobbyConfig() {
    }

    public static int headSize() {
        return headSize;
    }

    public static SizeUnit headSizeUnit() {
        return headSizeUnit;
    }

    public static boolean showNames() {
        return showNames;
    }

    public static void set(int size, SizeUnit unit, boolean names) {
        headSize = Mth.clamp(size, HEAD_SIZE_MIN, HEAD_SIZE_MAX);
        headSizeUnit = unit;
        showNames = names;
        save();
    }

    /** 头像大小换算成 GUI 像素（至少 8）。 */
    public static int headSizeGui() {
        int size = headSize;
        if (headSizeUnit == SizeUnit.PX) {
            double scale = Minecraft.getInstance().getWindow().getGuiScale();
            size = (int) Math.round(size / Math.max(1.0, scale));
        }
        return Math.max(HEAD_SIZE_MIN, size);
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) {
            save();
            return;
        }
        try {
            JsonObject json = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            if (json.has("head_size")) {
                headSize = Mth.clamp(json.get("head_size").getAsInt(), HEAD_SIZE_MIN, HEAD_SIZE_MAX);
            }
            if (json.has("head_size_unit")) {
                headSizeUnit = SizeUnit.valueOf(json.get("head_size_unit").getAsString().toUpperCase(Locale.ROOT));
            }
            if (json.has("show_names")) {
                showNames = json.get("show_names").getAsBoolean();
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not read {}, using defaults", FILE, e);
        }
    }

    private static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("head_size", headSize);
        json.addProperty("head_size_unit", headSizeUnit.name());
        json.addProperty("show_names", showNames);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(json) + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", FILE, e);
        }
    }
}
