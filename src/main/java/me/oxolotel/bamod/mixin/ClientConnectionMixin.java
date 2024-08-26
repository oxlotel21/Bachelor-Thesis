package me.oxolotel.bamod.mixin;

import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin extends SimpleChannelInboundHandler<Packet<?>> {

    @Shadow
    @Final
    private Queue<Consumer<ClientConnection>> queuedTasks;

    @Shadow
    private void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush){}

    @Shadow
    protected abstract void handleQueuedTasks();

    @Shadow
    public abstract boolean isOpen();

    @Shadow private Channel channel;

    /*
        Used to gather information about the packets on the server side
     */
    @Inject(at = @At("TAIL"), method = "handlePacket")
    private static void handlePacket(Packet<PacketListener> packet, PacketListener listener, CallbackInfo ci){
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER){
            System.out.println(packet.getClass());
        }
    }

    /*
    @Inject(at = @At("TAIL"), method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V")
    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci){
//        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
//            System.out.println(packet.getClass());
//        }
        /*
            Print information on chat messages
        * /
        if (packet instanceof ChatMessageC2SPacket chatPacket){

            System.out.println(channel.pipeline().names());

            String data = """
                    ChatMessage: %s
                    Acknowledgement: %s
                    Salt: %s
                    Signature: %s
                    Timestamp: %s
                    """
                .formatted(
                    chatPacket.chatMessage(),
                    chatPacket.acknowledgment(),
                    chatPacket.salt(),
                    chatPacket.signature(),
                    chatPacket.timestamp()
                );

            System.out.println(data);
        }

    }
    */

    private List<ChatMessageS2CPacket> packets = new ArrayList<>();

    /**
     * Used to partially hold back messages or reoder them when broadcasting them to players
     */
    @Overwrite
    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush) {
        if (packet instanceof ChatMessageS2CPacket chatMessage){
            if (Math.random() > 0.5) {
                packets.add(chatMessage);
                return;
            } else if (Math.random() > 0.5 && !packets.isEmpty()) {
                int index = (int) (Math.random() * packets.size());
                packet = packets.remove(index);
                Logger.getAnonymousLogger().warning("sending packet " + index);
            }
            Logger.getAnonymousLogger().warning("Sending message " + ((ChatMessageS2CPacket) packet).body().content());
        }
        if (this.isOpen()) {
            this.handleQueuedTasks();
            this.sendImmediately(packet, callbacks, flush);
        } else {
            Packet<?> finalPacket = packet;
            this.queuedTasks.add(connection -> {

                try {
                    Method send = connection.getClass().getDeclaredMethod("sendImmediately", Packet.class, PacketCallbacks.class, boolean.class);
                    send.setAccessible(true);
                    send.invoke(connection, finalPacket, callbacks, flush);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
