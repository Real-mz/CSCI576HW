
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


public class imageReaderGray{
	

  public void showImage(String[] args){
	  
    int width = 352;
  	int height = 288;
    int N = Integer.parseInt(args[1]);
    Point[][] vector = new Point[width/2][height]; 
    Cluster[] clust = new Cluster[N]; 
    int[][] pixvalue = new int[width][height];
    Point[] stop=new Point[clust.length];
    int[] pixelnew = new int [(width*height)];

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    BufferedImage image_compression = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    
   
    try {
        
	      File file = new File(args[0]);
	      InputStream is = new FileInputStream(file);
  
	      long len = file.length();
	      byte[] bytes = new byte[(int)len];
	      int offset = 0;
        int numRead = 0;
        
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
     
    	  int index = 0;
  
        int[] pixels = new int [(width*height)];
            
        for (int y = 0; y < height; y++) {
    			 for (int x = 0; x < width; x++) {
    				
              byte gray = bytes[index];
              pixels[index] = gray & 0xFF;  
              pixvalue[x][y] = gray & 0xFF;
    				  index++;
    			 }
    		}    
        //System.out.print(pixels[8]);
        image.getRaster().setPixels(0, 0, width, height, pixels);
         
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  
    InitializeVector(vector,pixvalue);
    InitializeCentroid(clust);
    for(int x=0;x<20000;x++){
    
      for(int w=0;w<vector.length;w++){
        for(int h=0;h<vector[w].length;h++){
          AddPoint(vector[w][h],clust);        
        }
      }
     
      for(int i=0;i<clust.length;i++){
        stop[i]=new Point();
        stop[i].setLocation(clust[i].centroid);
      }
    
      UpdateCluster(clust);
      if(StopCheck(clust,stop)){
         break;
      } 
    }
    
    for(int w=0;w<vector.length;w++){
    for(int h=0;h<vector[w].length;h++){
      Point close = FindClusterPoint(vector[w][h],clust);
      vector[w][h].setLocation(close);
    }
    }

    int inde=0;
    for(int h=0;h<vector[0].length;h++){
      for(int w=0;w<vector.length;w++){      
        
        pixelnew[inde] = (int)vector[w][h].getX();
        pixelnew[inde+1] = (int)vector[w][h].getY();
        inde = inde+2;
      
      }
    }
    //System.out.print(pixelnew[8]);
    image_compression.getRaster().setPixels(0, 0, width, height, pixelnew);



    // Create frame and label to display video
    JFrame frame = new JFrame();
    JPanel  panel = new JPanel ();
    
    JLabel original = new JLabel(new ImageIcon(image));
    JLabel compression = new JLabel(new ImageIcon(image_compression));
    panel.add (original);
    panel.add (compression);
    frame.getContentPane().add(panel);


    frame.pack();
    frame.setVisible(true);
     
  }

public static void InitializeVector(Point[][] vector, int[][] pix){
    //System.out.print(p.length);
    for(int h=0;h<vector[0].length;h++){
      for(int w=0;w<vector.length;w++){       
        int w1 = w*2;
        int w2 = w*2+1;
        vector[w][h] = new Point(pix[w1][h],pix[w2][h]);
      }
    }

} 

public static void InitializeCentroid(Cluster[] clus){
    int x,y;
    int ind=0;
    int remain=clus.length;

    if(clus.length>8){
    int gridnum = (int)Math.floor(Math.sqrt(clus.length));
    int totalgrid = (int)Math.pow(gridnum,2);
    int step = (int)256/gridnum;
    int start = (int)step/2;

    for(int h=0,yp=start;h<gridnum;h++){
      for(int w=0,xp=start;w<gridnum;w++){
        clus[ind] = new Cluster();
        clus[ind].centroid = new Point(xp,yp);
        xp+=step;
        ind++;
      
      }
      yp+=step;
    }
      remain -= totalgrid;
    }   
    
    Random rand = new Random();
    for(int temp=0;temp<remain;temp++){
   
      x = rand.nextInt(255);
      y = rand.nextInt(255);
      clus[ind] = new Cluster();
      
      clus[ind].centroid = new Point();
      clus[ind].centroid.setLocation(x,y);
      ind++;
    }
  }

public static Point FindClusterPoint(Point pt, Cluster[] clus){
    double dismin=255;
    double new_dismin=0; 
    Point clusterpoint = new Point();

    for(int ind=0;ind<clus.length;ind++){
      
      double dist = Math.abs(pt.distance(clus[ind].centroid));
      new_dismin=Math.min(dismin,dist);
    
      if(new_dismin<dismin){
        
        dismin=new_dismin;
        clusterpoint.setLocation(clus[ind].centroid);
      
      }
    } 
    return clusterpoint;
  }


  public static void AddPoint(Point po, Cluster[] clus){
   
    Point m = FindClusterPoint(po,clus);

    for(int ind=0;ind<clus.length;ind++){
   
      if(clus[ind].centroid.equals(m)){
      
        clus[ind].allp.add(clus[ind].nump,po);     
        clus[ind].nump++;
      
      } 
    }
  }


public static void UpdateCluster(Cluster[] clus){
    
    for(int ind=0;ind<clus.length;ind++){
      
      int x=0,y=0;
      int count;

      for(count=0;count<clus[ind].nump;count++){
        x+=clus[ind].allp.get(count).getX();
        y+=clus[ind].allp.get(count).getY();
      }
      
      if(count!=0){ 
        x/=count;
        y/=count;
      }
      clus[ind].centroid.setLocation(x,y); 
    }
}

public static boolean StopCheck(Cluster[] clus, Point[] p){
    boolean flag = true;
    
    for(int ind=0;ind<clus.length;ind++){
      if(!clus[ind].centroid.equals(p[ind])){       
          flag = false;      
      }
    }
    return flag;
  }


  public static void main(String[] args) {
     imageReaderGray ir = new imageReaderGray();
	   ir.showImage(args);
		
  }


}

