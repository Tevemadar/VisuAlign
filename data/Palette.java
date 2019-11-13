package data;

//import java.util.HashMap;
import java.util.Map;

public class Palette {
    public final Map<Integer, SegLabel> fullmap;
//    public final Map<Integer, Integer> fastmap=new HashMap<>();
    public final int fastcolors[];
    public Palette(Map<Integer, SegLabel> fullmap) {
        this.fullmap=fullmap;
        fastcolors=new int[fullmap.size()];
        fullmap.entrySet().forEach(entry -> {
            SegLabel l=entry.getValue();
            int idx=l.remap;
//            fastmap.put(entry.getKey(), idx);
            fastcolors[idx]=(l.red << 16)+(l.green << 8)+l.blue;
        });
    }
}
