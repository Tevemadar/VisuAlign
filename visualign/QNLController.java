package visualign;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import data.Marker;
import data.SegLabel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import nonlin.Triangle;
import parsers.JSON;

public class QNLController implements ChangeListener<Number> {
    @FXML
    private VBox vbox;

    @FXML
    private Pane pane;

    @FXML
    private Canvas imgcnv;

    @FXML
    private Canvas ovlycnv;

    @FXML
    private Slider opacitySlider;

    @FXML
    private ColorPicker outColor;

    @FXML
    private ColorPicker pinColor;

    @FXML
    private ColorPicker debColor;
    
    @FXML
    private RadioMenuItem debug;
    
    @FXML
    void onClick(MouseEvent event) {
        System.out.println(event);
    }

    int pick=-1;
    Marker picked;
    double pickx;
    double picky;
    void updatePick() {
        pick=-1;
        picked=null;
        pickx=(mouseX-imgx)*hwidth/imgw;
        picky=(mouseY-imgy)*hheight/imgh;
        double margin=hwidth*10/imgw;
        for(int i=markers.size()-1;i>=4;i--) {
            Marker m=markers.get(i);
            if(Math.abs(pickx-m.nx)<margin && Math.abs(picky-m.ny)<margin) {
                pick=i;
                picked=m;
                break;
            }
        }
    }
    
    double mouseX;
    double mouseY;
    @FXML
    void mouseMoved(MouseEvent event) {
        mouseX=event.getX();
        mouseY=event.getY();
    }

    double basex,basey;
    @FXML
    void mousePressed(MouseEvent event) {
        updatePick();
        if(picked!=null) {
            basex=picked.nx;
            basey=picked.ny;
        }
    }
    
    @FXML
    void mouseDragged(MouseEvent event) {
        mouseMoved(event);
        if(picked!=null) {
            picked.nx=basex+(mouseX-imgx)*hwidth/imgw-pickx;
            picked.ny=basey+(mouseY-imgy)*hheight/imgh-picky;
            triangulate();
            reDraw();
        }
    }
    
    List<Marker> markers=new ArrayList<Marker>();
    @FXML
    void keyTyped(KeyEvent event) {
        if(event.getCharacter().codePointAt(0)==8 || event.getCharacter().codePointAt(0)==127) {
            updatePick();
            if(pick>=0) {
                markers.remove(pick);
            }
        } else {
            double mx=(mouseX-imgx)*hwidth/imgw;
            double my=(mouseY-imgy)*hheight/imgh;
            boolean found=false;
            for(int i=0;i<triangles.size();i++) {
                double xy[]=triangles.get(i).transform(mx, my);
                if(xy!=null) {
                    markers.add(new Marker(xy[0],xy[1],mx,my));
                    found=true;
                    break;
                }
            }
            if(!found)throw new RuntimeException();
        }
        triangulate();
        reDraw();
    }
    
    @FXML
    void initialize() throws Exception {
        markers.add(new Marker(0,0));
        markers.add(new Marker(hwidth,0));
        markers.add(new Marker(0,hheight));
        markers.add(new Marker(hwidth, hheight));
        triangulate();
        
        pane.setFocusTraversable(true);
//        imgcnv.setFocusTraversable(true);
//        ovlycnv.setFocusTraversable(true);
        pane.widthProperty().addListener(this);
        pane.heightProperty().addListener(this);
        pinColor.setValue(Color.BLACK);
        outColor.setValue(Color.DARKBLUE);
        debColor.setValue(Color.GREY);
        palette = new ArrayList<SegLabel>();
        try (FileReader fr = new FileReader("Segmentation.json")) {
            JSON.mapList(JSON.parse(fr), palette, SegLabel.class, null);
        }
        fastpalette=new int[palette.size()];
        for(int i=0;i<fastpalette.length;i++) {
            SegLabel l=palette.get(i);
            fastpalette[i]=((int)l.red << 16)+((int)l.green << 8)+(int)l.blue;
        }
        try (DataInputStream dis=new DataInputStream(new BufferedInputStream(new FileInputStream("R601_s172_BDA_NR_10x-Segmentation.flat")))){
            byte bpp=dis.readByte();
            if(bpp!=1)throw new Exception("BPP: "+bpp);
            int ovlyw=dis.readInt();
            int ovlyh=dis.readInt();
            overlay=new int[ovlyh][ovlyw];
            for(int y=0;y<ovlyh;y++)
                for(int x=0;x<ovlyw;x++)
                    overlay[y][x]=dis.readByte();
        }
        image = new Image("file:R601_s172_BDA_NR_10x.png");
        
        opacitySlider.valueProperty().addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
        if(arg0==pane.widthProperty() || arg0==pane.heightProperty())drawImage();
        reDraw();
    }

