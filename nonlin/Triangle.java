package nonlin;

import java.util.Arrays;
import java.util.List;

import math.Matrix3;

public class Triangle {
//    final List<Point> points;// = new ArrayList<>();
    public final int a, b, c;
    public final List<Double> A,B,C;
//    public final Point A,B,C;
//    public final double ax,ay,bx,by,cx,cy;
    public final double minx,miny,maxx,maxy;
    
    public final Matrix3 decomp;
    
    public final double den,Mdenx,Mdeny,r2den;
        
    public Triangle(int a,int b,int c,List<List<Double>> points) {
//        this.points=points;
        int p[]=new int[] {a,b,c};
        Arrays.sort(p);
        this.a=p[0];
        this.b=p[1];
        this.c=p[2];
        A=points.get(this.a);
        double ax=A.get(2);
        double ay=A.get(3);
        B=points.get(this.b);
        double bx=B.get(2);
        double by=B.get(3);
        C=points.get(this.c);
        double cx=C.get(2);
        double cy=C.get(3);
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
        return new double[] {A.get(0)+(B.get(0)-A.get(0))*uv1[0]+(C.get(0)-A.get(0))*uv1[1],A.get(1)+(B.get(1)-A.get(1))*uv1[0]+(C.get(1)-A.get(1))*uv1[1]};
    }
    
    public double[] intri(double x,double y) {
        if(x<minx || x>maxx || y<miny || y>maxy)return null;
        var uv1=decomp.rowmul(new double[] {x,y,1});
        if(uv1[0]<0 || uv1[0]>1 || uv1[1]<0 || uv1[1]>1 || uv1[0]+uv1[1]>1)return null;
        return uv1;
    }
    
//    public double[] transform(double x,double y) {
//        if(x<minx || x>maxx || y<miny || y>maxy)return null;
//        double a=decomp.x(x, y);
//        if(a<0 || a>1)return null;
//        double b=decomp.y(x, y);
//        if(b<0 || b>1 || a+b>1)return null;
//        return new double[] {A.get(0)+(B.get(0)-A.get(0))*a+(C.get(0)-A.get(0))*b,A.get(1)+(B.get(1)-A.get(1))*a+(C.get(1)-A.get(1))*b};
//    }
//    
//    public boolean intri(double x,double y) {
//        if(x<minx || x>maxx || y<miny || y>maxy)return false;
//        double a=decomp.x(x, y);
//        if(a<0 || a>1)return false;
//        double b=decomp.y(x, y);
//        if(b<0 || b>1 || a+b>1)return false;
//        return true;
//    }
    
//    @Override
//    public String toString() {
//        return a+"-"+b+"-"+c+" / "+points.get(a)+"-"+points.get(b)+"-"+points.get(c);
//    }
}