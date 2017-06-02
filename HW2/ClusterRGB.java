
import java.awt.*;
import java.util.*;

public class ClusterRGB {
    Point3 centroid;
    public int nump;
    public java.util.List<Point3> allp;  
    
    ClusterRGB(){
      nump = 0;
      allp = new ArrayList<Point3>();
    } 

    ClusterRGB(int x, int y, int z){
      centroid = new Point3(x,y,z);
      nump = 0;
      allp = new ArrayList<Point3>();

    }

}