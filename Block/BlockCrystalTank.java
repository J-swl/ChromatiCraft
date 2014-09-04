/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mcp.mobius.waila.api.IWailaBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.TileEntity.TileEntityCrystalTank;
import Reika.DragonAPI.Instantiable.Data.BlockArray;
import Reika.DragonAPI.Interfaces.ConnectedTextureGlass;

public class BlockCrystalTank extends Block implements IWailaBlock, ConnectedTextureGlass {

	private final ArrayList<Integer> allDirs = new ArrayList();
	private IIcon[] edges = new IIcon[10];

	public BlockCrystalTank(Material mat) {
		super(mat);
		this.setCreativeTab(ChromatiCraft.tabChroma);
		this.setHardness(1);
		this.setResistance(600);

		for (int i = 1; i < 10; i++) {
			allDirs.add(i);
		}
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		return new CrystalTankAuxTile();
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return true;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		CrystalTankAuxTile tile = (CrystalTankAuxTile)world.getTileEntity(x, y, z);
		TileEntityCrystalTank te = tile.getTankController();
		return te != null && te.getFluid() != null ? te.getFluid().getLuminosity() : 0;
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		return blockIcon;//meta == 0 ? blockIcon : ChromaIcons.TRANSPARENT.getIcon();
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		blockIcon = ico.registerIcon("chromaticraft:basic/tank2");

		for (int i = 0; i < 10; i++) {
			edges[i] = ico.registerIcon("chromaticraft:tank/tank_"+i);
		}
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return ChromatiCraft.proxy.tankRender;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		CrystalTankAuxTile te = new CrystalTankAuxTile();
		world.setTileEntity(x, y, z, te);

		BlockArray blocks = new BlockArray();
		List<Block> li = Arrays.asList(this, ChromaTiles.TANK.getBlock());
		blocks.recursiveAddMultipleWithBounds(world, x, y, z, li, x-32, y-32, z-32, x+32, y+32, z+32);

		TileEntityCrystalTank con = null;
		for (int i = 0; i < blocks.getSize(); i++) {
			int[] xyz = blocks.getNthBlock(i);
			int dx = xyz[0];
			int dy = xyz[1];
			int dz = xyz[2];
			ChromaTiles c = ChromaTiles.getTile(world, dx, dy, dz);
			if (c == ChromaTiles.TANK) {
				if (con == null) {
					con = (TileEntityCrystalTank)world.getTileEntity(dx, dy, dz);
				}
				else {
					return; //max 1 controller
				}
			}
		}

		if (con != null) {
			te.setTile(con);
			world.setBlockMetadataWithNotify(x, y, z, 1, 3);
			te.addToTank();
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block old, int oldmeta) {
		CrystalTankAuxTile te = (CrystalTankAuxTile)world.getTileEntity(x, y, z);
		if (te != null) {
			te.removeFromTank();
		}
		super.breakBlock(world, x, y, z, old, oldmeta);
	}

	public static class CrystalTankAuxTile extends TileEntity implements IFluidHandler {

		private int tileX;
		private int tileY;
		private int tileZ;

		@Override
		public boolean canUpdate() {
			return false;
		}

		public void setTile(TileEntityCrystalTank te) {
			tileX = te.xCoord;
			tileY = te.yCoord;
			tileZ = te.zCoord;
		}

		public void addToTank() {
			TileEntityCrystalTank te = this.getTankController();
			if (te != null)
				te.addCoordinate(xCoord, yCoord, zCoord);
		}

		public void removeFromTank() {
			TileEntityCrystalTank te = this.getTankController();
			if (te != null)
				te.removeCoordinate(xCoord, yCoord, zCoord);
		}

		public TileEntityCrystalTank getTankController() {
			TileEntity te = worldObj.getTileEntity(tileX, tileY, tileZ);
			return te instanceof TileEntityCrystalTank ? (TileEntityCrystalTank)te : null;
		}

		@Override
		public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
			TileEntityCrystalTank te = this.getTankController();
			return te != null ? te.fill(from, resource, doFill) : 0;
		}

		@Override
		public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
			TileEntityCrystalTank te = this.getTankController();
			return te != null ? te.drain(from, resource, doDrain) : null;
		}

