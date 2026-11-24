package io.github.lwarqiu1shz.letsstarttogether.network;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 客户端 → 服务器：房主点了“开始游戏”。服务器会核对发送者确实是房主。 */
public record LobbyStartPayload() implements CustomPacketPayload {
    public static final LobbyStartPayload INSTANCE = new LobbyStartPayload();
    public static final Type<LobbyStartPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(LetsStartTogether.MOD_ID, "lobby_start"));
    public static final StreamCodec<FriendlyByteBuf, LobbyStartPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
