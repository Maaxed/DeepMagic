package fr.max2.deepmagic.init;

import java.util.function.Function;
import java.util.function.Supplier;

import fr.max2.deepmagic.DeepMagicMod;
import fr.max2.deepmagic.item.ConfigurationWandItem;
import fr.max2.deepmagic.item.TransportationWandItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(bus = Bus.MOD, modid = DeepMagicMod.MOD_ID)
public class ModItems
{
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, DeepMagicMod.MOD_ID);
	public static final RegistryObject<Item> DEEP_DARK_PEARL = register("black_hole", Item::new);
	public static final RegistryObject<TransportationWandItem> TRANSPORTATION_WAND = register("black_hole_wand", TransportationWandItem::new);
	public static final RegistryObject<ConfigurationWandItem> CONFIGURATION_WAND = register("black_hole_configurator", ConfigurationWandItem::new);

	private static final Supplier<Properties> DEFAULT_PROPERTIES = () -> new Properties().tab(ModItemGroups.MAIN_TAB);

	public static <I extends Item> RegistryObject<I> register(String name, Function<Properties, I> itemConstructor)
	{
	    return register(name, () -> itemConstructor.apply(DEFAULT_PROPERTIES.get()));
	}

	public static <I extends Item> RegistryObject<I> register(String name, Supplier<I> item)
	{
	    return REGISTRY.register(name, item);
	}
}
