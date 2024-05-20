package wyelight.hungerless;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.ConfigCommand;
import org.apache.logging.log4j.LogManager;
import wyelight.hungerless.commands.PlayerSpeedwalkToggleCommand;
//import wyelight.hungerless.config.Config;
//import wyelight.hungerless.config.ConfigScreen;
import wyelight.hungerless.config.ConfigServer;
import wyelight.hungerless.init.ModInit;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.Math.min;


@Mod(Constants.MOD_ID)
public class Hungerless {
    public Hungerless() {
        //ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
        //boolean isClientSide = Objects.requireNonNull(Minecraft.getInstance().player).level.isClientSide;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        //BLOCKS.register(bus);
        ModInit.MOB_EFFECTS.register(bus);
        ModInit.ITEMS.register(bus);
        ModInit.VANILLA_ITEMS.register(bus);
        ModInit.VANILLA_BLOCKS.register(bus);

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        ConfigServer.register(ModLoadingContext.get());
        //Minecraft mc = Minecraft.getInstance();
        //new Config(mc.gameDirectory.getAbsolutePath() + File.separator + "config" + File.separator + "HungerlessConfig.cfg");
        //Config.read();
        //ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
        MinecraftForge.EVENT_BUS.register(new EventListener());

        commonInit();
        System.out.println("Hungerless Mod Init");
    }

    public static final Float playerSpeedModifier = 0.015F; // What to add to the player's walk speed if sprinting is disabled
    public static final Float mobSpeedModifier = 0.022F; // What to add to melee mob's walk speed
    //public static final Float MODIFIED_PLAYER_SPEED = 0.115F; // What the player's walk speed is set to if sprinting is disabled

    private void registerCommands(final RegisterCommandsEvent event) {
        System.out.print("Commands Registered in event");
        //new PlayerSpeedwalkEnableCommand(event.getDispatcher());
        new PlayerSpeedwalkToggleCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }

