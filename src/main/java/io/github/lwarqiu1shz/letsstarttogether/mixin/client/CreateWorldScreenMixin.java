package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import io.github.lwarqiu1shz.letsstarttogether.client.LobbyCreateState;
import io.github.lwarqiu1shz.letsstarttogether.lobby.LobbyServer;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 建世界时按“是否为多人世界”的选择，把新世界标成联机小厅世界。 */
@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void lets_start_together$claimLobbyDefault(CallbackInfo ci) {
        LobbyCreateState.claim((CreateWorldScreen) (Object) this);
    }

    @Inject(method = "createNewWorld", at = @At("HEAD"))
    private void lets_start_together$markLobby(CallbackInfoReturnable<Boolean> cir) {
        LobbyServer.markNextWorldAsLobby(LobbyCreateState.isLobby((CreateWorldScreen) (Object) this));
    }

    @Inject(method = "createNewWorld", at = @At("RETURN"))
    private void lets_start_together$unmarkIfFailed(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            LobbyServer.markNextWorldAsLobby(false);
        }
    }
}
