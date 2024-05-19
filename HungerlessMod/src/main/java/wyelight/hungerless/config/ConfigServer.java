package wyelight.hungerless.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigServer {
    public static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SERVER_CONFIG;

    public static final ForgeConfigSpec.BooleanValue MOVEMENT_REWORK;
    public static final ForgeConfigSpec.BooleanValue MOB_MOVEMENT_REWORK;
    public static final ForgeConfigSpec.BooleanValue BONUS_EFFECTS;
    public static final ForgeConfigSpec.BooleanValue BONUS_EFFECTS_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue INSTANT_HEALING;

    static {
        SERVER_BUILDER.push("General Settings");

        MOVEMENT_REWORK = SERVER_BUILDER
                .comment("Rework player movement")
                .define("movementRework", true);

        MOB_MOVEMENT_REWORK = SERVER_BUILDER
                .comment("Rework mob movement")
                .define("mobMovementRework", true);

        BONUS_EFFECTS = SERVER_BUILDER
                .comment("Enable bonus effects")
                .define("bonusEffects", true);

        BONUS_EFFECTS_PARTICLES = SERVER_BUILDER
                .comment("Enable bonus effects particles")
                .define("bonusEffectsParticles", true);

        INSTANT_HEALING = SERVER_BUILDER
                .comment("Enable instant healing")
                .define("instantHealing", false);

        SERVER_BUILDER.pop();

        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    public static void register(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
    }
}