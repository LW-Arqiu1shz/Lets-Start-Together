package io.github.lwarqiu1shz.letsstarttogether.network;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 房主 → 服务器：把某人请出小厅。 */
public record LobbyKickPayload(UUID player) implements CustomPacketPayload {
    public static final Type<LobbyKickPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(LetsStartTogether.MOD_ID, "lobby_kick"));
    public static final StreamCodec<FriendlyByteBuf, LobbyKickPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, LobbyKickPayload::player,
            LobbyKickPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
