package wyelight.hungerless;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import wyelight.hungerless.config.Config;
import wyelight.hungerless.config.ConfigScreen;
import wyelight.hungerless.init.ModInit;

import java.io.File;
import java.util.Objects;

import static wyelight.hungerless.config.Config.read;


@Mod(Constants.MOD_ID)
public class Hungerless {
    public Hungerless() {
        //ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        //BLOCKS.register(bus);
        ModInit.ITEMS.register(bus);
        ModInit.VANILLA_ITEMS.register(bus);
        ModInit.VANILLA_BLOCKS.register(bus);
        /*
        MinecraftForge.EVENT_BUS
        public static boolean isClient = false;
        */
        Minecraft mc = Minecraft.getInstance();
        Config config = new Config(mc.gameDirectory.getAbsolutePath() + File.separator + "config" + File.separator + "ValiantConfig.cfg");
        config.read();
        MinecraftForge.EVENT_BUS.register(new EventListener());
        System.out.println("GotWood Mod Init");
        commonInit();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
    }

    //@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class EventListener {
        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onPlayerUpdate(LivingEvent.LivingTickEvent event) {
            if (event.getEntity() instanceof Player player) {
                if (Config.movementRework) {
                    player.getFoodData().setFoodLevel(3);
                    if (!player.isInFluidType() && !player.isCreative()) {
                        player.setSprinting(false);
                    }
                }
                else {
                    player.getFoodData().setFoodLevel(9);
                }
                /*
                if (Config.autoSwim) {
                    //Vec3 vec3 = player.getDeltaMovement();
                    if (player.canSwimInFluidType(player.getEyeInFluidType()) && !player.isShiftKeyDown()) {
                        if (player.getForcedPose() != Pose.SWIMMING) {
                            player.setForcedPose(Pose.SWIMMING);
                        }

                        player.setSwimming(true);
                        boolean swimForwardInput = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_W);
                        //boolean swimBackwardInput = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_S);
                        if (swimForwardInput) {
                            Vec3 lookvec = player.getLookAngle();
                            lookvec = lookvec.multiply(0.2, 0.2, 0.2);
                            player.setDeltaMovement(lookvec);
                        }
                    } else {
                        if (player.getForcedPose() == Pose.SWIMMING) {
                            player.setForcedPose(null);
                        }
                    }
                }*/
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onJoin(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof Player player) {
                if (Config.movementRework) {
                    player.getAbilities().setFlyingSpeed(0.06F);
                    player.getAbilities().setWalkingSpeed(0.115F);
                } else {
                    player.getAbilities().setWalkingSpeed(0.1F);
                    player.getAbilities().setFlyingSpeed(0.02F);
                }
            }
            if (Config.movementRework) {
                if (event.getEntity() instanceof Spider spider) {
                    Objects.requireNonNull(spider.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.32);
                }
                if (event.getEntity() instanceof Zombie zombie) {
                    if (!zombie.isBaby()) {
                        Objects.requireNonNull(zombie.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.32);
                    }
                }
                if (event.getEntity() instanceof Creeper creeper) {
                    Objects.requireNonNull(creeper.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.30);
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
            Player player = event.getEntity();
            if (Config.movementRework) {
                player.getAbilities().setFlyingSpeed(0.06F);
                player.getAbilities().setWalkingSpeed(0.115F);
            } else {
                player.getAbilities().setWalkingSpeed(0.1F);
                player.getAbilities().setFlyingSpeed(0.02F);
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
            ResourceLocation id = event.getOverlay().id();
            if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
                event.setCanceled(true);
            }
            /*
            if (Minecraft.getInstance().gui instanceof ForgeGui gui) {
                if (id.equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
                    gui.rightHeight += 3;
                    gui.leftHeight -= 7;
                }

                if (id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id())) {
                    Player player = gui.getMinecraft().player;

                    if (player != null) {
                        event.getPoseStack().translate(101F, 10, 0);

                    }
                }
            }
            */
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onOverlayPostEvent(RenderGuiOverlayEvent.Post event) {
            ResourceLocation id = event.getOverlay().id();
            /*
            if (id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id()) && Minecraft.getInstance().player != null) {
                event.getPoseStack().translate(-101F, -10, 0);
            }*/
        }

        public static void FoodHealing(Player player, Item item) {
            if (item == Items.ROTTEN_FLESH) {
                if (Math.random() > 0.6) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 2));
                }
            } else if (item == Items.BAKED_POTATO) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 0));
            } else if (item == Items.BEETROOT) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0));
            } else if (item == Items.CARROT) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 120, 0));
            } else if (item == Items.GOLDEN_CARROT) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 440, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 440, 0));
            } else if (item == Items.MUSHROOM_STEW) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 4, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 800, 0));
            } else if (item == Items.RABBIT_STEW) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 4, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0));
            } else if (item == Items.BEETROOT_SOUP) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 25, 4, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0));
            } else if (item == Items.PUMPKIN_PIE) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 4, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
            } else if (item == Items.GLOW_BERRIES) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 800, 0));
            } else if (item == Items.HONEY_BOTTLE) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
            } else if (item == Items.COOKED_RABBIT) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
            } else if (item == Items.COOKED_COD) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 0));
            } else if (item == Items.COOKED_SALMON) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 200, 0));
            } else if (item == Items.COOKED_CHICKEN) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0));
            } else if (item == Items.COOKED_MUTTON) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0));
            } else if (item == Items.COOKED_PORKCHOP) {
                player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1));
            } else if (item == Items.COOKED_BEEF) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Objects.requireNonNull(item.getFoodProperties()).getNutrition() * 12, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 2));
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
    public void commonInit()
    {
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item.isEdible() && !(item == Items.MUSHROOM_STEW || item == Items.RABBIT_STEW || item == Items.SUSPICIOUS_STEW || item == Items.BEETROOT_SOUP || item == Items.COOKIE || item == Items.DRIED_KELP || item == Items.PUMPKIN_PIE)) {
                ObfuscationReflectionHelper.setPrivateValue(Item.class, item, 4, "maxStackSize");
            }
        }
        System.out.println("Common Event");
    }
}