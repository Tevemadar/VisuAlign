package visualign;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import data.Marker;
import data.Palette;
import data.SegLabel;
import data.Series;
import data.Slice;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
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
import parsers.ITKLabel;
import parsers.JSON;
import slicer.Int32Slices;

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
    private Spinner<Integer> spn;
    
    @FXML
    private IntegerSpinnerValueFactory spnVal;

    @FXML
    void onClick(MouseEvent event) {
        System.out.println(event);
    }

    int pick = -1;
    Marker picked;
    double pickx;
    double picky;

    void updatePick() {
        List<Marker> markers=slice.markers;
        pick = -1;
        picked = null;
        pickx = (mouseX - imgx) * slice.width / imgw;
        picky = (mouseY - imgy) * slice.height / imgh;
        double margin = slice.width * 10 / imgw;
        for (int i = markers.size() - 1; i >= 0; i--) {
            Marker m = markers.get(i);
            if (Math.abs(pickx - m.nx) < margin && Math.abs(picky - m.ny) < margin) {
                pick = i;
                picked = m;
                break;
            }
        }
    }

    double mouseX;
    double mouseY;

    @FXML
    void mouseMoved(MouseEvent event) {
        ovlycnv.requestFocus();
        mouseX = event.getX();
        mouseY = event.getY();
    }

    double basex, basey;

    @FXML
    void mousePressed(MouseEvent event) {
        updatePick();
        if (picked != null) {
            basex = picked.nx;
            basey = picked.ny;
        }
    }

    @FXML
    void mouseDragged(MouseEvent event) {
        mouseMoved(event);
        if (picked != null) {
            picked.nx = basex + (mouseX - imgx) * slice.width / imgw - pickx;
            picked.ny = basey + (mouseY - imgy) * slice.height / imgh - picky;
            slice.triangulate();
            reDraw();
        }
    }

