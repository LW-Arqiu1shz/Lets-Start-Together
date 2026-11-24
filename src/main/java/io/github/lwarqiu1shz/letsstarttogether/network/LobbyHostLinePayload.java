package io.github.lwarqiu1shz.letsstarttogether.network;

import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 房主 → 服务器：房主聊天栏里出现的一条联机提示（mcwifipnp 的 IP、e4mc 的地址…），转给小厅里所有人。 */
public record LobbyHostLinePayload(Component line) implements CustomPacketPayload {
    public static final Type<LobbyHostLinePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(LetsStartTogether.MOD_ID, "lobby_host_line"));
    public static final StreamCodec<FriendlyByteBuf, LobbyHostLinePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(ComponentSerialization.CODEC), LobbyHostLinePayload::line,
            LobbyHostLinePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