    private Image image;
    int imgx, imgy, imgw, imgh;
    
    int overlay[][];
    List<SegLabel> palette;
    int fastpalette[];

    public void drawImage() {
        int cw = (int) imgcnv.getWidth();
        int ch = (int) imgcnv.getHeight();
        int iw = (int) image.getWidth();
        int ih = (int) image.getHeight();

        imgx = imgy = 0;
        if (cw * ih < ch * iw) {
            imgh = ih * cw / iw;
            imgw = cw;
            imgy = (ch - imgh) / 2;
        } else {
            imgw = iw * ch / ih;
            imgh = ch;
            imgx = (cw - imgw) / 2;
        }

        GraphicsContext ctx = imgcnv.getGraphicsContext2D();
        ctx.clearRect(0, 0, cw, ch);
        ctx.drawImage(image, imgx, imgy, imgw, imgh);

//        ctx.strokeRect(10, 10, cw - 20, ch - 20);
    }
    
    static final double hwidth=22500;
    static final double hheight=17500;
    List<Triangle> triangles;
    
    public void triangulate() {
        triangles=new ArrayList<Triangle>();
        triangles.add(new Triangle(0, 1, 2, markers));
        triangles.add(new Triangle(1, 2, 3, markers));
        for(int i=4;i<markers.size();i++) {
            Marker m=markers.get(i);
            double x=m.nx;
            double y=m.ny;
            boolean found=false;
            for(int t=0;t<triangles.size();t++) {
                Triangle tri=triangles.get(t);
                if(tri.intri(x, y)!=null) {
                    triangles.set(t, new Triangle(i,tri.a,tri.b,markers));
                    triangles.add(new Triangle(i,tri.a,tri.c,markers));
                    triangles.add(new Triangle(i,tri.b,tri.c,markers));
                    found=true;
                    break;
                }
            }
            if(!found)throw new RuntimeException();
            boolean flip;
            do {
                flip=false;
                for(int t=0;t<triangles.size() && !flip;t++) {
                    Triangle tri=triangles.get(t);
                    for(int j=0;j<markers.size() && !flip;j++)
                        if(j!=tri.a && j!=tri.b && j!=tri.c) {
                        Marker P=markers.get(j);
                        if(tri.incirc(P.nx,P.ny)) {
                            Triangle ta=new Triangle(j, tri.b, tri.c, markers);
                            int idx=triangles.indexOf(ta);
                            if(idx>=0) {
                                triangles.set(t, new Triangle(tri.a, tri.b, j, markers));
                                triangles.set(idx, new Triangle(tri.a, j, tri.c, markers));
                                flip=true;
                                break;
                            }
                            Triangle tb=new Triangle(tri.a, j, tri.c, markers);
                            idx=triangles.indexOf(tb);
                            if(idx>=0) {
                                triangles.set(t, new Triangle(tri.a, tri.b, j, markers));
                                triangles.set(idx, new Triangle(j, tri.b, tri.c, markers));
                                flip=true;
                                break;
                            }
                            Triangle tc=new Triangle(tri.a, tri.b, j, markers);
                            idx=triangles.indexOf(tc);
                            if(idx>=0) {
                                triangles.set(t, new Triangle(j, tri.b, tri.c, markers));
                                triangles.set(idx, new Triangle(tri.a, j, tri.c, markers));
                                flip=true;
                                break;
                            }
                        }
                    }
                }
            } while(flip);
        }
    }

    /*
     * Screen-space
     * x: 0...imgx-1
     * y: 0...imgy-1
     */
    public int sample(int x,int y) {
        double fx=x*hwidth/imgw;
        double fy=y*hheight/imgh;
        for(int i=0;i<triangles.size();i++) {
            double t[]=triangles.get(i).transform(fx, fy);
            if(t!=null) {
                int xx=(int)(t[0]*overlay[0].length/hwidth);
                int yy=(int)(t[1]*overlay.length/hheight);
//                if(xx<0 || yy<0 || xx>=overlay[0].length || yy>=overlay.length)
//                    return 0;
                return overlay[yy][xx];
            }
        }
        return 0;//0x80000000;
    }
    
