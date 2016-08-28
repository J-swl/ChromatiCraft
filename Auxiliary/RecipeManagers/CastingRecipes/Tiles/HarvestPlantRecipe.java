package Reika.ChromatiCraft.Auxiliary.RecipeManagers.CastingRecipes.Tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import Reika.ChromatiCraft.Auxiliary.RecipeManagers.CastingRecipe.TempleCastingRecipe;
import Reika.ChromatiCraft.Registry.CrystalElement;


public class HarvestPlantRecipe extends TempleCastingRecipe {

	public HarvestPlantRecipe(ItemStack out, IRecipe recipe) {
		super(out, recipe);

		this.addRune(CrystalElement.YELLOW, -5, 0, -2);
		this.addRune(CrystalElement.GREEN, -5, 0, 2);
	}

}
