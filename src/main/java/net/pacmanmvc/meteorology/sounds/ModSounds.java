package net.pacmanmvc.meteorology.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.pacmanmvc.meteorology.Meteorology;

public class ModSounds {

    public static final SoundEvent FLIPPERS_FLOP = registerSoundEvent("flippers_flop");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(Meteorology.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        Meteorology.LOGGER.info("Registering sounds");
    }
}
