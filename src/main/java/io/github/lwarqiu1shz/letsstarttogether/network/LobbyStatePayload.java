package io.github.lwarqiu1shz.letsstarttogether.network;

import com.mojang.authlib.GameProfile;
import io.github.lwarqiu1shz.letsstarttogether.LetsStartTogether;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 服务器 → 客户端：联机小厅现在的样子。
 *
 * @param members    小厅里的玩家，按加入顺序；第一个是房主
 * @param maxPlayers 最多几个人（mcwifipnp 里设的“最大玩家数”）
 * @param published  是否已经对外开放（没开放时其他人进不来，只画房主一个头像）
 * @param port       开放的端口，没开放时是 -1
 * @param starting   房主已经点了“开始游戏”
 */
public record LobbyStatePayload(List<GameProfile> members, int maxPlayers, boolean published, int port, boolean starting)
        implements CustomPacketPayload {
    public static final Type<LobbyStatePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(LetsStartTogether.MOD_ID, "lobby_state"));

    public static final StreamCodec<FriendlyByteBuf, LobbyStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.GAME_PROFILE.apply(ByteBufCodecs.list()), LobbyStatePayload::members,
            ByteBufCodecs.VAR_INT, LobbyStatePayload::maxPlayers,
            ByteBufCodecs.BOOL, LobbyStatePayload::published,
            ByteBufCodecs.VAR_INT, LobbyStatePayload::port,
            ByteBufCodecs.BOOL, LobbyStatePayload::starting,
            LobbyStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
