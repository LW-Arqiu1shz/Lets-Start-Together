package io.github.lwarqiu1shz.letsstarttogether.network;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 服务器 → 客户端：小厅聊天里的一行（玩家发言，或房主那边的联机提示，比如 IP / e4mc 地址）。
 * {@code fromHost} 为 true 时这行本来就在房主的聊天栏里，房主那边不用再加一次。
 */
public record LobbyLinePayload(Component line, boolean fromHost) implements CustomPacketPayload {
    public static final Type<LobbyLinePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(LetsStartTogether.MOD_ID, "lobby_line"));
    public static final StreamCodec<FriendlyByteBuf, LobbyLinePayload> CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC, LobbyLinePayload::line,
            ByteBufCodecs.BOOL, LobbyLinePayload::fromHost,
            LobbyLinePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
