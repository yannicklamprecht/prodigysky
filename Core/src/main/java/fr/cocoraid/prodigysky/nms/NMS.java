package fr.cocoraid.prodigysky.nms;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import fr.cocoraid.prodigysky.ProdigySky;
import fr.cocoraid.prodigysky.nms.biomes.Biomes;
import fr.cocoraid.prodigysky.nms.biomes.versions.Biomes_1_16R3;
import fr.cocoraid.prodigysky.nms.packet.Packets;
import fr.cocoraid.prodigysky.nms.packet.versions.Packets_1_16R3;

public class NMS {
   private Biomes biomes;
   private Packets packets;

   public NMS(ProdigySky instance) {
      ConsoleCommandSender cc = Bukkit.getConsoleSender();

      try {
         String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
         if (version.equals("v1_16_R3")) {
            this.biomes = new Biomes_1_16R3();
            this.packets = new Packets_1_16R3();
         } else {
            cc.sendMessage("§4[ProdigyNightclub] The plugin is not compatible with this version sorry :'(");
            Bukkit.getPluginManager().disablePlugin(instance);
         }
      } catch (ArrayIndexOutOfBoundsException var4) {
         var4.printStackTrace();
      }

   }

   public Packets getPackets() {
      return this.packets;
   }

   public Biomes getBiomes() {
      return this.biomes;
   }
}
