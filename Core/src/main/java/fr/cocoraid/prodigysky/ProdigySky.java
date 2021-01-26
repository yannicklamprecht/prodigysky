package fr.cocoraid.prodigysky;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import fr.cocoraid.prodigysky.commands.MainCMD;
import fr.cocoraid.prodigysky.filemanager.Configuration;
import fr.cocoraid.prodigysky.filemanager.CustomBiomes;
import fr.cocoraid.prodigysky.listeners.EventListener;
import fr.cocoraid.prodigysky.listeners.PacketListener;
import fr.cocoraid.prodigysky.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ProdigySky extends JavaPlugin {
   private PaperCommandManager manager;
   private static ProdigySky instance;
   private NMS nms;
   private Configuration configuration;
   private CustomBiomes customBiomes;

   public void onEnable() {
      this.displayBanner();
      instance = this;
      this.nms = new NMS(this);
      this.configuration = new Configuration(this);
      this.configuration.init();
      this.configuration.load();
      this.customBiomes = new CustomBiomes(this);
      this.loadCommands();
      Bukkit.getPluginManager().registerEvents(new EventListener(), this);
      new PacketListener(this);
      (new BukkitRunnable() {
         public void run() {
         }
      }).runTaskTimerAsynchronously(this, 20L, 20L);
   }

   public void onDisable() {
   }

   private void displayBanner() {
      ConsoleCommandSender cc = Bukkit.getConsoleSender();
      cc.sendMessage("§5 ______               _ _                 §b______ _");
      cc.sendMessage("§5(_____ \\             | (_)               §b/ _____) |");
      cc.sendMessage("§5 _____) )___ ___   __| |_  ____ _   _   §b( (____ | |  _ _   _ ");
      cc.sendMessage("§5|  ____/ ___) _ \\ / _  | |/ _  | | | |   §b\\____ \\| |_/ ) | | |");
      cc.sendMessage("§5| |   | |  | |_| ( (_| | ( (_| | |_| |  §b _____) )  _ (| |_| |");
      cc.sendMessage("§5|_|   |_|   \\___/ \\____|_|\\___ |\\__  |§b  (______/|_| \\_)\\__  |");
      cc.sendMessage("§5                          (_____(____/      §b           (____/");
      cc.sendMessage("§d The prodigy is the man who knows how to shape the sky");
   }

   private void loadCommands() {
      this.manager = new PaperCommandManager(this);
      this.manager.getCommandConditions().addCondition(World.class, "worldEnabled", (c, exec, value) -> {
         if (value != null) {
            if (!this.configuration.getEnabledWorlds().contains(value)) {
               throw new ConditionFailedException("The world " + value.getName() + " is not added in the enabled world list !");
            }
         }
      });
      this.manager.getCommandConditions().addCondition(Player.class, "playerWorldEnabled", (c, exec, value) -> {
         if (value != null) {
            if (!this.configuration.getEnabledWorlds().contains(value.getWorld())) {
               throw new ConditionFailedException("The world " + value.getWorld().getName() + " is not added in the enabled world list !");
            }
         }
      });
      this.manager.getCommandCompletions().registerAsyncCompletion("biomeName", (c) -> this.customBiomes.getBiomes().keySet());
      this.manager.registerCommand(new MainCMD(this));
   }

   public CustomBiomes getCustomBiomes() {
      return this.customBiomes;
   }

   public static ProdigySky getInstance() {
      return instance;
   }

   public NMS getNMS() {
      return this.nms;
   }

   public Configuration getConfiguration() {
      return this.configuration;
   }
}
