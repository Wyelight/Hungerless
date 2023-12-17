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
                    player.getAbilities().setWalkingSpeed(MODIFIED_PLAYER_SPEED);
                    player.getAbilities().setFlyingSpeed(MODIFIED_PLAYER_SPEED);
                    if (!player.isInFluidType() && !player.isCreative()) {
                        player.setSprinting(false);
                    }
                }
                else {
                    boolean check_hunger = player.getActiveEffectsMap().containsKey(MobEffects.HUNGER);
                    if (check_hunger) {
                        player.getFoodData().setFoodLevel(3);
                    }
                    else {
                        player.getFoodData().setFoodLevel(9);
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onJoin(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof Player player) {
                if (Config.movementRework) {
                    Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(MODIFIED_PLAYER_SPEED);
                }
            }
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
                FoodHealing(player, itemstack.getItem());
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onOverlayEvent(RenderGuiOverlayEvent.Pre event) {
            //ResourceLocation id = event.getOverlay().id();
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

            if (check_fall_resist && Objects.equals(dmgType, "fall")){
                MobEffectInstance effectInstance = entity.getActiveEffectsMap().get(ModInit.FALL_RESISTANCE.get());
                event.setAmount(dmgAmount/(2+effectInstance.getAmplifier()));   // reduces fall damage
            }

            if (check_sturdy){
                if (entity.getHealth() == entity.getMaxHealth() && dmgAmount >= entity.getMaxHealth()) {
                    event.setAmount(entity.getMaxHealth() - 1); // Makes it impossible to "one shot" this entity
                }
                if (entity.getHealth() <= 6){
                    event.setAmount(dmgAmount/2.0F); // Reduce damage taken when low health
                }
            }

            if (check_magic_resist){
                if (dmgType.equals("wither")) {
                    event.setAmount(0.0F);
                    //entity.sendSystemMessage(Component.literal("Resisted Wither"));
                }
                if (dmgType.equals("magic") || dmgType.equals("indirectMagic") || dmgType.equals("lightningBolt") || dmgType.equals("sonicBoom") || dmgType.equals("dragonBreath")){
                    MobEffectInstance effectInstance = entity.getActiveEffectsMap().get(ModInit.MAGIC_RESISTANCE.get());
                    event.setAmount(dmgAmount/(2.0F+effectInstance.getAmplifier()));
                    //event.setAmount(0.0F);
                    //entity.sendSystemMessage(Component.literal("Resisted Magic"));
                }
            }

            if (event.getEntity() instanceof Player player) {
                //player.sendSystemMessage(Component.literal(dmgType+" damage!"));
            }
        }

        public static void FoodHealing(Player player, Item item) {
            Config.read();
            if (item == Items.ROTTEN_FLESH) {
                if (Math.random() > 0.6) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 2));
                }
            } else if (item == Items.POTATO) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(ModInit.STURDY.get(), 200, 0));
                }
            } else if (item == Items.BAKED_POTATO) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(ModInit.STURDY.get(), 600, 0));
                }
            } else if (item == Items.BEETROOT) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0));
                }
            } else if (item == Items.CARROT) {
                player.getAbilities().setWalkingSpeed(0.315F);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0));
                }
            } else if (item == Items.GOLDEN_CARROT) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 440, 1, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 440, 0));
                }
            } else if (item == Items.MUSHROOM_STEW) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 4, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 800, 0));
                }
            } else if (item == Items.RABBIT_STEW) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 4, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0));
                }
            } else if (item == Items.BEETROOT_SOUP) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 4, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0));
                }
            } else if (item == Items.PUMPKIN_PIE) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 4, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
                }
            } else if (item == Items.GLOW_BERRIES) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1));
                }
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 800, 0));
            } else if (item == Items.HONEY_BOTTLE) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
                }
            } else if (item == Items.COOKED_RABBIT) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 400, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
                }
            } else if (item == Items.COOKED_COD) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 0));
                }
            } else if (item == Items.COOKED_SALMON) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 200, 0));
                }
            } else if (item == Items.COOKED_CHICKEN) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(ModInit.FALL_RESISTANCE.get(), 400, 0));
                }
            } else if (item == Items.COOKED_MUTTON) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0));
                }
            } else if (item == Items.COOKED_PORKCHOP) {
                player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
            } else if (item == Items.COOKED_BEEF) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Config.bonusEffects) {
                    player.addEffect(new MobEffectInstance(ModInit.MAGIC_RESISTANCE.get(), 400, 0));
                }
            } else if (item == Items.BEEF) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Math.random() > 0.5) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1));
                }
            } else if (item == Items.PORKCHOP) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Math.random() > 0.5) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 0));
                }
            } else if (item == Items.CHICKEN) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Math.random() > 0.2) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0));
                }
            } else if (item == Items.MUTTON) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                if (Math.random() > 0.5) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 240, 0));
                }
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
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