/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.ModInterface;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Auxiliary.Interfaces.ItemOnRightClick;
import Reika.ChromatiCraft.Auxiliary.Interfaces.OperationInterval;
import Reika.ChromatiCraft.Base.TileEntity.InventoriedRelayPowered;
import Reika.ChromatiCraft.Base.TileEntity.TileEntityAdjacencyUpgrade;
import Reika.ChromatiCraft.Magic.ElementTagCompound;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.Trackers.ReflectiveFailureTracker;
import Reika.DragonAPI.Instantiable.StepTimer;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaMystcraftHelper;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaMystcraftHelper.APISegment;

import com.xcompwiz.mystcraft.api.hook.SymbolAPI;
import com.xcompwiz.mystcraft.api.linking.ILinkInfo;
import com.xcompwiz.mystcraft.api.symbol.IAgeSymbol;


public class TileEntityPageExtractor extends InventoriedRelayPowered implements ItemOnRightClick, OperationInterval {

	private static Class bookClass;
	private static ItemStack lastItem;

	private static final ElementTagCompound required = new ElementTagCompound();

	private final WeightedRandom<IAgeSymbol> symbolMap = new WeightedRandom();

	private final StepTimer timer = new StepTimer(100);
	public float progressFraction;

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateEntity(world, x, y, z, meta);
		if (!world.isRemote) {
			ItemStack is = inv[0];
			if (is != lastItem) {
				this.rebuildSymbolMap(is);
				lastItem = is;
			}
			if (inv[1] == null && !symbolMap.isEmpty()) {
				IAgeSymbol ia = symbolMap.getRandomEntry();
				ItemStack page = ReikaMystcraftHelper.getSymbolPage(ia);
				//ReikaJavaLibrary.pConsole(inv[0]+" > "+symbolMap+" > "+ia.identifier());
				if (page != null) {
					timer.update();
					progressFraction = timer.getFraction();
					if (timer.checkCap()) {
						inv[1] = page.copy();
						double c = this.getConsumptionChance(ia);
						if (ReikaRandomHelper.doWithChance(c)) {
							inv[0] = null;
						}
					}
				}
				else {
					timer.reset();
				}
			}
			else {
				timer.reset();
			}
		}
	}

	private void rebuildSymbolMap(ItemStack is) {
		symbolMap.clear();
		if (is != null && is.getItem().getClass() == bookClass) {
			ILinkInfo link = ReikaMystcraftHelper.getLinkbookLink(is);
			if (link != null) {
				Integer dim = link.getDimensionUID();
				if (dim != null && DimensionManager.isDimensionRegistered(dim)) {
					DimensionManager.initDimension(dim);
					WorldServer age = DimensionManager.getWorld(dim);
					if (age != null) {
						for (String s : ReikaMystcraftHelper.getAgeSymbols(age)) {
							IAgeSymbol ia = ((SymbolAPI)ReikaMystcraftHelper.getAPI(APISegment.SYMBOL)).getSymbol(s);
							//ReikaJavaLibrary.pConsole(s+" > "+ia);
							if (ia != null) {
								int rank = ReikaMystcraftHelper.getSymbolRank(ia);
								double weight = Math.exp(-rank/2D);
								symbolMap.addEntry(ia, weight);
							}
						}
					}
				}
			}
		}
	}

	private double getConsumptionChance(IAgeSymbol ia) {
		int rank = ReikaMystcraftHelper.getSymbolRank(ia);
		//ReikaJavaLibrary.pConsole(ia.identifier()+" > "+rank+" > "+(100-98*Math.exp(-rank/1.75)));
		boolean boost = TileEntityAdjacencyUpgrade.getAdjacentUpgrade(this, CrystalElement.PURPLE) > 1;
		double f = boost ? 99.5 : 98;
		double p = boost ? 5 : 3;
		return 100-f*Math.exp(-rank/p);
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		progressFraction = NBT.getFloat("prog");
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setFloat("prog", progressFraction);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack is, int side) {
		return slot == 1;
	}

	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack is) {
		return is.getItem().getClass() == bookClass && slot == 0;
	}

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.BOOKDECOMP;
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	static {
		if (ModList.MYSTCRAFT.isLoaded()) {
			try {
				bookClass = Class.forName("com.xcompwiz.mystcraft.item.ItemAgebook");
			}
			catch (Exception e) {
				ChromatiCraft.logger.logError("Could not initialize page extractor:");
				e.printStackTrace();
				ReflectiveFailureTracker.instance.logModReflectiveFailure(ModList.MYSTCRAFT, e);
			}
		}

		required.addTag(CrystalElement.LIGHTGRAY, 20);
		required.addTag(CrystalElement.GRAY, 10);
		required.addTag(CrystalElement.BROWN, 10);
	}

	@Override
	public ItemStack onRightClickWith(ItemStack item, EntityPlayer ep) {
		if (ep.isSneaking()) {
			ItemStack ret = inv[0];
			inv[0] = null;
			ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "random.pop");
			return ret;
		}
		else if (item == null && inv[1] != null) {
			ItemStack ret = inv[1];
			inv[1] = null;
			ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "random.pop");
			return ret;
		}
		else if (inv[0] == null && item != null && this.isItemValidForSlot(0, item)) {
			inv[0] = item.copy();
			ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "random.pop");
			return null;
		}
		else {
			return item;
		}
	}

	@Override
	public float getOperationFraction() {
		return progressFraction;
	}

	@Override
	public OperationState getState() {
		return !symbolMap.isEmpty() && inv[0] != null && inv[1] == null ? energy.containsAtLeast(required) ? OperationState.RUNNING : OperationState.PENDING : OperationState.INVALID;
	}

	@Override
	protected boolean canReceiveFrom(CrystalElement e, ForgeDirection dir) {
		return this.isAcceptingColor(e) && dir != ForgeDirection.DOWN;
	}

	@Override
	protected ElementTagCompound getRequiredEnergy() {
		return required;
	}

	@Override
	public boolean isAcceptingColor(CrystalElement e) {
		return required.contains(e);
	}

	@Override
	public int getMaxStorage(CrystalElement e) {
		return 2000;
	}

}
