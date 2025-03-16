package cn.zbx1425.resourcepackupdater;

import cn.zbx1425.resourcepackupdater.drm.ServerLockRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ResourcePackUpdater.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ResourcePackUpdater.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static final ResourceLocation CLIENT_VERSION_PACKET_ID = new ResourceLocation(ResourcePackUpdater.MOD_ID, "client_version");
    public static final ResourceLocation SERVER_LOCK_PACKET_ID = new ResourceLocation(ResourcePackUpdater.MOD_ID, "server_lock");

    public static void register(final FMLClientSetupEvent event) {
        CHANNEL.registerMessage(0, ClientVersionPacket.class, ClientVersionPacket::encode, ClientVersionPacket::decode, ClientVersionPacket::handle);
    }

    public static void sendClientVersionPacket() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(ResourcePackUpdater.MOD_VERSION);
        CHANNEL.send(PacketDistributor.SERVER.noArg(), new ClientVersionPacket(buf));
    }

    public static class ClientVersionPacket {
        private final String version;

        public ClientVersionPacket(FriendlyByteBuf buf) {
            this.version = buf.readUtf();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(version);
        }

        public static ClientVersionPacket decode(FriendlyByteBuf buf) {
            return new ClientVersionPacket(buf);
        }

        public static void handle(ClientVersionPacket packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ServerLockRegistry.onLoginInitiated();
            });
            context.get().setPacketHandled(true);
        }
    }
}
