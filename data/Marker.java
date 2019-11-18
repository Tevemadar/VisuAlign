package data;

import java.util.ArrayList;
import java.util.Arrays;

public class Marker {
    private Marker() {}
//    public static ArrayList<Double> tuple(Double ...doubles){
//        return new ArrayList<Double>(Arrays.asList(doubles));
//    }
    public static ArrayList<Double> marker(double ox,double oy,double nx,double ny){
        return new ArrayList<>(Arrays.asList(ox,oy,nx,ny));
    }
    public static ArrayList<Double> marker(double x,double y){
        return new ArrayList<>(Arrays.asList(x,y,x,y));
    }
}
