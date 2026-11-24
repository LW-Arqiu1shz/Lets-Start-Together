package io.github.lwarqiu1shz.letsstarttogether.network;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 客户端 → 服务器：在小厅里发一句话。服务器加上名字后用 {@link LobbyLinePayload} 发给所有人。 */
public record LobbyChatPayload(String text) implements CustomPacketPayload {
    public static final int MAX_LENGTH = 256;
    public static final Type<LobbyChatPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(LetsStartTogether.MOD_ID, "lobby_chat"));
    public static final StreamCodec<FriendlyByteBuf, LobbyChatPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_LENGTH), LobbyChatPayload::text,
            LobbyChatPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
