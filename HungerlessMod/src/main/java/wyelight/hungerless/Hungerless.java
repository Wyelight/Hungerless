package wyelight.hungerless;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import wyelight.hungerless.config.Config;
import wyelight.hungerless.config.ConfigScreen;
import wyelight.hungerless.init.ModInit;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.Math.min;


@Mod(Constants.MOD_ID)
public class Hungerless {
    public Hungerless() throws IllegalAccessException {
        //ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        //BLOCKS.register(bus);
        ModInit.MOB_EFFECTS.register(bus);
        ModInit.ITEMS.register(bus);
        ModInit.VANILLA_ITEMS.register(bus);
        ModInit.VANILLA_BLOCKS.register(bus);
        Minecraft mc = Minecraft.getInstance();
        new Config(mc.gameDirectory.getAbsolutePath() + File.separator + "config" + File.separator + "HungerlessConfig.cfg");
        Config.read();
        MinecraftForge.EVENT_BUS.register(new EventListener());
        System.out.println("GotWood Mod Init");
        commonInit();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
    }

    public static final Float MODIFIED_PLAYER_SPEED = 0.115F; // What the player's walk speed is set to if sprinting is disabled

    //public static final Float MODIFIED_PLAYER_SPEED = 0.115F; // What the player's walk speed is set to if sprinting is disabled