		@Override
		public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
			TileEntityCrystalTank te = this.getTankController();
			return te != null ? te.drain(from, maxDrain, doDrain) : null;
		}

		@Override
		public boolean canFill(ForgeDirection from, Fluid fluid) {
			TileEntityCrystalTank te = this.getTankController();
			return te != null && te.canFill(from, fluid);
		}

		@Override
		public boolean canDrain(ForgeDirection from, Fluid fluid) {
			TileEntityCrystalTank te = this.getTankController();
			return te != null && te.canDrain(from, fluid);
		}

		@Override
		public FluidTankInfo[] getTankInfo(ForgeDirection from) {
			TileEntityCrystalTank te = this.getTankController();
			return te != null ? te.getTankInfo(from) : new FluidTankInfo[0];
		}

		@Override
		public void writeToNBT(NBTTagCompound NBT) {
			super.writeToNBT(NBT);

			NBT.setInteger("tx", tileX);
			NBT.setInteger("ty", tileY);
			NBT.setInteger("tz", tileZ);
		}

		@Override
		public void readFromNBT(NBTTagCompound NBT) {
			super.readFromNBT(NBT);

			tileX = NBT.getInteger("tx");
			tileY = NBT.getInteger("ty");
			tileZ = NBT.getInteger("tz");
		}

		@Override
		public Packet getDescriptionPacket() {
			NBTTagCompound NBT = new NBTTagCompound();
			this.writeToNBT(NBT);
			S35PacketUpdateTileEntity pack = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, NBT);
			return pack;
		}

		@Override
		public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity p)  {
			this.readFromNBT(p.field_148860_e);
		}

	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor acc, IWailaConfigHandler cfg) {
		return new ItemStack(this);
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor acc, IWailaConfigHandler config) {
		World world = acc.getWorld();
		MovingObjectPosition mov = acc.getPosition();
		if (mov != null) {
			int x = mov.blockX;
			int y = mov.blockY;
			int z = mov.blockZ;
			currenttip.add(EnumChatFormatting.WHITE+this.getPickBlock(mov, world, x, y, z).getDisplayName());
		}
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack is, List<String> currenttip, IWailaDataAccessor acc, IWailaConfigHandler cfg) {
		CrystalTankAuxTile te = (CrystalTankAuxTile)acc.getTileEntity();
		TileEntityCrystalTank tank = te.getTankController();
		if (tank != null) {
			tank.syncAllData(false);
			int amt = tank.getLevel();
			int capacity = tank.getCapacity();
			Fluid f = tank.getFluid();
			if (amt > 0 && f != null) {
				currenttip.add(String.format("Tank: %dmB/%dmB of %s", amt, capacity, f.getLocalizedName()));
			}
			else {
				currenttip.add(String.format("Tank: Empty (Capacity %dmB)", capacity));
			}
		}
		else {
			currenttip.add("No Tank");
		}
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack is, List<String> currenttip, IWailaDataAccessor acc, IWailaConfigHandler cfg) {
		String s1 = EnumChatFormatting.ITALIC.toString();
		String s2 = EnumChatFormatting.BLUE.toString();
		currenttip.add(s2+s1+"ChromatiCraft");
		return currenttip;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess iba, int x, int y, int z, int side) {
		ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[side];
		int dx = x+dir.offsetX;
		int dy = y+dir.offsetY;
		int dz = z+dir.offsetZ;
		return iba.getBlock(dx, dy, dz) != this && ChromaTiles.getTile(iba, dx, dy, dz) != ChromaTiles.TANK;
	}

	public ArrayList<Integer> getEdgesForFace(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		ArrayList<Integer> li = new ArrayList();
		li.addAll(allDirs);

		if (world.getBlockMetadata(x, y, z) == 0)
			return li;

		li.remove(new Integer(5)); //glass tex

		if (face.offsetX != 0) { //test YZ
			//sides; removed if have adjacent on side
			if (world.getBlock(x, y, z+1) == this || ChromaTiles.getTile(world, x, y, z+1) == ChromaTiles.TANK)
				li.remove(new Integer(2));
			if (world.getBlock(x, y, z-1) == this || ChromaTiles.getTile(world, x, y, z-1) == ChromaTiles.TANK)
				li.remove(new Integer(8));
			if (world.getBlock(x, y+1, z) == this || ChromaTiles.getTile(world, x, y+1, z) == ChromaTiles.TANK)
				li.remove(new Integer(4));
			if (world.getBlock(x, y-1, z) == this || ChromaTiles.getTile(world, x, y-1, z) == ChromaTiles.TANK)
				li.remove(new Integer(6));

			//Corners; only removed if have adjacent on side AND corner
			if (world.getBlock(x, y+1, z+1) == this && !li.contains(4) && !li.contains(2))
				li.remove(new Integer(1));
			if (world.getBlock(x, y-1, z-1) == this && !li.contains(6) && !li.contains(8))
				li.remove(new Integer(9));
			if (world.getBlock(x, y+1, z-1) == this && !li.contains(4) && !li.contains(8))
				li.remove(new Integer(7));
			if (world.getBlock(x, y-1, z+1) == this && !li.contains(2) && !li.contains(6))
				li.remove(new Integer(3));
		}
		if (face.offsetY != 0) { //test XZ
			//sides; removed if have adjacent on side
			if (world.getBlock(x, y, z+1) == this || ChromaTiles.getTile(world, x, y, z+1) == ChromaTiles.TANK)
				li.remove(new Integer(2));
			if (world.getBlock(x, y, z-1) == this || ChromaTiles.getTile(world, x, y, z-1) == ChromaTiles.TANK)
				li.remove(new Integer(8));
			if (world.getBlock(x+1, y, z) == this || ChromaTiles.getTile(world, x+1, y, z) == ChromaTiles.TANK)
				li.remove(new Integer(4));
			if (world.getBlock(x-1, y, z) == this || ChromaTiles.getTile(world, x-1, y, z) == ChromaTiles.TANK)
				li.remove(new Integer(6));

			//Corners; only removed if have adjacent on side AND corner
			if (world.getBlock(x+1, y, z+1) == this && !li.contains(4) && !li.contains(2))
				li.remove(new Integer(1));
			if (world.getBlock(x-1, y, z-1) == this && !li.contains(6) && !li.contains(8))
				li.remove(new Integer(9));
			if (world.getBlock(x+1, y, z-1) == this && !li.contains(4) && !li.contains(8))
				li.remove(new Integer(7));
			if (world.getBlock(x-1, y, z+1) == this && !li.contains(2) && !li.contains(6))
				li.remove(new Integer(3));
		}
		if (face.offsetZ != 0) { //test XY
			//sides; removed if have adjacent on side
			if (world.getBlock(x, y+1, z) == this || ChromaTiles.getTile(world, x, y+1, z) == ChromaTiles.TANK)
				li.remove(new Integer(4));
			if (world.getBlock(x, y-1, z) == this || ChromaTiles.getTile(world, x, y-1, z) == ChromaTiles.TANK)
				li.remove(new Integer(6));
			if (world.getBlock(x+1, y, z) == this || ChromaTiles.getTile(world, x+1, y, z) == ChromaTiles.TANK)
				li.remove(new Integer(2));
			if (world.getBlock(x-1, y, z) == this || ChromaTiles.getTile(world, x-1, y, z) == ChromaTiles.TANK)
				li.remove(new Integer(8));

			//Corners; only removed if have adjacent on side AND corner
			if (world.getBlock(x+1, y+1, z) == this && !li.contains(2) && !li.contains(4))
				li.remove(new Integer(1));
			if (world.getBlock(x-1, y-1, z) == this && !li.contains(8) && !li.contains(6))
				li.remove(new Integer(9));
			if (world.getBlock(x+1, y-1, z) == this && !li.contains(2) && !li.contains(6))
				li.remove(new Integer(3));
			if (world.getBlock(x-1, y+1, z) == this && !li.contains(4) && !li.contains(8))
				li.remove(new Integer(7));
		}
		return li;
	}

	public IIcon getIconForEdge(int edge) {
		return edges[edge];
	}

	@Override
	public boolean renderCentralTextureForItem(int meta) {
		return false;
	}

}