    //@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class EventListener {
        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onPlayerUpdate(LivingEvent.LivingTickEvent event) {
            if (event.getEntity() instanceof Player player) {
                if (ConfigServer.MOVEMENT_REWORK.get()) {
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
            //UpdateSpeed(event.getEntity());
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void onJoin(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof LivingEntity livingEntity){
                UpdateSpeed(livingEntity);
            }
        }
        @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
        public void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
            UpdateSpeed(event.getEntity());
        }

        @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
        public void onLivingJump(LivingEvent.LivingJumpEvent event) {
            UpdateSpeed(event.getEntity());
        }
        @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
        public void onLivingFall(LivingFallEvent event) {
            UpdateSpeed(event.getEntity());
        }

        public static void UpdateSpeed(LivingEntity livingEntity) {
            if (livingEntity instanceof Zombie || livingEntity instanceof WitherSkeleton || livingEntity instanceof Creeper || livingEntity instanceof Spider || livingEntity instanceof Wolf || livingEntity instanceof PiglinBrute || livingEntity instanceof AbstractIllager) {
                System.out.print("Confirm Entity " + livingEntity);
                AttributeModifier attributeModSpeedBoost = new AttributeModifier("SpeedBoost", mobSpeedModifier, AttributeModifier.Operation.ADDITION);
                AttributeInstance attributeInst = livingEntity.getAttribute(Attributes.MOVEMENT_SPEED);
                boolean hasMod = false;

                Set<AttributeModifier> modifierSet = Objects.requireNonNull(attributeInst).getModifiers();
                for (AttributeModifier aM : modifierSet) {
                    if (aM.getName().equals("SpeedBoost")) {
                        if (!ConfigServer.MOB_MOVEMENT_REWORK.get()) {
                            Objects.requireNonNull(attributeInst).removeModifier(aM);
                        }
                        hasMod = true;
                        System.out.print("Confirm Entity Attribute " + livingEntity);
                        break;
                    }
                }

                if (ConfigServer.MOB_MOVEMENT_REWORK.get() && !hasMod) {
                    Objects.requireNonNull(attributeInst).addTransientModifier(attributeModSpeedBoost);
                    //livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                    //System.out.print("Confirm Entity Glow " + livingEntity);
                    //System.out.println(livingEntity.getName()+" Sped up");

                }
            }
            if (livingEntity instanceof Player) {
                AttributeModifier attributeModSpeedBoost = new AttributeModifier("SpeedBoost", playerSpeedModifier, AttributeModifier.Operation.ADDITION);
                AttributeInstance attributeInst = livingEntity.getAttribute(Attributes.MOVEMENT_SPEED);
                boolean hasMod = false;

                Set<AttributeModifier> modifierSet = Objects.requireNonNull(attributeInst).getModifiers();
                for (AttributeModifier aM : modifierSet) {
                    if (aM.getName().equals("SpeedBoost")) {
                        if (!ConfigServer.MOVEMENT_REWORK.get()) {
                            Objects.requireNonNull(attributeInst).removeModifier(aM);
                        }
                        hasMod = true;
                        break;
                    }
                }

                if (ConfigServer.MOVEMENT_REWORK.get() && !hasMod) {
                    Objects.requireNonNull(attributeInst).addTransientModifier(attributeModSpeedBoost);
                    //livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                    //System.out.println(livingEntity.getName()+" Sped up");

                }
            }
        }
        public static void PlayerSpeedWalk(Player player) {
            AttributeModifier attributeModSpeedBoost = new AttributeModifier("SpeedBoost", playerSpeedModifier, AttributeModifier.Operation.ADDITION);
            AttributeInstance attributeInst = player.getAttribute(Attributes.MOVEMENT_SPEED);
            boolean hasMod = false;

            Set<AttributeModifier> modifierSet = Objects.requireNonNull(attributeInst).getModifiers();
            for (AttributeModifier aM : modifierSet) {
                if (aM.getName().equals("SpeedBoost")) {
                    Objects.requireNonNull(attributeInst).removeModifier(aM);
                    player.sendSystemMessage(Component.literal("Player removed Speedwalk"));
                    hasMod = true;
                    break;
                }
            }

            if (!hasMod) {
                Objects.requireNonNull(attributeInst).addTransientModifier(attributeModSpeedBoost);
                System.out.println("Player given Speedwalk");
                player.sendSystemMessage(Component.literal("Player given Speedwalk"));
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
        public void onServerStartingEvent(ServerStartingEvent event) {
            //event.reg
        }

        @OnlyIn(Dist.CLIENT)
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
            float dmgAmount = event.getAmount();
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
                if (dmgAmount > entity.getMaxHealth()/2 && !Objects.equals(dmgType, "fall")) {
                    //entity.sendSystemMessage(Component.literal("Resisted "+dmgAmount));
                    MobEffectInstance effectInstance = entity.getActiveEffectsMap().get(ModInit.STURDY.get());
                    event.setAmount(min(dmgAmount/(2.0F+effectInstance.getAmplifier()),entity.getMaxHealth()-2));
                    entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40+(effectInstance.getAmplifier()*10)));
                }
            }
            // New Magic Resist effect logic
            if (check_magic_resist){
                if (dmgType.equals("wither")) {
                    // Removes wither effect upon receiving wither damage
                    event.getEntity().removeEffect(MobEffects.WITHER);
                    //entity.sendSystemMessage(Component.literal("Resisted Wither"));
                }
                if (dmgType.equals("magic") || dmgType.equals("indirectMagic") || dmgType.equals("lightningBolt") || dmgType.equals("sonicBoom") || dmgType.equals("dragonBreath")){
                    MobEffectInstance effectInstance = entity.getActiveEffectsMap().get(ModInit.MAGIC_RESISTANCE.get());
                    // Resists magic damage based on multiplier
                    event.setAmount(dmgAmount/(2.0F+effectInstance.getAmplifier()));
                    //entity.sendSystemMessage(Component.literal("Resisted Magic"));
                }
            }
            //if (event.getEntity() instanceof Player player) {
                //identifies damage type
                //player.sendSystemMessage(Component.literal(dmgType+" damage!"));
            //}
        }

        @OnlyIn(Dist.CLIENT)
        public static void UpdateMovement(){

            AABB boundingBox = new AABB(
                    Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
            );
            //Predicate<Entity> alwaysTruePredicate = entity -> true;
            Player player = Objects.requireNonNull(Minecraft.getInstance().player);
            Vec3 vec3 = player.getViewVector(1.0F);
            List<Entity> entityList = player.level.getEntities(player,player.getBoundingBox().inflate(1000000D), EntitySelector.ENTITY_STILL_ALIVE);
            for (Entity e: entityList){
                if (e instanceof LivingEntity livingEntity){
                    UpdateSpeed(livingEntity);
                }
            }
            System.out.print("Getting entity list");
            System.out.print(entityList);
            //System.out.print(entityList.toString());
        }


        public static void FoodHealing(Player player, Item item) {


            boolean show_particles = ConfigServer.BONUS_EFFECTS_PARTICLES.get();
            float nutrition = Objects.requireNonNull(item.getFoodProperties()).getNutrition();
            if (item == Items.ROTTEN_FLESH) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, 0));
            }
            // Bonus Effects
            if (ConfigServer.BONUS_EFFECTS.get()) {
                if (item == Items.BAKED_POTATO) {
                    player.addEffect(new MobEffectInstance(ModInit.STURDY.get(), 800, 0, false, show_particles, true));
                } else if (item == Items.BEETROOT) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0, false, show_particles, true));
                } else if (item == Items.CARROT) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0, false, show_particles, true));
                } else if (item == Items.GOLDEN_CARROT) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0, false, show_particles, true));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0, false, show_particles, true));
                } else if (item == Items.MUSHROOM_STEW) {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 800, 0, false, show_particles, true));
                } else if (item == Items.RABBIT_STEW) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, show_particles, true));
                } else if (item == Items.BEETROOT_SOUP) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 0, false, show_particles, true));
                } else if (item == Items.PUMPKIN_PIE) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0, false, show_particles, true));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0, false, show_particles, true));
                } else if (item == Items.GLOW_BERRIES) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1, false, show_particles, true));
                    player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 800, 0));
                } else if (item == Items.HONEY_BOTTLE) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1, false, show_particles, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, show_particles, true));
                } else if (item == Items.COOKED_RABBIT) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 400, 0, false, show_particles, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, show_particles, true));
                } else if (item == Items.COOKED_COD) {
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 0, false, show_particles, true));
                } else if (item == Items.COOKED_SALMON) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 200, 0, false, show_particles, true));
                } else if (item == Items.COOKED_CHICKEN) {
                    player.addEffect(new MobEffectInstance(ModInit.FALL_RESISTANCE.get(), 600, 0, false, show_particles, true));
                } else if (item == Items.COOKED_MUTTON) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 0, false, show_particles, true));
                } else if (item == Items.COOKED_PORKCHOP) {
                    player.addEffect(new MobEffectInstance(ModInit.STURDY.get(), 800, 0, false, show_particles, true));
                } else if (item == Items.COOKED_BEEF) {
                    player.addEffect(new MobEffectInstance(ModInit.MAGIC_RESISTANCE.get(), 400, 0, false, show_particles, true));
                } else if (item == Items.BEEF) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                } else if (item == Items.PORKCHOP) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
                } else if (item == Items.CHICKEN) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
                } else if (item == Items.MUTTON) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 0));
                }
            }
            if (item == Items.PORKCHOP) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            }
            else if (item == Items.CHICKEN) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            }
            else if (item == Items.MUTTON) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            }

            if (ConfigServer.INSTANT_HEALING.get()) {
                player.heal(nutrition);
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Math.round(nutrition * 12), 2, false, false));
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

