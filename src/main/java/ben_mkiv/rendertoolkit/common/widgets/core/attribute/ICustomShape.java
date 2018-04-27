package ben_mkiv.rendertoolkit.common.widgets.core.attribute;

public interface ICustomShape extends IAttribute{
    int getVertexCount();
    void setVertex(int n, float nx, float ny, float nz);
    void addVertex(float nx, float ny, float nz);
    void removeVertex(int n);
}
