package fr.cocoraid.prodigysky.feature;

import fr.prodigysky.api.EffectDuration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BiomeData {
   private final String name;
   private final EffectDuration duration;
   private boolean smog;
   private final List<UUID> tempPlayers = new ArrayList<>();

   public BiomeData(String name, EffectDuration duration) {
      this.name = name;
      this.duration = duration;
   }

   public void setSmog(boolean smog) {
      this.smog = smog;
   }

   public String getName() {
      return this.name;
   }

   public EffectDuration getDuration() {
      return this.duration;
   }

   public boolean isSmog() {
      return this.smog;
   }

   public List<UUID> getTempPlayers() {
      return this.tempPlayers;
   }
}
