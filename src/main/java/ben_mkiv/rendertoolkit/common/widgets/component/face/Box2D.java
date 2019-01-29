package ben_mkiv.rendertoolkit.common.widgets.component.face;

import ben_mkiv.rendertoolkit.common.widgets.IRenderableWidget;
import ben_mkiv.rendertoolkit.common.widgets.WidgetGLOverlay;
import ben_mkiv.rendertoolkit.common.widgets.WidgetType;
import ben_mkiv.rendertoolkit.common.widgets.core.attribute.IAutoTranslateable;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class Box2D extends WidgetGLOverlay implements IAutoTranslateable {
	public Box2D() {}

	@Override
	public void writeData(ByteBuf buff) {
		super.writeData(buff);
		writeDataSIZE(buff);		
	}

	@Override
	public void readData(ByteBuf buff) {
		super.readData(buff);
		readDataSIZE(buff);
	}
	
	@Override
	public WidgetType getType() {
		return WidgetType.BOX2D;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IRenderableWidget getRenderable() {
		return this.new RenderableBox2DWidget();
	}
	
	@SideOnly(Side.CLIENT)
	public class RenderableBox2DWidget extends RenderableGLWidget {
		@Override
		public void render(EntityPlayer player, Vec3d location, long conditionStates) {
			this.preRender(conditionStates);
			this.applyModifiers(conditionStates);
			this.applyAlignments();

			float[] col1 = this.getCurrentColorFloat(conditionStates, 1);
			float[] col2 = this.getCurrentColorFloat(conditionStates, 0);

			Tessellator tessellator = Tessellator.getInstance();
			createGradient(tessellator.getBuffer(), col1, col1, col2, col2);
			//drawUVMappedRect(tessellator.getBuffer(), 0, width, 0, height);
			tessellator.draw();
			this.postRender();
		}

		public void createGradient(BufferBuilder buff, float[] col1, float[] col2, float[] col3, float[] col4) {
			buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			buff.pos(0F, 0F, 0F).color(col1[0], col1[1], col1[2], col1[3]).endVertex();
			buff.pos(0F, height, 0F).color(col2[0], col2[1], col2[2], col2[3]).endVertex();
			buff.pos(width, height, 0F).color(col3[0], col3[1], col3[2], col3[3]).endVertex();
			buff.pos(width, 0F, 0F).color(col4[0], col4[1], col4[2], col4[3]).endVertex();
		}

		public void drawUVMappedRect(BufferBuilder buff, float minU, float maxU, float minV, float maxV) {
			buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buff.pos(0F, 0F, 0F).tex(minU, maxV).endVertex();
			buff.pos(0F, height, 0F).tex(maxU, maxV).endVertex();
			buff.pos(width, height, 0F).tex(maxU, minV).endVertex();
			buff.pos(width, 0F, 0F).tex(minU, minV).endVertex();
		}

		public void applyAlignments() {
			switch (this.getHorizontalAlign()) {
				case CENTER:
					GL11.glTranslatef((width / 2F), 0F, 0F);
					break;
				case LEFT:
					GL11.glTranslatef(-width, 0F, 0F);
					break;
			}

			switch (this.getVerticalAlign()) {
				case MIDDLE:
					GL11.glTranslatef(0F, (-height / 2F), 0F);
					break;
				case TOP:
					GL11.glTranslatef(0F, -height, 0F);
					break;
			}
		}

	}

}
