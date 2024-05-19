package wyelight.hungerless.init;

import net.minecraft.commands.Commands;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wyelight.hungerless.effect.ModMobEffect;
import wyelight.hungerless.world.item.ModBowlFoodItem;
import wyelight.hungerless.world.item.ModFoodItem;
import wyelight.hungerless.world.level.block.ModCakeBlock;

public class ModInit {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "hungerless");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "hungerless");
    public static final DeferredRegister<Commands> COMMANDS = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES.getRegistryName(), "hungerless");
    public static final DeferredRegister<Item> VANILLA_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");
    public static final DeferredRegister<Block> VANILLA_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "minecraft");

    public static final RegistryObject<Block> NEW_CAKE = VANILLA_BLOCKS.register("cake",() -> new ModCakeBlock(BlockBehaviour.Properties.of(Material.CAKE).strength(0.5F).sound(SoundType.WOOL)));

    //public static final RegistryObject<Item> NEW_CAKE_ITEM = VANILLA_ITEMS.register("cake", () -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("cake")));

    public static final RegistryObject<Item> NEW_APPLE = VANILLA_ITEMS.register("apple", () -> new ModFoodItem(new Item.Properties().food(Foods.APPLE).stacksTo(4),32));
    public static final RegistryObject<Item> NEW_GLOW_BERRIES = VANILLA_ITEMS.register("glow_berries", () -> new ModFoodItem(new Item.Properties().food(Foods.GLOW_BERRIES).stacksTo(4),8));
    public static final RegistryObject<Item> NEW_SWEET_BERRIES = VANILLA_ITEMS.register("sweet_berries", () -> new ModFoodItem(new Item.Properties().food(Foods.SWEET_BERRIES).stacksTo(4),8));
    public static final RegistryObject<Item> NEW_COOKIE = VANILLA_ITEMS.register("cookie", () -> new ModFoodItem(new Item.Properties().food(Foods.COOKIE).stacksTo(8),16));
    public static final RegistryObject<Item> NEW_MELON_SLICE = VANILLA_ITEMS.register("melon_slice", () -> new ModFoodItem(new Item.Properties().food(Foods.MELON_SLICE).stacksTo(9),16));
    public static final RegistryObject<Item> NEW_DRIED_KELP = VANILLA_ITEMS.register("dried_kelp", () -> new ModFoodItem(new Item.Properties().food(Foods.DRIED_KELP).stacksTo(8),8));
    public static final RegistryObject<Item> NEW_PUMPKIN_PIE = VANILLA_ITEMS.register("pumpkin_pie", () -> new ModFoodItem(new Item.Properties().food(Foods.PUMPKIN_PIE).stacksTo(1),64));
    public static final RegistryObject<Item> NEW_RABBIT_STEW = VANILLA_ITEMS.register("rabbit_stew", () -> new ModBowlFoodItem(new Item.Properties().food(Foods.RABBIT_STEW).stacksTo(1),2));
    public static final RegistryObject<Item> NEW_MUSHROOM_STEW = VANILLA_ITEMS.register("mushroom_stew", () -> new ModBowlFoodItem(new Item.Properties().food(Foods.MUSHROOM_STEW).stacksTo(1),2));
    public static final RegistryObject<Item> NEW_BEETROOT_SOUP = VANILLA_ITEMS.register("beetroot_soup", () -> new ModBowlFoodItem(new Item.Properties().food(Foods.BEETROOT_SOUP).stacksTo(1),2));

    public static final FoodProperties ROTTEN_FLESH_PROPS = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.1F).meat().build();
    public static final RegistryObject<Item> NEW_ROTTEN_FLESH = VANILLA_ITEMS.register("rotten_flesh", () -> new ModBowlFoodItem(new Item.Properties().food(ROTTEN_FLESH_PROPS).stacksTo(4),2));

    public static final RegistryObject<Item> SWEET_BERRY_SEEDS = ITEMS.register("sweet_berry_seeds", () ->  new ItemNameBlockItem(Blocks.SWEET_BERRY_BUSH, (new Item.Properties())));
    public static final RegistryObject<Item> GLOW_BERRY_SEEDS = ITEMS.register("glow_berry_seeds", () -> new ItemNameBlockItem(Blocks.CAVE_VINES, (new Item.Properties())));

    public static final RegistryObject<MobEffect> FALL_RESISTANCE = MOB_EFFECTS.register("fall_resistance", () -> new ModMobEffect(MobEffectCategory.BENEFICIAL,16773073));
    public static final RegistryObject<MobEffect> STURDY = MOB_EFFECTS.register("sturdy", () -> new ModMobEffect(MobEffectCategory.BENEFICIAL,9520880));
    public static final RegistryObject<MobEffect> MAGIC_RESISTANCE = MOB_EFFECTS.register("magic_resistance", () -> new ModMobEffect(MobEffectCategory.BENEFICIAL,11141290));
}

