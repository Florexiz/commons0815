package ben_mkiv.rendertoolkit.common.widgets;

import ben_mkiv.commons0815.utils.PlayerStats;
import ben_mkiv.commons0815.utils.utilsCommon;
import ben_mkiv.rendertoolkit.common.widgets.component.common.ItemIcon;
import ben_mkiv.rendertoolkit.common.widgets.component.face.Box2D;
import ben_mkiv.rendertoolkit.common.widgets.component.face.Text2D;
import ben_mkiv.rendertoolkit.common.widgets.component.world.Text3D;
import ben_mkiv.commons0815.utils.ClientUtils;
import ben_mkiv.rendertoolkit.renderToolkit;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import ben_mkiv.rendertoolkit.common.widgets.core.attribute.IResizable;
import ben_mkiv.rendertoolkit.common.widgets.core.attribute.IPrivate;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.util.math.RayTraceResult;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

public abstract class WidgetGLOverlay extends Widget implements IResizable, IPrivate {
	public RenderType rendertype;

	private float x = 0, y = 0, z = 0;
	private Vec3d margin = new Vec3d(0, 0, 0);

	public float width = 1, height = 1; //default to one, so that stuff appears even without setting size

	public enum VAlignment{	TOP, MIDDLE, BOTTOM }
	public enum HAlignment{	LEFT, CENTER, RIGHT }

	public VAlignment valign;
	public HAlignment halign;

	public boolean isThroughVisibility = false;
	public boolean isLookingAtEnable = false;
	public boolean faceWidgetToPlayer = false;

	public Vec3d lookAt = new Vec3d(0, 0, 0);
	
	public int viewDistance = 64;

	public long age = 0;

	public WidgetGLOverlay(){
		this.valign = VAlignment.TOP;
		this.halign = HAlignment.LEFT;
		this.rendertype = RenderType.GameOverlayLocated;
	}
	
	public void writeData(ByteBuf buff) {
		WidgetModifierList.writeData(buff);
		buff.writeInt(viewDistance);
		buff.writeDouble(lookAt.x);
		buff.writeDouble(lookAt.y);
		buff.writeDouble(lookAt.z);
		buff.writeFloat(x);
		buff.writeFloat(y);
		buff.writeFloat(z);
		buff.writeInt(valign.ordinal());
		buff.writeInt(halign.ordinal());
		buff.writeBoolean(isLookingAtEnable);
	}
	
	public void readData(ByteBuf buff) {
		WidgetModifierList.readData(buff);
		viewDistance = buff.readInt();
		lookAt = new Vec3d(buff.readDouble(), buff.readDouble(), buff.readDouble());
		x = buff.readFloat();
		y = buff.readFloat();
		z = buff.readFloat();
		valign = VAlignment.values()[buff.readInt()];
		halign = HAlignment.values()[buff.readInt()];
		isLookingAtEnable = buff.readBoolean();
	}

	public void writeDataSIZE(ByteBuf buff) {
		buff.writeFloat(this.width);
		buff.writeFloat(this.height);
	}
	
	public void readDataSIZE(ByteBuf buff) {
		this.width = buff.readFloat();
		this.height = buff.readFloat();
	}
	
	public void setSize(double w, double h) {
		this.width = (float) w;
		this.height = (float) h;
	}	
	
	public double getWidth() {
		return this.width; }

	public double getHeight() {
		return this.height; }
	
	public int getDistanceView() {
		return viewDistance; }

	public void setDistanceView(int distance) {
		this.viewDistance = distance; }

	public void setLookingAt(double x, double y, double z) {
		lookAt = new Vec3d(x, y, z); }

	public boolean isLookingAtEnable() {
		return isLookingAtEnable; }

	public void setLookingAtEnable(boolean enable) {
		isLookingAtEnable = enable; }
		
	public void setFaceWidgetToPlayer(boolean enable) {
		faceWidgetToPlayer = enable; }

	public double getLookingAtX() {
		return lookAt.x; }

	public double getLookingAtY() {
		return lookAt.y; }

	public double getLookingAtZ() {
		return lookAt.z; }

	public void setVerticalAlignment(String align){
		this.valign = VAlignment.valueOf(align.toUpperCase());
	}

