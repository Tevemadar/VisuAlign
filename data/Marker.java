package data;

public class Marker {
    public double ox,oy,nx,ny;
    public Marker(double ox,double oy,double nx,double ny) {
        this.ox=ox;
        this.oy=oy;
        this.nx=nx;
        this.ny=ny;
    }
    public Marker(double x,double y) {
        ox=nx=x;
        oy=ny=y;
    }
    
    @Override
    public String toString() {
        return String.format("(%f,%f -> %f,%f)", ox,oy,nx,ny);
    }
}
