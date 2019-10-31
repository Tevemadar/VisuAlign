package nonlin;

import java.util.Arrays;
import java.util.List;

import data.Marker;
import math.Matrix3;

public class Triangle {
//    final List<Point> points;// = new ArrayList<>();
    public final int a, b, c;
    public final Marker A,B,C;
//    public final Point A,B,C;
//    public final double ax,ay,bx,by,cx,cy;
    public final double minx,miny,maxx,maxy;
    
    public final Matrix3 decomp;
    
    public final double den,Mdenx,Mdeny,r2den;
        
    public Triangle(int a,int b,int c,List<Marker> points) {
//        this.points=points;
        int p[]=new int[] {a,b,c};
        Arrays.sort(p);
        this.a=p[0];
        this.b=p[1];
        this.c=p[2];
        A=points.get(this.a);
        double ax=A.nx;
        double ay=A.ny;
        B=points.get(this.b);
        double bx=B.nx;
        double by=B.ny;
        C=points.get(this.c);
        double cx=C.nx;
        double cy=C.ny;
        minx=Math.min(ax,Math.min(bx, cx));
        miny=Math.min(ay,Math.min(by, cy));
        maxx=Math.max(ax,Math.max(bx, cx));
        maxy=Math.max(ay,Math.max(by, cy));
        decomp=new Matrix3(
                bx-ax,by-ay,0,
                cx-ax,cy-ay,0,
                ax,ay,1
                ).inverse();
        
        double a2=d2(bx,by,cx,cy);
        double b2=d2(ax,ay,cx,cy);
        double c2=d2(ax,ay,bx,by);
        double fa=a2*(b2+c2-a2);
        double fb=b2*(c2+a2-b2);
        double fc=c2*(a2+b2-c2);
        den=fa+fb+fc;
        Mdenx=fa*ax+fb*bx+fc*cx;
        Mdeny=fa*ay+fb*by+fc*cy;
        r2den=d2(ax*den,ay*den,Mdenx,Mdeny);
     }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Triangle) {
            Triangle t=(Triangle)obj;
            return t.a==a && t.b==b && t.c==c;// && t.points==points;
        }
        return false;
    }
    
    public static final double d2(double ax,double ay,double bx,double by) {
        return (ax-bx)*(ax-bx)+(ay-by)*(ay-by);
    }
    
    public boolean incirc(double x,double y) {
//        if(D==A || D==B || D==C)
//            return false;
        return d2(x*den,y*den,Mdenx,Mdeny)<r2den;
    }
    
    public double[] transform(double x,double y) {
        var uv1=intri(x,y);
        if(uv1==null)return null;
        return new double[] {A.ox+(B.ox-A.ox)*uv1[0]+(C.ox-A.ox)*uv1[1],A.oy+(B.oy-A.oy)*uv1[0]+(C.oy-A.oy)*uv1[1]};
    }
    
    public double[] intri(double x,double y) {
        if(x<minx || x>maxx || y<miny || y>maxy)return null;
        var uv1=decomp.rowmul(new double[] {x,y,1});
        if(uv1[0]<0 || uv1[0]>1 || uv1[1]<0 || uv1[1]>1 || uv1[0]+uv1[1]>1)return null;
        return uv1;
    }
    
//    @Override
//    public String toString() {
//        return a+"-"+b+"-"+c+" / "+points.get(a)+"-"+points.get(b)+"-"+points.get(c);
//    }
}