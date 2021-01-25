package fr.cocoraid.prodigysky.filemanager;

import fr.cocoraid.prodigysky.ProdigySky;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;

public class CustomBiomes {

  private boolean missingDatapacks;
  private final ProdigySky instance;
  private Map<String, Integer> biomes = new HashMap<>();

  public CustomBiomes(ProdigySky instance) {
    this.instance = instance;
    ConsoleCommandSender cc = Bukkit.getConsoleSender();
    this.loadDefaults();
    List<String> biomesWaiting = new ArrayList<>();
    File biomeFolder = new File(instance.getDataFolder().getPath() + "/custom_biomes");
    biomeFolder.mkdirs();
    File[] files = biomeFolder.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.getName().endsWith(".json")) {
          biomesWaiting.add(file.getName().replace(".json", "").toLowerCase());
          this.placeBiomeForWorlds(file);
        }
      }
    }

    this.registerCustomBiomes();
    this.cleanup(biomesWaiting);
    if (biomesWaiting.stream().anyMatch((b) -> !this.biomes.containsKey(b))) {
      cc.sendMessage(
          "§6[ProdigySky Warning] §cOne or more biomes are not yet loaded by the server... Please restart the server (dont't reload)");
      this.missingDatapacks = true;
    }

  }

  private void registerCustomBiomes() {
    this.biomes = this.instance.getNMS().getBiomes().getBiomes();
  }

  private void placeBiomeForWorlds(File biome) {

    for (World w : this.instance.getConfiguration().getEnabledWorlds()) {
      String DATAPACK_PATH = w.getWorldFolder().getPath()
          + "/datapacks/prodigysky/data/minecraft/worldgen/biome/prodigysky";
      File folder = new File(DATAPACK_PATH);
      if (!folder.exists()) {
        folder.mkdirs();
      }

      File metaFile = new File(w.getWorldFolder().getPath() + "/datapacks/prodigysky/pack.mcmeta");
      if (!metaFile.exists()) {

        try(InputStream is = this.instance.getResource("pack.mcmeta")) {
          FileUtils.copyInputStreamToFile(is, metaFile);
        } catch (IOException var9) {
          var9.printStackTrace();
        }
      }

      try {
        FileUtils.copyFileToDirectory(biome, folder);
      } catch (IOException var10) {
        var10.printStackTrace();
      }
    }

  }

  private void loadDefaults() {
    File customBiomeFolder = new File(this.instance.getDataFolder().getPath() + "/custom_biomes");
    if (!customBiomeFolder.exists()) {
      File jarFile = new File(
          this.instance.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

      try {
        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
          String name = entries.nextElement().getName();
          if (name.startsWith("examples/") && !name.endsWith("/")) {
            this.instance.saveResource(name, false);
          }
        }

        jar.close();
      } catch (IOException var7) {
        var7.printStackTrace();
      }

      File sourceFile = new File(this.instance.getDataFolder().getPath() + "/examples");
      sourceFile.renameTo(customBiomeFolder);
    }
  }

  private void cleanup(List<String> customBiomes) {
    Iterator<World> var2 = Bukkit.getWorlds().iterator();

    while (true) {
      File folder;
      do {
        World world;
        File prskyDatapack;
        do {
          if (!var2.hasNext()) {
            return;
          }

          world = var2.next();
          prskyDatapack = new File(world.getWorldFolder().getPath() + "/datapacks/prodigysky");
        } while (!prskyDatapack.exists());

        if (!this.instance.getConfiguration().getEnabledWorlds().contains(world)) {
          try {
            FileUtils.deleteDirectory(prskyDatapack);
          } catch (IOException var13) {
            var13.printStackTrace();
          }
        }

        String DATAPACK_PATH = world.getWorldFolder().getPath()
            + "/datapacks/prodigysky/data/minecraft/worldgen/biome/prodigysky";
        folder = new File(DATAPACK_PATH);
      } while (!folder.exists());

      for (File biome : Objects.requireNonNull(folder.listFiles())) {
        if (biome.getName().endsWith(".json")) {
          String name = biome.getName().replace(".json", "").toLowerCase();
          if (!customBiomes.contains(name)) {
            try {
              FileUtils.forceDelete(biome);
            } catch (IOException var14) {
              var14.printStackTrace();
            }
          }
        }
      }
    }
  }

  public Map<String, Integer> getBiomes() {
    return this.biomes;
  }

  public boolean isMissingDatapacks() {
    return this.missingDatapacks;
  }
}