	public void setHorizontalAlignment(String align){
		this.halign = HAlignment.valueOf(align.toUpperCase());
	}

	public Vec3d getRenderPosition(String playerName, Vec3d offset, long conditions){
		PlayerStats s = renderToolkit.proxy.getPlayerStats(playerName);
		offset.add(this.WidgetModifierList.getRenderPosition(conditions, offset, s.screenWidth, s.screenHeight, 0));
		return new Vec3d(offset.x+margin.x, offset.y+margin.y, offset.z+margin.z);
	}

	@SideOnly(Side.CLIENT)
	public class RenderableGLWidget implements IRenderableWidget {
		boolean depthtest, texture2d, blending, smoothshading, alpha;
		boolean doBlending, doTexture, doSmoothShade, doAlpha;
		boolean renderPositionSet = false;

		@Override
		public void render(EntityPlayer player, Vec3d renderOffset, long conditionStates) {}

		public void setRenderFlags() {
			depthtest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
			texture2d = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
			blending = GL11.glIsEnabled(GL11.GL_BLEND);

			smoothshading = false;
			doBlending = false;
			doTexture = false;
			doSmoothShade = false;

			if(GL11.glGetInteger(GL11.GL_SHADE_MODEL) == GL11.GL_SMOOTH)
				smoothshading = true;

			if(isThroughVisibility)
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			else
				GL11.glEnable(GL11.GL_DEPTH_TEST);

			GL11.glDisable(GL11.GL_LIGHTING);

			for(int i=0, count = WidgetModifierList.modifiers.size(); i < count; i++){
				switch(WidgetModifierList.modifiers.get(i).getType()){
					case COLOR: if((float) WidgetModifierList.modifiers.get(i).getValues()[3] < 1) doBlending = true; break;
					case TEXTURE: doTexture = true; break;
					default: break;
				}
			}

			Class type = getClass();
			if(type.equals(Box2D.RenderableBox2DWidget.class)){
				doSmoothShade = true;
				doBlending = true;
				doAlpha = true;
				//doTexture = false;
			}
			else if(type.equals(Text2D.RenderText.class) || type.equals(Text3D.RenderableFloatingText.class)){
				doTexture = true;
				doBlending = true;
				GL11.glDisable(GL11.GL_ALPHA_TEST);
			}
			else if(type.equals(ItemIcon.RenderableItemIcon.class)){
				doBlending = true;
				doTexture = true;
			}

			if(doTexture)
				GL11.glEnable(GL11.GL_TEXTURE_2D);
			else
				GL11.glDisable(GL11.GL_TEXTURE_2D);

			if(doBlending){
				GL11.glEnable(GL11.GL_BLEND);		//vertex based alpha
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
			else {
				GL11.glDisable(GL11.GL_BLEND);
			}
			if(doSmoothShade)
				GL11.glShadeModel(GL11.GL_SMOOTH);
			else
				GL11.glShadeModel(GL11.GL_FLAT);
		}


		public int preRender(long conditionStates, Vec3d renderOffset){
			age++;
			this.setRenderFlags();
			if(age % 50 == 0){
				updateRenderPosition(conditionStates, renderOffset);
				age = 1;
			}
			return WidgetModifierList.getCurrentColor(conditionStates, 0);
		}

		public void updateRenderPosition(long conditionStates, Vec3d renderOffset){
			Vec3d renderOrigin = renderOffset;
			Vec3d renderPosition = WidgetModifierList.getRenderPosition(conditionStates, renderOrigin, renderToolkit.ClientSurface.resolution.getScaledWidth(), renderToolkit.ClientSurface.resolution.getScaledHeight(), 1);
			x = (float) (renderPosition.x + margin.x);
			y = (float) (renderPosition.y + margin.y);
			z = (float) (renderPosition.z + margin.z);
			renderPositionSet = true;
		}

		public int applyModifiers(long conditionStates){
			WidgetModifierList.apply(conditionStates);

			return WidgetModifierList.getCurrentColor(WidgetModifierList.lastConditionStates, 0);
		}

		public void addPlayerRotation(EntityPlayer player){
			if(!faceWidgetToPlayer) return;
			if(player == null) player = renderToolkit.proxy.getPlayer("");
			GL11.glRotated(player.rotationYaw,0.0D,1.0D,0.0D);
			GL11.glRotated(-player.rotationPitch,1.0D,0.0D,0.0D);
		}

		public void removePlayerRotation(EntityPlayer player){
			if(!faceWidgetToPlayer) return;
			if(player == null) player = renderToolkit.proxy.getPlayer("");
			GL11.glRotated(player.rotationPitch,1.0D,0.0D,0.0D);
			GL11.glRotated(-player.rotationYaw,0.0D,1.0D,0.0D);
		}

		public void addPlayerRotation(EntityPlayer player, Vec3d lookingAtVector){
			if(!faceWidgetToPlayer) return;

			GL11.glRotated(player.rotationYaw,0.0D,1.0D,0.0D);

			if(lookingAtVector.y > (player.getPositionVector().y + 1.8F))
				GL11.glRotated(-player.rotationPitch,1.0D,0.0D,0.0D);
			else
				GL11.glRotated(player.rotationPitch,1.0D,0.0D,0.0D);
		}

		public void removePlayerRotation(EntityPlayer player, Vec3d lookingAtVector){
			if(!faceWidgetToPlayer) return;

			if(lookingAtVector.y > (player.getPositionVector().y + 1.8F))
				GL11.glRotated(player.rotationPitch,1.0D,0.0D,0.0D);
			else
				GL11.glRotated(-player.rotationPitch,1.0D,0.0D,0.0D);

			GL11.glRotated(-player.rotationYaw,0.0D,1.0D,0.0D);
		}

		public void applyModifierList(long conditionStates, ArrayList<WidgetModifier.WidgetModifierType> modifierTypes){
			for(WidgetModifier m : WidgetModifierList.modifiers){
				for(WidgetModifier.WidgetModifierType t : modifierTypes){
					if(t.equals(m.getType())){
						m.apply(conditionStates);
					}
				}
			}
		}

		public void revokeModifierList(ArrayList<WidgetModifier.WidgetModifierType> modifierTypes){
			WidgetModifierList.revoke(WidgetModifierList.lastConditionStates, modifierTypes);
		}

		public void revokeModifiers() {
			WidgetModifierList.revoke(WidgetModifierList.lastConditionStates);
		}

		public float[] getCurrentColorFloat(long conditionStates, int index){
			return WidgetModifierList.getCurrentColorFloat(conditionStates, index);
		}

		public void postRender(){
			revokeModifiers();
			revokeRenderFlags();
		}

		public void revokeRenderFlags(){
			if(depthtest) 
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			else
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			if(blending) 
				GL11.glEnable(GL11.GL_BLEND);
			else
				GL11.glDisable(GL11.GL_BLEND);
				
			if(texture2d) 
				GL11.glEnable(GL11.GL_TEXTURE_2D);
			else
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				
			if(smoothshading)
				GL11.glShadeModel(GL11.GL_SMOOTH);
			else 
				GL11.glShadeModel(GL11.GL_FLAT);
		}
				
		@Override
		public boolean shouldWidgetBeRendered(EntityPlayer player) {
			if(getRenderType().equals(RenderType.WorldLocated))
				if (renderPositionSet && !utilsCommon.inRange(player, x, y, z, viewDistance))
					return false;

			RayTraceResult pos = ClientUtils.getBlockCoordsLookingAt(player);
			if(isLookingAtEnable && (pos == null || pos.getBlockPos().getX() != lookAt.x || pos.getBlockPos().getY() != lookAt.y || pos.getBlockPos().getZ() != lookAt.z) )
				return false;		
					
			return isVisible();
		}

		@Override
		public UUID getWidgetOwner() {
			return getOwnerUUID();
		}

		@Override
		public boolean isWidgetOwner(String uuid){
			if(getOwnerUUID() == null)
				return true;

			if(uuid.equals(getOwnerUUID().toString()))
				return true;

			return false;
		}

		@Override
		public RenderType getRenderType() {
			return rendertype;
		}

		public VAlignment getVerticalAlign(){
			return valign;
		}

		public HAlignment getHorizontalAlign(){
			return halign;
		}
	}
}
