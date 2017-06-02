
import java.awt.*;
import java.util.*;

public class Cluster {
    public Point centroid;
    public int nump;
    public java.util.List<Point> allp;  
    
    Cluster(){
      nump = 0;
      allp = new ArrayList<Point>();
    } 

    Cluster(int x, int y){
      centroid = new Point(x,y);
      nump = 0;
      allp = new ArrayList<Point>();

    }

}