    public void reDraw() {
        drawOvly();
        if(debug.isSelected())
            drawDebug();
        drawPins();
    }
    
    public void drawOvly() {
        GraphicsContext ctx = ovlycnv.getGraphicsContext2D();
        ctx.clearRect(0, 0, imgcnv.getWidth(), imgcnv.getHeight());
        PixelWriter pw = ctx.getPixelWriter();
        
        int op=(int)opacitySlider.getValue()*255/100;
        if(op<255) {
            int a= op <<24;
            IntStream.range(0, imgh).parallel().forEach(y->{
                for(int x=0;x<imgw;x++) {
                    int o=sample(x,y);
                    if(o!=0)
                        synchronized(pw) {
                        pw.setArgb(imgx+x, imgy+y, a+fastpalette[o]);}
                }
            });
        } else {
            Color c=outColor.getValue();
            int a=0xFF000000+((int)(c.getRed()*255) << 16)+((int)(c.getGreen()*255) << 8)+(int)(c.getBlue()*255);
//            for(int y=1;y<imgh;y++)
//                for(int x=1;x<imgw;x++) {
//                    int o=sample(x,y);
//                    if(o!=sample(x-1,y) || o!=sample(x,y-1))
//                        pw.setArgb(imgx+x, imgy+y, a);
//                }
            IntStream.range(0, imgh).parallel().forEach(y->{
                for(int x=1;x<imgw;x++) {
                    int o=sample(x,y);
                    if(o!=sample(x-1,y) || o!=sample(x,y-1))
                        synchronized(pw) {
                        pw.setArgb(imgx+x, imgy+y, a);}
                }
            });
        }
    }

    private void drawPins() {
        GraphicsContext ctx = ovlycnv.getGraphicsContext2D();
        ctx.setStroke(pinColor.getValue());
        for(int i=4;i<markers.size();i++) {
            Marker m=markers.get(i);
            double nx=m.nx*imgw/hwidth+imgx;
            double ny=m.ny*imgh/hheight+imgy;
            ctx.strokeLine(nx, ny-10, nx, ny+10);
            ctx.strokeLine(nx-10, ny, nx+10, ny);
            double ox=m.ox*imgw/hwidth+imgx;
            double oy=m.oy*imgh/hheight+imgy;
            ctx.strokeLine(ox, oy, nx, ny);
        }
    }

    private void drawDebug() {
        GraphicsContext ctx = ovlycnv.getGraphicsContext2D();
        ctx.setStroke(debColor.getValue());
        for(int i=0;i<triangles.size();i++) {
            Triangle t=triangles.get(i);
            double ax=imgx+imgw*t.A.nx/hwidth;
            double ay=imgy+imgh*t.A.ny/hheight;
            double bx=imgx+imgw*t.B.nx/hwidth;
            double by=imgy+imgh*t.B.ny/hheight;
            double cx=imgx+imgw*t.C.nx/hwidth;
            double cy=imgy+imgh*t.C.ny/hheight;
            ctx.strokeLine(ax, ay, bx, by);
            ctx.strokeLine(bx, by, cx, cy);
            ctx.strokeLine(cx, cy, ax, ay);
        }
    }
    
    @FXML
    void debug(ActionEvent event) {
        reDraw();
    }

    @FXML
    void exit(ActionEvent event) {

    }
    
    @FXML
    void open(ActionEvent event) {
        FileChooser fc=new FileChooser();
        fc.setTitle("Pick JSON file");
        fc.setSelectedExtensionFilter(new ExtensionFilter("JSON files", "json"));
        File f=fc.showOpenDialog(stage);
        if(f!=null) {
            
        }
    }

    @FXML
    void save(ActionEvent event) {

    }

    @FXML
    void saveas(ActionEvent event) {

    }

    @FXML
    void close(ActionEvent event) {

    }
    
    @FXML
    void rePin(ActionEvent event) {
        drawPins();
    }
    
    @FXML
    void reOvly(ActionEvent event) {
        reDraw();
    }
    
    @FXML
    void reDebug(ActionEvent event) {
        drawDebug();
        drawPins();
    }
    
    Stage stage;
}
