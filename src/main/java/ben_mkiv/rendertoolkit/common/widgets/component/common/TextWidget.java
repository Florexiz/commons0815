package ben_mkiv.rendertoolkit.common.widgets.component.common;

import ben_mkiv.commons0815.font.FontLoader;
import ben_mkiv.commons0815.font.TrueTypeFont;
import ben_mkiv.commons0815.utils.utilsClient;
import ben_mkiv.rendertoolkit.common.widgets.WidgetGLWorld;
import ben_mkiv.rendertoolkit.common.widgets.core.attribute.IAlignable;
import ben_mkiv.rendertoolkit.common.widgets.core.attribute.ITextable;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public abstract class TextWidget extends WidgetGLWorld implements ITextable, IAlignable {
    String text, fontName;
    boolean antialias;
    float fontSize;
    public float stringWidth, stringHeight;
    public float offsetX, offsetY;
    static ArrayList<TrueTypeFont> fontRenderer = new ArrayList<>();

    public TextWidget(){
        text = "";
        fontName = "";
        fontSize = 12;
        antialias = false;
    }

    @SideOnly(Side.CLIENT)
    public void drawString(int currentColor){
        if(getFontName().length() == 0)
            utilsClient.fontRenderer().drawString(getText(), 0, 0, currentColor);
        else
            getFont(getFontName()).drawString(getText(), 0, 0, currentColor);
    }

    @SideOnly(Side.CLIENT)
    public void updateStringDimensions(){
        if(getFontName().length() == 0){
            FontRenderer fontRender = utilsClient.fontRenderer();
            stringWidth = fontRender.getStringWidth(this.getText());
            stringHeight = fontRender.FONT_HEIGHT;
        }
        else {
            stringWidth = getFont(getFontName()).getWidth(getText());
            stringHeight = getFont(getFontName()).getHeight();
        }
    }

    public void updateAlignments() {
        switch (halign) {
            case LEFT:
                offsetX = -stringWidth;
                break;
            case CENTER:
                offsetX = -stringWidth / 2F;
                break;
            case RIGHT:
                offsetX = 0;
                break;
        }

        switch (valign) {
            case TOP:
                offsetY = -stringHeight;
                break;
            case MIDDLE:
                offsetY = -stringHeight / 2F;
                break;
            case BOTTOM:
                offsetY = 0;
                break;
        }
    }

    @Override
    public void writeData(ByteBuf buff) {
        super.writeData(buff);
        ByteBufUtils.writeUTF8String(buff, text);
        ByteBufUtils.writeUTF8String(buff, fontName);
        buff.writeFloat(fontSize);
        buff.writeBoolean(antialias);
    }

    @Override
    public void readData(ByteBuf buff) {
        super.readData(buff);
        setText(ByteBufUtils.readUTF8String(buff));
        setFont(ByteBufUtils.readUTF8String(buff));
        setFontSize(buff.readFloat());
        setAntialias(buff.readBoolean());
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    public TrueTypeFont getFont(String fontName){
        for(TrueTypeFont ttf : fontRenderer) {
            if(ttf.getFontName().equalsIgnoreCase(fontName)
                    && ttf.getFontSize() == fontSize
                    && ttf.getAntialias() == antialias)
                return ttf;
        }

        fontRenderer.add(FontLoader.loadSystemFont(fontName, fontSize, antialias));

        return getFont(fontName);
    }

    @Override
    public void setFont(String fontName) {
        this.fontName = fontName;
    }

    @Override
    public void setFontSize(float size) {
        this.fontSize = size;
    }

    public String getFontName(){
        return this.fontName;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setAntialias(boolean state){
        this.antialias = state;
    }

}