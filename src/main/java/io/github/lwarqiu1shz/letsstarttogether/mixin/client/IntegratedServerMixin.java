package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.server.LanServerPinger;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 在小厅里开放联机。原版的 publishServer 默认房主已经在世界里，会去改房主本地玩家的权限，
 * 而小厅里房主还没进世界（没有本地玩家）。这时跳过那几步，其余照原版做；
 * 房主进世界时服务器会把权限发过来。
 */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private int publishedPort;
    @Shadow private @Nullable GameType publishedGameType;
    @Shadow private @Nullable LanServerPinger lanPinger;

    @Inject(method = "publishServer", at = @At("HEAD"), cancellable = true)
    private void lets_start_together$publishFromLobby(@Nullable GameType gameMode, boolean allowCommands, int port,
                                                     CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.getConnection() != null || this.minecraft.player != null) {
            return;
        }
        IntegratedServer self = (IntegratedServer) (Object) this;
        try {
            this.minecraft.prepareForMultiplayer();
            self.getConnection().startTcpServerListener(null, port);
            LogUtils.getLogger().info("Started serving on {} (from the lobby)", port);
            this.publishedPort = port;
            this.lanPinger = new LanServerPinger(self.getMotd(), port + "");
            this.lanPinger.start();
            this.publishedGameType = gameMode;
            self.getPlayerList().setAllowCommandsForAllPlayers(allowCommands);
            cir.setReturnValue(true);
        } catch (IOException e) {
            cir.setReturnValue(false);
        }
    }
}