    //@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class EventListener {
        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onPlayerUpdate(LivingEvent.LivingTickEvent event) {
            if (event.getEntity() instanceof Player player) {
                if (Config.movementRework) {
                    player.getFoodData().setFoodLevel(3);
                    //player.getAbilities().setWalkingSpeed(MODIFIED_PLAYER_SPEED);
                    //player.getAbilities().setFlyingSpeed(MODIFIED_PLAYER_SPEED);
                    // This disables the new swimming method
                    if (!player.isInFluidType() && !player.isCreative()) {
                        player.setSprinting(false);
                    }
                }
                else {
                    // Can no longer sprint when afflicted with the hunger status effect
                    boolean check_hunger = player.getActiveEffectsMap().containsKey(MobEffects.HUNGER);
                    if (check_hunger) {
                        // Sets food level below regen and sprinting level
                        player.getFoodData().setFoodLevel(3);
                    }
                    else {
                        // Sets food level below regen level
                        player.getFoodData().setFoodLevel(9);
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onJoin(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof Player player) {
                // Increases movement speed of player if sprinting is disabled
                if (Config.movementRework) {
                    Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(MODIFIED_PLAYER_SPEED);
                }
            }
            // Increases movement speed of common hostile mobs
            if (Config.mobMovementRework) {
                if (event.getEntity() instanceof Spider spider) {
                    Objects.requireNonNull(spider.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.35F);
                }
                if (event.getEntity() instanceof Zombie zombie) {
                    if (!zombie.isBaby()) {
                        Objects.requireNonNull(zombie.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.25F);
                    }
                }
                if (event.getEntity() instanceof Creeper creeper) {
                    Objects.requireNonNull(creeper.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.30F);
                }
                if (event.getEntity() instanceof WitherSkeleton witherSkeleton) {
                    Objects.requireNonNull(witherSkeleton.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.35F);
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
            ItemStack itemstack = event.getItem();
            if (event.getEntity() instanceof Player player && itemstack.isEdible()) {
                // Triggers food healing and possibly bonus effect
                FoodHealing(player, itemstack.getItem());
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onOverlayEvent(RenderGuiOverlayEvent.Pre event) {
            //ResourceLocation id = event.getOverlay().id();
            // Disables food display bar
            if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onDamageEvent(LivingHurtEvent event) {

            String dmgType = event.getSource().getMsgId();
            Float dmgAmount = event.getAmount();
            LivingEntity entity = event.getEntity();

            boolean check_fall_resist = entity.getActiveEffectsMap().containsKey(ModInit.FALL_RESISTANCE.get());
            boolean check_sturdy = entity.getActiveEffectsMap().containsKey(ModInit.STURDY.get());
            boolean check_magic_resist = entity.getActiveEffectsMap().containsKey(ModInit.MAGIC_RESISTANCE.get());

            // New Fall Resistance effect logic
            if (check_fall_resist && Objects.equals(dmgType, "fall")) {
                MobEffectInstance effectInstance = entity.getActiveEffectsMap().get(ModInit.FALL_RESISTANCE.get());
                event.setAmount(dmgAmount / (2 + effectInstance.getAmplifier()));   // reduces fall damage
            }
            // New Study effect logic
            if (check_sturdy){
                if (entity.getHealth() == entity.getMaxHealth() && dmgAmount >= entity.getMaxHealth()) {
                    event.setAmount(entity.getMaxHealth() - 2); // Makes it impossible to "one shot" this entity
                }
                /*
                if (entity.getHealth() <= 6){
                    event.setAmount(dmgAmount/2.0F); // Reduce damage taken when low health
                }*/
            }
            // New Magic Resist effect logic
            if (check_magic_resist){
                if (dmgType.equals("wither")) {
                    // Disables all wither damage
                    event.setAmount(0.0F);

                    //entity.sendSystemMessage(Component.literal("Resisted Wither"));
                }
                if (dmgType.equals("magic") || dmgType.equals("indirectMagic") || dmgType.equals("lightningBolt") || dmgType.equals("sonicBoom") || dmgType.equals("dragonBreath")){
                    MobEffectInstance effectInstance = entity.getActiveEffectsMap().get(ModInit.MAGIC_RESISTANCE.get());
                    // Resists magic damage based on multiplier
                    event.setAmount(dmgAmount/(2.0F+effectInstance.getAmplifier()));
                    //event.setAmount(0.0F);
                    //entity.sendSystemMessage(Component.literal("Resisted Magic"));
                }
            }

            //if (event.getEntity() instanceof Player player) {
                //player.sendSystemMessage(Component.literal(dmgType+" damage!"));
            //}
        }

        public static void FoodHealing(Player player, Item item) {
            Config.read();
            boolean show_particles = Config.bonusEffectsParticles;
            float nutrition = Objects.requireNonNull(item.getFoodProperties()).getNutrition();
            if (item == Items.ROTTEN_FLESH) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, 0));
            }
            // Bonus Effects
            if (item == Items.BAKED_POTATO) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(ModInit.STURDY.get(), 800, 0, false, show_particles, true));
                }
            } else if (item == Items.BEETROOT) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0, false, show_particles, true));
                }
            } else if (item == Items.CARROT) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0, false, show_particles, true));
                }
            } else if (item == Items.GOLDEN_CARROT) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0, false, show_particles, true));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0, false, show_particles, true));
                }
            } else if (item == Items.MUSHROOM_STEW) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 800, 0, false, show_particles, true));
                }
            } else if (item == Items.RABBIT_STEW) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, show_particles, true));
                }
            } else if (item == Items.BEETROOT_SOUP) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 0, false, show_particles, true));
                }
            } else if (item == Items.PUMPKIN_PIE) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0, false, show_particles, true));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0, false, show_particles, true));
                }
            } else if (item == Items.GLOW_BERRIES) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1, false, show_particles, true));
                }
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 800, 0));
            } else if (item == Items.HONEY_BOTTLE) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1, false, show_particles, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, show_particles, true));
                }
            } else if (item == Items.COOKED_RABBIT) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 400, 0, false, show_particles, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, show_particles, true));
                }
            } else if (item == Items.COOKED_COD) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 0, false, show_particles, true));
                }
            } else if (item == Items.COOKED_SALMON) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 200, 0, false, show_particles, true));
                }
            } else if (item == Items.COOKED_CHICKEN) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(ModInit.FALL_RESISTANCE.get(), 600, 0, false, show_particles, true));
                }
            } else if (item == Items.COOKED_MUTTON) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 0, false, show_particles, true));
                }
            } else if (item == Items.COOKED_PORKCHOP) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(ModInit.STURDY.get(), 800, 0, false, show_particles, true));
                }
            } else if (item == Items.COOKED_BEEF) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(ModInit.MAGIC_RESISTANCE.get(), 400, 0, false, show_particles, true));
                }
            } else if (item == Items.BEEF) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                }
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            } else if (item == Items.PORKCHOP) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
                }
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            } else if (item == Items.CHICKEN) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
                }
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            } else if (item == Items.MUTTON) {
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 0));
                }
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            }
            if (Config.instantHealing) {
                player.heal(nutrition);
            }
            else{
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Math.round(nutrition*12) , 2, false, false));
            }
        }

    }
    public void commonInit(){
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item.isEdible() && new ItemStack(item).getMaxStackSize() == 64) {
                try {
                    ObfuscationReflectionHelper.setPrivateValue(Item.class, item, 4, "f_41370_");
                }
                catch (Exception err) {
                    LogManager.getLogger().error("Failed to change stack size.");
                }
            }
        }
        System.out.println("Common Event");

    }
}