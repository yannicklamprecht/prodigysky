package fr.cocoraid.prodigysky.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import fr.cocoraid.prodigysky.ProdigySky;
import fr.cocoraid.prodigysky.feature.BiomeData;
import fr.cocoraid.prodigysky.nms.NMSUtils;
import fr.prodigysky.api.ProdigySkyAPI;
import java.util.Arrays;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PacketListener {
   ProtocolManager manager = ProtocolLibrary.getProtocolManager();
   private static final Class<?> dimensionManagerClass = MinecraftReflection.getMinecraftClass("DimensionManager");

   public PacketListener(final ProdigySky instance) {
      this.manager.addPacketListener(new PacketAdapter(instance, Server.MAP_CHUNK) {
         public void onPacketSending(PacketEvent event) {
            PacketContainer packet = event.getPacket();
            Player p = event.getPlayer();
            String biomeName;
            int biomeID;
            int[] biomeIDs;
            if (ProdigySkyAPI.getBiomePlayer().containsKey(p.getUniqueId())) {
               biomeName = ((BiomeData)ProdigySkyAPI.getBiomePlayer().get(p.getUniqueId())).getName();
               biomeID = (Integer)instance.getCustomBiomes().getBiomes().get(biomeName);
               biomeIDs = packet.getIntegerArrays().read(0);
               Arrays.fill(biomeIDs, biomeID);
               packet.getIntegerArrays().write(0, biomeIDs);
            } else if (ProdigySkyAPI.getBiomeWorlds().containsKey(p.getWorld())) {
               biomeName = ((BiomeData)ProdigySkyAPI.getBiomeWorlds().get(p.getWorld())).getName();
               biomeID = (Integer)instance.getCustomBiomes().getBiomes().get(biomeName);
               biomeIDs = packet.getIntegerArrays().read(0);
               Arrays.fill(biomeIDs, biomeID);
               packet.getIntegerArrays().write(0, biomeIDs);
            }

         }
      });
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params(instance,
          Server.LOGIN, Server.RESPAWN)) {
         public void onPacketSending(PacketEvent event) {
            Player p = event.getPlayer();
            World w = EventListener.changeWorlds.containsKey(p.getUniqueId()) ? EventListener.changeWorlds.get(p.getUniqueId())
                : p.getWorld();
            EventListener.changeWorlds.remove(p.getUniqueId());
            boolean smog = false;
            if (ProdigySkyAPI.getBiomePlayer().containsKey(p.getUniqueId())) {
               smog = ((BiomeData)ProdigySkyAPI.getBiomePlayer().get(p.getUniqueId())).isSmog();
            } else if (ProdigySkyAPI.getBiomeWorlds().containsKey(w)) {
               smog = ((BiomeData)ProdigySkyAPI.getBiomeWorlds().get(w)).isSmog();
            }

            if (smog) {
               Object dm = event.getPacket().getModifier().withType(PacketListener.dimensionManagerClass).read(0);
               Object clone = NMSUtils.cloneDimension(dm, true);
               event.getPacket().getModifier().withType(PacketListener.dimensionManagerClass).write(0, clone);
            }

         }
      });
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params(instance,
          Server.UPDATE_TIME)) {
         public void onPacketSending(PacketEvent event) {
            Player p = event.getPlayer();
            if (ProdigySkyAPI.getBiomePlayer().containsKey(p.getUniqueId()) || ProdigySkyAPI.getBiomeWorlds().containsKey(p.getWorld())) {
               event.getPacket().getLongs().write(0, 0L);
            }

         }
      });
   }
}
