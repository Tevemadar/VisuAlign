package data;

import java.util.ArrayList;
import java.util.List;

import math.Estimator;
import nonlin.Triangle;
import static data.Marker.marker;

public class Slice implements Comparable<Slice> {
    public String filename;
    public double nr;
    public double width;
    public double height;
    public List<Double> anchoring = new ArrayList<>();
    public List<ArrayList<Double>> markers = new ArrayList<>();
    
    public String toString() {
        return "\n{\"filename\":\""+filename+"\",\"nr\":"+(int)nr+",\"width\":"+(int)width+",\"height\":"+(int)height
                +(noanchoring?"":",\"anchoring\":"+anchoring)+(markers.size()>0?",\"markers\":"+markers:"")+"}";
    }
    
    public List<List<Double>> trimarkers;
    public List<Triangle> triangles;
    
    public void triangulate() {
//        long start=System.currentTimeMillis();
        trimarkers=new ArrayList<>();
        trimarkers.add(marker(0,0));
        trimarkers.add(marker(width, 0));
        trimarkers.add(marker(0, height));
        trimarkers.add(marker(width, height));
        triangles = new ArrayList<Triangle>();
        triangles.add(new Triangle(0, 1, 2, trimarkers));
        triangles.add(new Triangle(1, 2, 3, trimarkers));
//        for (int i = 0; i < markers.size() && System.currentTimeMillis()<start+5000; i++) {
        for (int i = 0; i < markers.size(); i++) {
            List<Double> m = markers.get(i);
            trimarkers.add(m);
            double x = m.get(2);
            double y = m.get(3);
//            boolean found = false;
            for (int t = 0; t < triangles.size(); t++) {
                Triangle tri = triangles.get(t);
//                if (tri.intri(x, y) != null) {
                if (tri.intri(x, y)!=null) {
                    triangles.set(t, new Triangle(i+4, tri.a, tri.b, trimarkers));
                    triangles.add(new Triangle(i+4, tri.a, tri.c, trimarkers));
                    triangles.add(new Triangle(i+4, tri.b, tri.c, trimarkers));
//                    found = true;
                    break;
                }
            }
//            if (!found)
//                throw new RuntimeException();
            boolean flip;
            do {
                flip = false;
                for (int t = 0; t < triangles.size() && !flip; t++) {
                    Triangle tri = triangles.get(t);
                    for (int j = 0; j < trimarkers.size() && !flip; j++)
                        if (j != tri.a && j != tri.b && j != tri.c) {
                            List<Double> P = trimarkers.get(j);
                            if (tri.incirc(P.get(2), P.get(3))) {
                                Triangle ta = new Triangle(j, tri.b, tri.c, trimarkers);
                                int idx = triangles.indexOf(ta);
                                if (idx >= 0) {
                                    triangles.set(t, new Triangle(tri.a, tri.b, j, trimarkers));
                                    triangles.set(idx, new Triangle(tri.a, j, tri.c, trimarkers));
                                    flip = true;
                                    break;
                                }
                                Triangle tb = new Triangle(tri.a, j, tri.c, trimarkers);
                                idx = triangles.indexOf(tb);
                                if (idx >= 0) {
                                    triangles.set(t, new Triangle(tri.a, tri.b, j, trimarkers));
                                    triangles.set(idx, new Triangle(j, tri.b, tri.c, trimarkers));
                                    flip = true;
                                    break;
                                }
                                Triangle tc = new Triangle(tri.a, tri.b, j, trimarkers);
                                idx = triangles.indexOf(tc);
                                if (idx >= 0) {
                                    triangles.set(t, new Triangle(j, tri.b, tri.c, trimarkers));
                                    triangles.set(idx, new Triangle(tri.a, j, tri.c, trimarkers));
                                    flip = true;
                                    break;
                                }
                            }
                        }
                }
//            } while (flip && System.currentTimeMillis()<start+5000);
            } while (flip);
        }
    }
    
//    public void triangulate() {
//        trimarkers=new ArrayList<>();
//        trimarkers.add(marker(0,0));
//        trimarkers.add(marker(width, 0));
//        trimarkers.add(marker(0, height));
//        trimarkers.add(marker(width, height));
//        triangles = new ArrayList<Triangle>();
//        triangles.add(new Triangle(0, 1, 2, trimarkers));
//        triangles.add(new Triangle(1, 2, 3, trimarkers));
//        for (int i = 0; i < markers.size(); i++) {
//            List<Double> m = markers.get(i);
//            trimarkers.add(m);
//            double x = m.get(2);
//            double y = m.get(3);
//            boolean found = false;
//            for (int t = 0; t < triangles.size(); t++) {
//                Triangle tri = triangles.get(t);
//                if (tri.intri(x, y) != null) {
//                    triangles.set(t, new Triangle(i+4, tri.a, tri.b, trimarkers));
//                    triangles.add(new Triangle(i+4, tri.a, tri.c, trimarkers));
//                    triangles.add(new Triangle(i+4, tri.b, tri.c, trimarkers));
//                    found = true;
//                    break;
//                }
//            }
////            if (!found)
////                throw new RuntimeException();
////            if(found) {
////                
////            }
////            boolean flip;
////            do {
////                flip = false;
////                for (int t = 0; t < triangles.size() && !flip; t++) {
////                    Triangle tri = triangles.get(t);
////                    for (int j = 0; j < trimarkers.size() && !flip; j++)
////                        if (j != tri.a && j != tri.b && j != tri.c) {
////                            List<Double> P = trimarkers.get(j);
////                            if (tri.incirc(P.get(2), P.get(3))) {
////                                Triangle ta = new Triangle(j, tri.b, tri.c, trimarkers);
////                                int idx = triangles.indexOf(ta);
////                                if (idx >= 0) {
////                                    triangles.set(t, new Triangle(tri.a, tri.b, j, trimarkers));
////                                    triangles.set(idx, new Triangle(tri.a, j, tri.c, trimarkers));
////                                    flip = true;
////                                    break;
////                                }
////                                Triangle tb = new Triangle(tri.a, j, tri.c, trimarkers);
////                                idx = triangles.indexOf(tb);
////                                if (idx >= 0) {
////                                    triangles.set(t, new Triangle(tri.a, tri.b, j, trimarkers));
////                                    triangles.set(idx, new Triangle(j, tri.b, tri.c, trimarkers));
////                                    flip = true;
////                                    break;
////                                }
////                                Triangle tc = new Triangle(tri.a, tri.b, j, trimarkers);
////                                idx = triangles.indexOf(tc);
////                                if (idx >= 0) {
////                                    triangles.set(t, new Triangle(j, tri.b, tri.c, trimarkers));
////                                    triangles.set(idx, new Triangle(tri.a, j, tri.c, trimarkers));
////                                    flip = true;
////                                    break;
////                                }
////                            }
////                        }
////                }
////            } while (flip && System.currentTimeMillis()<start+5000);
//        }
//    }