//    List<Marker> markers = new ArrayList<Marker>();

    @FXML
    void keyTyped(KeyEvent event) {
        if(slice==null)return;
        
        List<Marker> markers=slice.markers;
        List<Triangle> triangles=slice.triangles;

        updatePick();
        if (event.getCharacter().codePointAt(0) == 8 || event.getCharacter().codePointAt(0) == 127) {
//            updatePick();
            if (pick >= 0) {
                markers.remove(pick);
            }
        } else {
            if (pick >= 0) return;
            double mx = (mouseX - imgx) * slice.width / imgw;
            double my = (mouseY - imgy) * slice.height / imgh;
            boolean found = false;
            for (int i = 0; i < triangles.size(); i++) {
                double xy[] = triangles.get(i).transform(mx, my);
                if (xy != null) {
                    markers.add(new Marker(xy[0], xy[1], mx, my));
                    found = true;
                    break;
                }
            }
//            if (!found)
//                throw new RuntimeException();
        }
        slice.triangulate();
        reDraw();
    }

    @FXML
    void initialize() throws Exception {
//        markers.add(new Marker(0, 0));
//        markers.add(new Marker(slice.width, 0));
//        markers.add(new Marker(0, slice.height));
//        markers.add(new Marker(slice.width, slice.height));
//        triangulate();

//        pane.setFocusTraversable(true);
//        imgcnv.setFocusTraversable(true);
//        ovlycnv.setFocusTraversable(true);
        pane.widthProperty().addListener(this);
        pane.heightProperty().addListener(this);
        pinColor.setValue(Color.BLACK);
        outColor.setValue(Color.DARKBLUE);
        debColor.setValue(Color.GREY);
//        palette = new ArrayList<SegLabel>();
//        try (FileReader fr = new FileReader("Segmentation.json")) {
//            JSON.mapList(JSON.parse(fr), palette, SegLabel.class, null);
//        }
//        fastpalette=new int[palette.size()];
//        for(int i=0;i<fastpalette.length;i++) {
//            SegLabel l=palette.get(i);
//            fastpalette[i]=((int)l.red << 16)+((int)l.green << 8)+(int)l.blue;
//        }
//        try (DataInputStream dis=new DataInputStream(new BufferedInputStream(new FileInputStream("R601_s172_BDA_NR_10x-Segmentation.flat")))){
//            byte bpp=dis.readByte();
//            if(bpp!=1)throw new Exception("BPP: "+bpp);
//            int ovlyw=dis.readInt();
//            int ovlyh=dis.readInt();
//            overlay=new int[ovlyh][ovlyw];
//            for(int y=0;y<ovlyh;y++)
//                for(int x=0;x<ovlyw;x++)
//                    overlay[y][x]=dis.readByte();
//        }
//        image = new Image("file:R601_s172_BDA_NR_10x.png");
//        
        opacitySlider.valueProperty().addListener(this);
        spnVal.valueProperty().addListener(this);
    }

    Series series;
    Slice slice;

    @Override
    public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
        if(arg0==spnVal.valueProperty()) {
            loadView();
            return;
        }
        if (arg0 == pane.widthProperty() || arg0 == pane.heightProperty())
            drawImage();
        reDraw();
    }

    private Image image;
    int imgx, imgy, imgw, imgh;

    int x_overlay[][];
    int fastoverlay[];

    public void drawImage() {
        if (slice == null)
            return; // !!

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

//    double slice.width;
//    double slice.height;
//    List<Triangle> triangles;

//    public void triangulate() {
//        triangles = new ArrayList<Triangle>();
//        triangles.add(new Triangle(0, 1, 2, markers));
//        triangles.add(new Triangle(1, 2, 3, markers));
//        for (int i = 4; i < markers.size(); i++) {
//            Marker m = markers.get(i);
//            double x = m.nx;
//            double y = m.ny;
//            boolean found = false;
//            for (int t = 0; t < triangles.size(); t++) {
//                Triangle tri = triangles.get(t);
//                if (tri.intri(x, y) != null) {
//                    triangles.set(t, new Triangle(i, tri.a, tri.b, markers));
//                    triangles.add(new Triangle(i, tri.a, tri.c, markers));
//                    triangles.add(new Triangle(i, tri.b, tri.c, markers));
//                    found = true;
//                    break;
//                }
//            }
//            if (!found)
//                throw new RuntimeException();
//            boolean flip;
//            do {
//                flip = false;
//                for (int t = 0; t < triangles.size() && !flip; t++) {
//                    Triangle tri = triangles.get(t);
//                    for (int j = 0; j < markers.size() && !flip; j++)
//                        if (j != tri.a && j != tri.b && j != tri.c) {
//                            Marker P = markers.get(j);
//                            if (tri.incirc(P.nx, P.ny)) {
//                                Triangle ta = new Triangle(j, tri.b, tri.c, markers);
//                                int idx = triangles.indexOf(ta);
//                                if (idx >= 0) {
//                                    triangles.set(t, new Triangle(tri.a, tri.b, j, markers));
//                                    triangles.set(idx, new Triangle(tri.a, j, tri.c, markers));
//                                    flip = true;
//                                    break;
//                                }
//                                Triangle tb = new Triangle(tri.a, j, tri.c, markers);
//                                idx = triangles.indexOf(tb);
//                                if (idx >= 0) {
//                                    triangles.set(t, new Triangle(tri.a, tri.b, j, markers));
//                                    triangles.set(idx, new Triangle(j, tri.b, tri.c, markers));
//                                    flip = true;
//                                    break;
//                                }
//                                Triangle tc = new Triangle(tri.a, tri.b, j, markers);
//                                idx = triangles.indexOf(tc);
//                                if (idx >= 0) {
//                                    triangles.set(t, new Triangle(j, tri.b, tri.c, markers));
//                                    triangles.set(idx, new Triangle(tri.a, j, tri.c, markers));
//                                    flip = true;
//                                    break;
//                                }
//                            }
//                        }
//                }
//            } while (flip);
//        }
//    }

    /*
     * Screen-space x: 0...imgx-1 y: 0...imgy-1
     */
    public int sample(int x, int y) {
        List<Triangle> triangles=slice.triangles;
        double fx = x * slice.width / imgw;
        double fy = y * slice.height / imgh;
        for (int i = 0; i < triangles.size(); i++) {
            double t[] = triangles.get(i).transform(fx, fy);
            if (t != null) {
                int xx = (int) (t[0] * x_overlay[0].length / slice.width);
                int yy = (int) (t[1] * x_overlay.length / slice.height);
//                if(xx<0 || yy<0 || xx>=overlay[0].length || yy>=overlay.length)
//                    return 0;
                //return overlay[yy][xx];
                return fastoverlay[xx+yy*x_overlay[0].length];
            }
        }
        return 0;// 0x80000000;
    }

    public void reDraw() {
        if(slice==null)return;//!!
        drawOvly();
        if (debug.isSelected())
            drawDebug();
        drawPins();
    }

    public void drawOvly() {
        GraphicsContext ctx = ovlycnv.getGraphicsContext2D();
        ctx.clearRect(0, 0, imgcnv.getWidth(), imgcnv.getHeight());
        PixelWriter pw = ctx.getPixelWriter();

        int op = (int) opacitySlider.getValue() * 255 / 100;
        if (op < 255) {
            int a = op << 24;
            IntStream.range(0, imgh).parallel().forEach(y -> {
                for (int x = 0; x < imgw; x++) {
                    int o = sample(x, y);
                    if (o != 0)
                        synchronized (pw) {
                            pw.setArgb(imgx + x, imgy + y, a + palette.fastcolors[o]);
                        }
                }
            });
        } else {
            Color c = outColor.getValue();
            int a = 0xFF000000 + ((int) (c.getRed() * 255) << 16) + ((int) (c.getGreen() * 255) << 8)
                    + (int) (c.getBlue() * 255);
//            for(int y=1;y<imgh;y++)
//                for(int x=1;x<imgw;x++) {
//                    int o=sample(x,y);
//                    if(o!=sample(x-1,y) || o!=sample(x,y-1))
//                        pw.setArgb(imgx+x, imgy+y, a);
//                }
            IntStream.range(0, imgh).parallel().forEach(y -> {
                for (int x = 1; x < imgw; x++) {
                    int o = sample(x, y);
                    if (o != sample(x - 1, y) || o != sample(x, y - 1))
                        synchronized (pw) {
                            pw.setArgb(imgx + x, imgy + y, a);
                        }
                }
            });
        }
    }

    private void drawPins() {
        GraphicsContext ctx = ovlycnv.getGraphicsContext2D();
        ctx.setStroke(pinColor.getValue());
        List<Marker> markers=slice.markers;
        for (int i = 0; i < markers.size(); i++) {
            Marker m = markers.get(i);
            double nx = m.nx * imgw / slice.width + imgx;
            double ny = m.ny * imgh / slice.height + imgy;
            ctx.strokeLine(nx, ny - 10, nx, ny + 10);
            ctx.strokeLine(nx - 10, ny, nx + 10, ny);
            double ox = m.ox * imgw / slice.width + imgx;
            double oy = m.oy * imgh / slice.height + imgy;
            ctx.strokeLine(ox, oy, nx, ny);
        }
    }

    private void drawDebug() {
        GraphicsContext ctx = ovlycnv.getGraphicsContext2D();
        ctx.setStroke(debColor.getValue());
        List<Triangle> triangles=slice.triangles;
        for (int i = 0; i < triangles.size(); i++) {
            Triangle t = triangles.get(i);
            double ax = imgx + imgw * t.A.nx / slice.width;
            double ay = imgy + imgh * t.A.ny / slice.height;
            double bx = imgx + imgw * t.B.nx / slice.width;
            double by = imgy + imgh * t.B.ny / slice.height;
            double cx = imgx + imgw * t.C.nx / slice.width;
            double cy = imgy + imgh * t.C.ny / slice.height;
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

    Palette palette;
    Int32Slices slicer;
    Path base;

    @FXML
    void open(ActionEvent event) throws Exception {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pick JSON file");
        ExtensionFilter ef=new ExtensionFilter("JSON files", "*.json");
        fc.getExtensionFilters().add(ef);
        //fc.setSelectedExtensionFilter(ef);
        File f = fc.showOpenDialog(stage);
        if (f != null) {
            // !! unload
            base=f.getParentFile().toPath();
            series = new Series();
            try (FileReader fr = new FileReader(f)) {
                Map<String, String> resolver = new HashMap<>();
                resolver.put("resolution", "target-resolution");
                JSON.mapObject(JSON.parse(fr), series, resolver);
            }
            series.propagate();
            //!!
            if(series.target.equals("WHS_Rat_v2_39um.cutlas")) {
                palette=ITKLabel.parseLabels("C:\\BigNII\\temp\\WHS_SD_rat_atlas_v3.label");
                slicer=new Int32Slices("C:\\BigNII\\temp\\WHS_SD_rat_atlas_v3.nii");
            }
            spnVal.setMin(1);
            spnVal.setMax(series.slices.size());
            spnVal.setValue(spnVal.getMax()/2);
//            loadView();
        }
    }
    
    void loadView() {
        slice=series.slices.get(spnVal.getValue()-1);
        image=new Image("file:"+base.resolve(slice.filename));
        drawImage();
        
        Double ouv[]=slice.anchoring.toArray(new Double[0]);
        x_overlay=slicer.getInt32Slice(ouv[0], ouv[1], ouv[2], ouv[3], ouv[4], ouv[5], ouv[6], ouv[7], ouv[8], false);
        fastoverlay=new int[x_overlay.length*x_overlay[0].length];
        for(int y=0;y<x_overlay.length;y++) {
            int l[]=x_overlay[y];
            for(int x=0;x<l.length;x++)
                fastoverlay[x+y*l.length]=palette.fullmap.get(l[x]).remap;
        }
        
//        slice.width=s.width;
//        slice.height=s.height;
//        markers.add(new Marker(0, 0));
//        markers.add(new Marker(slice.width, 0));
//        markers.add(new Marker(0, slice.height));
//        markers.add(new Marker(slice.width, slice.height));
        slice.triangulate();
        reDraw();
    }

    @FXML
    void save(ActionEvent event) {

    }
    @FXML
    void exprt(ActionEvent event) {

    }

    @FXML
    void saveas(ActionEvent event) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pick JSON file");
        ExtensionFilter ef=new ExtensionFilter("JSON files", "*.json");
        fc.getExtensionFilters().add(ef);
        File f=fc.showSaveDialog(stage);
        if(f!=null) {
            try(FileWriter fw=new FileWriter(f)){
                series.toJSON(fw);
            }
        }
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
    
    @FXML
    void goFirst(ActionEvent event) {
        if(series==null)return;
        spnVal.setValue(1);
    }
    
    @FXML
    void goLast(ActionEvent event) {
        if(series==null)return;
        spnVal.setValue(series.slices.size());
    }
    
    @FXML
    void less10(ActionEvent event) {
        if(series==null)return;
        spnVal.decrement(10);
    }
    
    @FXML
    void more10(ActionEvent event) {
        if(series==null)return;
        spnVal.increment(10);
    }

    Stage stage;
}
