package math;

public class Matrix3 {
    final double m[][];

    public Matrix3() {
        m = new double[3][3];
    }

    public Matrix3(double a11, double a21, double a31, double a12, double a22, double a32, double a13, double a23,
            double a33) {
        m = new double[][] { { a11, a21, a31 }, { a12, a22, a32 }, { a13, a23, a33 } };
    }

    public Matrix3 inverse() {
        double det = m[0][0] * (m[1][1] * m[2][2] - m[2][1] * m[1][2])
                - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
        if (det == 0) {
            return null;
        }
        return new Matrix3((m[1][1] * m[2][2] - m[2][1] * m[1][2]) / det, (m[0][2] * m[2][1] - m[0][1] * m[2][2]) / det,
                (m[0][1] * m[1][2] - m[0][2] * m[1][1]) / det, (m[1][2] * m[2][0] - m[1][0] * m[2][2]) / det,
                (m[0][0] * m[2][2] - m[0][2] * m[2][0]) / det, (m[1][0] * m[0][2] - m[0][0] * m[1][2]) / det,
                (m[1][0] * m[2][1] - m[2][0] * m[1][1]) / det, (m[2][0] * m[0][1] - m[0][0] * m[2][1]) / det,
                (m[0][0] * m[1][1] - m[1][0] * m[0][1]) / det);
    }
    
    public double[] rowmul(double row[]) {
        var ret=new double[3];
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                ret[i]+=row[j]*m[j][i];
        return ret;
    }
    
//    public double x(double x,double y) {
//        return x*m[0][0]+y*m[1][0]+m[2][0];
//    }
//    public double y(double x,double y) {
//        return x*m[0][1]+y*m[1][1]+m[2][1];
//    }
}