    @Override
    public int compareTo(Slice s) {
        return (int) (nr - s.nr);
    }

    private double normalize(int idx) {
        double len = 0;
        for (int i = 0; i < 3; i++)
            len += anchoring.get(idx + i) * anchoring.get(idx + i);
        len = Math.sqrt(len);
        for (int i = 0; i < 3; i++)
            anchoring.set(idx + i, anchoring.get(idx + i) / len);
        return len;
    }

    private void orthonormalize() {
        normalize(3);
        double dot = 0;
        for (int i = 0; i < 3; i++)
            dot += anchoring.get(i + 3) * anchoring.get(i + 6);
        for (int i = 0; i < 3; i++)
            anchoring.set(i + 6, anchoring.get(i + 6) - anchoring.get(i + 3) * dot);
        normalize(6);
    }

    boolean decompose() {
        if (anchoring.size() == 9) {
            for (int i = 0; i < 3; i++)
                anchoring.set(i, anchoring.get(i) + (anchoring.get(i + 3) + anchoring.get(i + 6)) / 2);
            anchoring.add(normalize(3) / width);
            anchoring.add(normalize(6) / height);
            return true;
        }
        return false;
    }

    boolean recompose() {
        if (anchoring.size() == 11) {
            orthonormalize();
            final double wr = anchoring.get(9);
            final double hr = anchoring.get(10);
            for (int i = 0; i < 3; i++) {
                anchoring.set(i + 3, anchoring.get(i + 3) * wr * width);
                anchoring.set(i + 6, anchoring.get(i + 6) * hr * height);
                anchoring.set(i, anchoring.get(i) - (anchoring.get(i + 3) + anchoring.get(i + 6)) / 2);
            }
            anchoring.remove(10);
            anchoring.remove(9);
            return true;
        }
        return false;
    }

    public boolean noanchoring;
    public boolean from(Estimator ests[]) {
        if (anchoring.size() == 0) {
            noanchoring=true;
            for (int i = 0; i < ests.length; i++)
                anchoring.add(ests[i].get(nr));
            orthonormalize();
            return true;
        }
        return false;
    }
}
