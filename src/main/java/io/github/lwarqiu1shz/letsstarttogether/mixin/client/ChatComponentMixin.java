package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import io.github.lwarqiu1shz.letsstarttogether.client.LobbyClient;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 在小厅里时，聊天栏收到的每条消息（小厅聊天、mcwifipnp / e4mc 给的地址…）也抄一份给小厅显示。 */
@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"))
    private void lets_start_together$copyToLobby(Component contents, MessageSignature signature, GuiMessageSource source,
                                               GuiMessageTag tag, CallbackInfo ci) {
        LobbyClient.onChatMessage(contents);
    }
}
