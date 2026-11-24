package io.github.lwarqiu1shz.letsstarttogether.lobby;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;

/**
 * 一个世界是不是“联机小厅世界”：存档文件夹里有没有 {@value #FILE_NAME}。
 *
 * <p>放在存档文件夹里，所以不用打开世界就能读写（存档编辑界面的开关、存档列表的小皇冠），
 * 复制、备份存档时也会跟着走。</p>
 */
public final class LobbyWorlds {
    public static final String FILE_NAME = "lets_start_together.json";
    /** 模组 id 改名（r8）前用的文件名，读到时照样算小厅世界，下次改开关时换成新名字。 */
    private static final String OLD_FILE_NAME = "letsstarttogether.json";
    private static final Logger LOGGER = LogUtils.getLogger();

    private LobbyWorlds() {
    }

    public static boolean isLobby(Path worldDir) {
        return Files.isRegularFile(worldDir.resolve(FILE_NAME)) || Files.isRegularFile(worldDir.resolve(OLD_FILE_NAME));
    }

    public static void setLobby(Path worldDir, boolean lobby) {
        Path file = worldDir.resolve(FILE_NAME);
        try {
            Files.deleteIfExists(worldDir.resolve(OLD_FILE_NAME));
            if (lobby) {
                Files.writeString(file, "{\n  \"lobby\": true\n}\n", StandardCharsets.UTF_8);
            } else {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not change the lobby flag of {}", worldDir, e);
        }
    }
}
