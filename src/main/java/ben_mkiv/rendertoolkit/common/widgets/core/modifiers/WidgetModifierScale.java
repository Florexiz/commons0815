package ben_mkiv.rendertoolkit.common.widgets.core.modifiers;

import ben_mkiv.rendertoolkit.common.widgets.WidgetModifier;
import ben_mkiv.rendertoolkit.common.widgets.core.Easing;
import ben_mkiv.rendertoolkit.common.widgets.core.attribute.IEasing;
import io.netty.buffer.ByteBuf;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class WidgetModifierScale extends WidgetModifier implements IEasing {
	private float x=1, y=1, z=1;
	private float X, Y, Z;

	private ArrayList<ArrayList> easingListX, easingListY, easingListZ;

	@Override
	public void addEasing(String type, String typeIO, float duration, String list, float min, float max, String mode){
		switch(list.toLowerCase()) {
			case "x":
				this.easingListX = Easing.setEasing(Easing.EasingType.valueOf(type.toUpperCase()), Easing.EasingTypeIO.valueOf(typeIO.toUpperCase()), duration, min, max, Easing.EasingTypeMode.valueOf(mode.toUpperCase()));
				break;
			case "y":
				this.easingListY = Easing.setEasing(Easing.EasingType.valueOf(type.toUpperCase()), Easing.EasingTypeIO.valueOf(typeIO.toUpperCase()), duration, min, max, Easing.EasingTypeMode.valueOf(mode.toUpperCase()));
				break;
			case "z":
				this.easingListZ = Easing.setEasing(Easing.EasingType.valueOf(type.toUpperCase()), Easing.EasingTypeIO.valueOf(typeIO.toUpperCase()), duration, min, max, Easing.EasingTypeMode.valueOf(mode.toUpperCase()));
				break;
		}
	}

	@Override
	public void removeEasing(String list){
		switch(list.toLowerCase()) {
			case "x":
				this.easingListX = new ArrayList<ArrayList>();
				break;
			case "y":
				this.easingListY = new ArrayList<ArrayList>();
				break;
			case "z":
				this.easingListZ = new ArrayList<ArrayList>();
				break;
		}
	}

	public WidgetModifierScale(float x, float y, float z){
		this.easingListX = new ArrayList<ArrayList>();
		this.easingListY = new ArrayList<ArrayList>();
		this.easingListZ = new ArrayList<ArrayList>();

		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void update(float[] values){
		if(values.length < 3)
			return;

		this.x = values[0];
		this.y = values[1];
		this.z = values[2];

		this.applyEasings();
	}
		
	public void apply(long conditionStates){	
		if(!shouldApplyModifier(conditionStates)) return;

		this.applyEasings();

		GL11.glScalef(X, Y, Z);
	}

	private void applyEasings(){
		this.X = Easing.applyEasing(this.easingListX, this.x);
		this.Y = Easing.applyEasing(this.easingListY, this.y);
		this.Z = Easing.applyEasing(this.easingListZ, this.z);
	}

	public void revoke(long conditionStates) {
		if (!shouldApplyModifier(conditionStates)) return;

		if(X > 0) GL11.glScalef(1/X, 1, 1);
		if(Y > 0) GL11.glScalef(1, 1/Y, 1);
		if(Z > 0) GL11.glScalef(1, 1, 1/Z);
	}
	
	public void writeData(ByteBuf buff) {
		super.writeData(buff);
		buff.writeFloat(this.x);
		buff.writeFloat(this.y);
		buff.writeFloat(this.z);
		Easing.writeEasing(buff, this.easingListX);
		Easing.writeEasing(buff, this.easingListY);
		Easing.writeEasing(buff, this.easingListZ);
	}
	
	public void readData(ByteBuf buff) {
		super.readData(buff);
		this.x = buff.readFloat();
		this.y = buff.readFloat();
		this.z = buff.readFloat();
		this.easingListX = Easing.readEasing(buff);
		this.easingListY = Easing.readEasing(buff);
		this.easingListZ = Easing.readEasing(buff);
	}
	
	public WidgetModifierType getType(){
		return WidgetModifierType.SCALE;
	}
	
	public Object[] getValues(){
		this.applyEasings();
		return new Object[]{ this.X, this.Y, this.Z };
	}
}
