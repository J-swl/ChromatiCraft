// Date: 01/08/2016 5:50:53 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package Reika.ChromatiCraft.Models;

import java.util.ArrayList;

import net.minecraft.tileentity.TileEntity;
import Reika.ChromatiCraft.Base.ChromaModelBase;
import Reika.DragonAPI.Instantiable.Rendering.LODModelPart;

public class ModelFluidRelay extends ChromaModelBase
{
	//fields
	LODModelPart Shape1;
	LODModelPart Shape2;
	LODModelPart Shape3;
	LODModelPart Shape4;
	LODModelPart Shape4a;
	LODModelPart Shape4b;
	LODModelPart Shape4c;
	LODModelPart Shape5;

	public ModelFluidRelay() {
		textureWidth = 32;
		textureHeight = 32;

		Shape1 = new LODModelPart(this, 8, 4);
		Shape1.addBox(0F, 0F, 0F, 1, 1, 1);
		Shape1.setRotationPoint(-0.5F, 19F, -0.5F);
		Shape1.setTextureSize(32, 32);
		Shape1.mirror = true;
		this.setRotation(Shape1, 0F, 0F, 0F);
		Shape2 = new LODModelPart(this, 8, 0);
		Shape2.addBox(0F, 0F, 0F, 2, 1, 2);
		Shape2.setRotationPoint(-1F, 20F, -1F);
		Shape2.setTextureSize(32, 32);
		Shape2.mirror = true;
		this.setRotation(Shape2, 0F, 0F, 0F);
		Shape3 = new LODModelPart(this, 17, 0);
		Shape3.addBox(0F, 0F, 0F, 3, 3, 3);
		Shape3.setRotationPoint(-1.5F, 21F, -1.5F);
		Shape3.setTextureSize(32, 32);
		Shape3.mirror = true;
		this.setRotation(Shape3, 0F, 0F, 0F);
		Shape4 = new LODModelPart(this, 0, 14);
		Shape4.addBox(-0.5F, -0.4F, -0.8F, 1, 6, 1);
		Shape4.setRotationPoint(0F, 20F, 0F);
		Shape4.setTextureSize(32, 32);
		Shape4.mirror = true;
		this.setRotation(Shape4, -0.5235988F, 0F, 0F);
		Shape4a = new LODModelPart(this, 5, 14);
		Shape4a.addBox(-0.8F, -0.4F, -0.5F, 1, 6, 1);
		Shape4a.setRotationPoint(0F, 20F, 0F);
		Shape4a.setTextureSize(32, 32);
		Shape4a.mirror = true;
		this.setRotation(Shape4a, 0F, 0F, 0.5235988F);
		Shape4b = new LODModelPart(this, 10, 14);
		Shape4b.addBox(-0.5F, -0.4F, -0.2F, 1, 6, 1);
		Shape4b.setRotationPoint(0F, 20F, 0F);
		Shape4b.setTextureSize(32, 32);
		Shape4b.mirror = true;
		this.setRotation(Shape4b, 0.5235988F, 0F, 0F);
		Shape4c = new LODModelPart(this, 15, 14);
		Shape4c.addBox(-0.2F, -0.4F, -0.5F, 1, 6, 1);
		Shape4c.setRotationPoint(0F, 20F, 0F);
		Shape4c.setTextureSize(32, 32);
		Shape4c.mirror = true;
		this.setRotation(Shape4c, 0F, 0F, -0.5235988F);
		Shape5 = new LODModelPart(this, 0, 8);
		Shape5.addBox(0F, 0F, 0F, 4, 1, 4);
		Shape5.setRotationPoint(-2F, 22F, -2F);
		Shape5.setTextureSize(32, 32);
		Shape5.mirror = true;
		this.setRotation(Shape5, 0F, 0F, 0F);
	}

	@Override
	public void renderAll(TileEntity te, ArrayList li) 	{
		Shape1.render(te, f5);
		Shape2.render(te, f5);
		Shape3.render(te, f5);
		Shape4.render(te, f5);
		Shape4a.render(te, f5);
		Shape4b.render(te, f5);
		Shape4c.render(te, f5);
		Shape5.render(te, f5);
	}

}
