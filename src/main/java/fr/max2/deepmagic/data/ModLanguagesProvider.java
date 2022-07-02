package fr.max2.deepmagic.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import fr.max2.deepmagic.DeepMagicMod;
import fr.max2.deepmagic.init.ModItemGroups;
import fr.max2.deepmagic.init.ModItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.data.DataProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguagesProvider implements DataProvider 
{
	private final List<LanguagePartProvider> languages = new ArrayList<>();
	
	private ModLanguagesProvider(DataGenerator gen, String modId, String... locales)
	{
		for (String locale : locales)
		{
			this.languages.add(new LanguagePartProvider(gen, modId, locale));
		}
	}
	
	public ModLanguagesProvider(DataGenerator gen)
	{
		this(gen, DeepMagicMod.MOD_ID, "en_us", "fr_fr");
	}
	
	protected void addTranslations()
    {
    	// Items
		add(ModItems.DEEP_DARK_DUST, "Deep Dark Dust", "Poudre des abîbes");
    	
    	// ItemGroups
    	add(ModItemGroups.MAIN_TAB, "DeepMagic", "DeepMagic");
    }
	
	@Override
	public void run(CachedOutput cache) throws IOException
	{
		this.addTranslations();
		for (LanguageProvider language : this.languages)
		{
			language.run(cache);
		}
	}
	
	protected void add(Supplier<? extends Item> key, String... names)
	{
		add(key.get().getDescriptionId(), names);
	}
	
	protected void add(CreativeModeTab key, String... names)
	{
		TranslatableContents contents = (TranslatableContents)key.getDisplayName().getContents();
		add(contents.getKey(), names);
	}
	
	protected void add(String key, String... values)
	{
		for (int i = 0; i < this.languages.size(); i++)
		{
			this.languages.get(i).add(key, values[i]);
		}
	}
	
	@Override
	public String getName()
	{
		return "Factinventory Languages";
	}

	private static class LanguagePartProvider extends LanguageProvider
	{
		public LanguagePartProvider(DataGenerator gen, String modid, String locale)
		{
			super(gen, modid, locale);
		}

		@Override
		protected void addTranslations()
		{ }
	}
}
