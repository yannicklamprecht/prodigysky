package fr.cocoraid.prodigysky.nms.packet;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface Packets {
   void sendFakeBiome(JavaPlugin javaPlugin, Player var1);

   void setSmog(Player var1);

   void restoreDefaultWorld(Player var1);
}
