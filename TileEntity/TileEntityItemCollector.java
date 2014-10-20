package Reika.ChromatiCraft.TileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import Reika.ChromatiCraft.ChromaticEventManager;
import Reika.ChromatiCraft.Base.TileEntity.InventoriedChromaticBase;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;

public class TileEntityItemCollector extends InventoriedChromaticBase {

	private int experience = 0;
	public boolean canIntake = false;

	public int getExperience() {
		return experience;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack is, int side) {
		return slot < 27;
	}

	@Override
	public int getSizeInventory() {
		return 45;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack is) {
		return false;
	}

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.ITEMCOLLECTOR;
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {

	}

	@Override
	protected void onFirstTick(World world, int x, int y, int z) {
		if (!world.isRemote)
			ChromaticEventManager.instance.addCollector(this);
		canIntake = true;
	}

	public boolean checkAbsorb(Entity e) {
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		if (!this.isInWorld())
			return false;
		if (!canIntake)
			return false;
		if (e.worldObj.provider.dimensionId != worldObj.provider.dimensionId)
			return false;
		if (e instanceof EntityItem || e instanceof EntityXPOrb) {
			if (Math.abs(e.posX-x) <= 16 && Math.abs(e.posY-y) <= 3 && Math.abs(e.posZ-z) <= 16) {
				if (e instanceof EntityItem) {
					EntityItem ei = (EntityItem)e;
					if (this.canAbsorbItem(ei.getEntityItem())) {
						return this.absorbItem(worldObj, x, y, z, ei);
					}
				}
				else {
					this.absorbXP(worldObj, x, y, z, (EntityXPOrb)e);
					return true;
				}
			}
		}
		return false;
	}

	private boolean canAbsorbItem(ItemStack is) {
		for (int i = 27; i < this.getSizeInventory(); i++) {
			if (ReikaItemHelper.matchStacks(is, inv[i]) && ItemStack.areItemStackTagsEqual(is, inv[i])) {
				return true;
			}
			else {

			}
		}
		return false;
	}

	private boolean absorbItem(World world, int x, int y, int z, EntityItem ent) {
		ItemStack is = ent.getEntityItem();
		int targetslot = this.checkForStack(is);
		if (targetslot != -1) {
			if (inv[targetslot] == null)
				inv[targetslot] = is.copy();
			else
				inv[targetslot].stackSize += is.stackSize;
		}
		else {
			return false;
		}
		world.playSoundEffect(x+0.5, y+0.5, z+0.5, "random.pop", 0.1F+0.5F*rand.nextFloat(), rand.nextFloat());
		ent.playSound("random.pop", 0.5F, 2F);
		return true;
	}

	private void absorbXP(World world, int x, int y, int z, EntityXPOrb xp) {
		int val = xp.getXpValue();
		experience += val;
		world.playSoundEffect(x+0.5, y+0.5, z+0.5, "random.orb", 0.1F, 0.5F * ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.8F));
		xp.playSound("random.pop", 0.5F, 2F);
	}

	private int checkForStack(ItemStack is) {
		int target = -1;
		Item id = is.getItem();
		int meta = is.getItemDamage();
		int size = is.stackSize;
		int firstempty = -1;

		for (int k = 0; k < inv.length; k++) { //Find first empty slot
			if (inv[k] == null) {
				firstempty = k;
				k = inv.length;
			}
		}
		for (int j = 0; j < inv.length; j++) {
			if (inv[j] != null) {
				if (ReikaItemHelper.matchStacks(is, inv[j])) {
					if (ItemStack.areItemStackTagsEqual(is, inv[j])) {
						if (inv[j].stackSize+size <= this.getInventoryStackLimit()) {
							target = j;
							j = inv.length;
						}
						else {
							int diff = is.getMaxStackSize() - inv[j].stackSize;
							inv[j].stackSize += diff;
							is.stackSize -= diff;
						}
					}
				}
			}
		}

		if (target == -1)
			target = firstempty;
		return target;
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT)
	{
		super.readSyncTag(NBT);
		experience = NBT.getInteger("xp");
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT)
	{
		super.writeSyncTag(NBT);
		NBT.setInteger("xp", experience);
	}

}
