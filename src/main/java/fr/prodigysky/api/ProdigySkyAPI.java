package fr.prodigysky.api;

import fr.cocoraid.prodigysky.ProdigySky;
import fr.cocoraid.prodigysky.feature.BiomeData;
import fr.cocoraid.prodigysky.nms.packet.Packets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ProdigySkyAPI {
   private static final Map<World, BiomeData> biomeWorlds = new HashMap<>();
   private static final Map<UUID, BiomeData> biomePlayer = new HashMap<>();

   public static void setBiome(Player player, String biomeTemplate, boolean smog, EffectDuration duration) {
      BiomeData biomeData = new BiomeData(biomeTemplate.toLowerCase(), duration);
      biomeData.setSmog(smog);
      if (!hasExactlyTheSameBiome(player, biomeData)) {
         boolean oldSmog = getBiomePlayer().containsKey(player.getUniqueId()) && getBiomePlayer().get(player.getUniqueId())
             .isSmog();
         Packets packets = ProdigySky.getInstance().getNMS().getPackets();
         player.setPlayerTime(0L, false);
         biomePlayer.put(player.getUniqueId(), biomeData);
         World w = player.getWorld();
         boolean alreadySmog = false;
         if (getBiomeWorlds().containsKey(w) && getBiomeWorlds().get(w).isSmog() || oldSmog) {
            alreadySmog = true;
         }

         if (smog && !alreadySmog) {
            packets.setSmog(player);
         } else if (alreadySmog && !smog) {
            packets.restoreDefaultWorld(player);
         }

         if (!getBiomeWorlds().containsKey(w) || getBiomeWorlds().containsKey(w) && getBiomeWorlds().get(w)
             .getName().equalsIgnoreCase(biomeTemplate)) {
            packets.sendFakeBiome(player);
         }

      }
   }

   public static void removeBiome(Player player) {
      if (getBiomePlayer().containsKey(player.getUniqueId())) {
         boolean hasSmog = getBiomePlayer().get(player.getUniqueId()).isSmog();
         getBiomePlayer().remove(player.getUniqueId());
         Packets packets = ProdigySky.getInstance().getNMS().getPackets();
         packets.sendFakeBiome(player);
         if (hasSmog) {
            packets.restoreDefaultWorld(player);
         }

      }
   }

   public static void removeBiome(World w) {
      if (getBiomeWorlds().containsKey(w)) {
         boolean hasSmog = getBiomeWorlds().get(w).isSmog();
         EffectDuration duration = getBiomeWorlds().get(w).getDuration();
         List<UUID> tempPlayers = getBiomeWorlds().get(w).getTempPlayers();
         getBiomeWorlds().remove(w);
         Packets packets = ProdigySky.getInstance().getNMS().getPackets();
         if (duration == EffectDuration.PERSISTENT) {
            Bukkit.getOnlinePlayers().stream().filter((cur) -> cur.getWorld().equals(w)).forEach((cur) -> {
               packets.sendFakeBiome(cur);
               boolean playerSmog = getBiomePlayer().containsKey(cur.getUniqueId()) && getBiomePlayer().get(cur.getUniqueId())
                   .isSmog();
               if (hasSmog && !playerSmog) {
                  packets.restoreDefaultWorld(cur);
               }

            });
         } else if (duration == EffectDuration.VOLATILE && !tempPlayers.isEmpty()) {
            tempPlayers.forEach((uuid) -> {
               Player cur = Bukkit.getPlayer(uuid);
               packets.sendFakeBiome(cur);
               boolean playerSmog = getBiomePlayer().containsKey(cur.getUniqueId()) && getBiomePlayer().get(cur.getUniqueId())
                   .isSmog();
               if (hasSmog && !playerSmog) {
                  packets.restoreDefaultWorld(cur);
               }

            });
         }

      }
   }

   public static void setBiome(World world, String biomeTemplate, boolean smog, EffectDuration duration) {
      BiomeData biomeData = new BiomeData(biomeTemplate.toLowerCase(), duration);
      biomeData.setSmog(smog);
      if (!hasExactlyTheSameBiome(world, biomeData)) {
         boolean hasWorldSmog = biomeWorlds.containsKey(world) && biomeWorlds.get(world).isSmog();
         biomeWorlds.put(world, biomeData);
         Packets packets = ProdigySky.getInstance().getNMS().getPackets();
         Bukkit.getOnlinePlayers().stream().filter((cur) -> {
            return cur.getWorld().equals(world) && !getBiomePlayer().containsKey(cur.getUniqueId());
         }).forEach((cur) -> {
            cur.setPlayerTime(0L, false);
            if (duration == EffectDuration.VOLATILE) {
               biomeData.getTempPlayers().add(cur.getUniqueId());
            }

            boolean smogged = false;
            if (!hasWorldSmog && smog && (!getBiomePlayer().containsKey(cur.getUniqueId()) || getBiomePlayer().containsKey(cur.getUniqueId()) && !getBiomePlayer().get(cur.getUniqueId())
                .isSmog())) {
               smogged = true;
               packets.setSmog(cur);
            }

            if (!smogged && !smog && hasWorldSmog) {
               packets.restoreDefaultWorld(cur);
            }

            packets.sendFakeBiome(cur);
         });
      }
   }

   public static Map<UUID, BiomeData> getBiomePlayer() {
      return biomePlayer;
   }

   public static Map<World, BiomeData> getBiomeWorlds() {
      return biomeWorlds;
   }

   private static boolean hasExactlyTheSameBiome(Player p, BiomeData toCompare) {
      if (!biomePlayer.containsKey(p.getUniqueId())) {
         return false;
      } else {
         BiomeData bd = biomePlayer.get(p.getUniqueId());
         return bd.getName().equalsIgnoreCase(toCompare.getName()) && bd.isSmog() == toCompare.isSmog() && bd.getDuration() == toCompare.getDuration();
      }
   }

   private static boolean hasExactlyTheSameBiome(World world, BiomeData toCompare) {
      if (!biomeWorlds.containsKey(world)) {
         return false;
      } else {
         BiomeData bd = biomeWorlds.get(world);
         return bd.getName().equalsIgnoreCase(toCompare.getName()) && bd.isSmog() == toCompare.isSmog() && bd.getDuration() == toCompare.getDuration();
      }
   }

   public static BiomeData getBiomeData(UUID uuid) {
      return getBiomePlayer().get(uuid);
   }

   public static BiomeData getBiomeData(World w) {
      return getBiomeWorlds().get(w);
   }
}
