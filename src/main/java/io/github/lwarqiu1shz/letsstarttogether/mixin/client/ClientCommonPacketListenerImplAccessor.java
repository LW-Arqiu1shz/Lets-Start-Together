package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 小厅的包是在配置阶段收到的，要从那时的监听器里拿到连接。 */
@Mixin(ClientCommonPacketListenerImpl.class)
public interface ClientCommonPacketListenerImplAccessor {
    @Accessor("connection")
    Connection lets_start_together$connection();
}
