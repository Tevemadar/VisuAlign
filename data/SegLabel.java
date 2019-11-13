package data;

public class SegLabel {
    public final int index;
    public final int red;
    public final int green;
    public final int blue;
    public final String name;
    public final int remap;
    public SegLabel(int index,int red,int green,int blue,String name,int remap) {
        this.index=index;
        this.red=red;
        this.green=green;
        this.blue=blue;
        this.name=name;
        this.remap=remap;
    }
}
