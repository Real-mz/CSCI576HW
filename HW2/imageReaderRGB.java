
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


public class imageReaderRGB{
	

  public void showImage(String[] args){
	  
    int width = 352;
  	int height = 288;
    int N = Integer.parseInt(args[1]);
    Point3[][] vector = new Point3[width/2][height]; 
    ClusterRGB[] clust = new ClusterRGB[N]; 
    int[][] pixvalue = new int[width][height];
    Point3[] stop=new Point3[clust.length];
    int[] pixelnew = new int [(width*height)];

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    BufferedImage image_compression = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
   
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
    				
              //byte gray = bytes[index];
            byte r = bytes[index];
            byte g = bytes[index+height*width];
            byte b = bytes[index+height*width*2]; 
            int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
            image.setRGB(x,y,pix);
              //pixels[index] = gray & 0xFF;  
              pixvalue[x][y] = pix;
    				  index++;
    			 }
    		}    
        
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
        stop[i]=new Point3();
        stop[i].setLocation(clust[i].centroid.getr(),clust[i].centroid.getg(),clust[i].centroid.getb());
      }
    
      UpdateClusterRGB(clust);
      if(StopCheck(clust,stop)){
         break;
      } 
    }
    
    for(int w=0;w<vector.length;w++){
    for(int h=0;h<vector[w].length;h++){
      Point3 close = FindClusterRGBPoint(vector[w][h],clust);
      vector[w][h].setLocation(close.getr(),close.getg(),close.getb());
    }
    }

    int inde=0;
    for(int h=0;h<vector[0].length;h++){
      for(int w=0;w<vector.length;w++){      
        
        pixelnew[inde] = ((int)vector[w][h].getr()*256+(int)vector[w][h].getg())*256+(int)vector[w][h].getb();
        pixelnew[inde+1] = pixelnew[inde];
        inde = inde+2;
        
      }
    }
    int i = 0;
    for(int h=0;h<height;h++){
      for(int w=0;w<width;w++){      
        
        image_compression.setRGB(w,h,pixelnew[i]);
        i++;
      }
    }


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

public static void InitializeVector(Point3[][] vector, int[][] pix){

    for(int h=0;h<vector[0].length;h++){
      for(int w=0;w<vector.length;w++){       
        int w1 = w*2;
        int w2 = w*2+1;
        int w1pix = pix[w1][h];
        int w2pix = pix[w2][h];
        //int vecpix = (w1pix+w2pix)/2;
        int vecpix = w2pix;
        //int test = pix[w][h];
        Color vecColor = new Color(vecpix); 
        
        vector[w][h] = new Point3(vecColor.getRed(),vecColor.getGreen(),vecColor.getBlue());
      }
    }

} 

public static void InitializeCentroid(ClusterRGB[] clus){
    int x,y,z;
    int ind=0;
    int remain=clus.length;
    
    Random rand = new Random();
    for(int temp=0;temp<remain;temp++){
   
      x = rand.nextInt(255);
      y = rand.nextInt(255);
      z = rand.nextInt(255);
      clus[ind] = new ClusterRGB();
      
      clus[ind].centroid = new Point3();
      clus[ind].centroid.setLocation(x,y,z);
      ind++;
    }
  }

public static Point3 FindClusterRGBPoint(Point3 pt, ClusterRGB[] clus){
    double dismin=255;
    double new_dismin=0; 
    Point3 ClusterRGBpoint = new Point3();

    for(int ind=0;ind<clus.length;ind++){
      
      double dist = Math.abs(pt.getDistance(clus[ind].centroid));
      new_dismin=Math.min(dismin,dist);
    
      if(new_dismin<dismin){
        
        dismin=new_dismin;
        ClusterRGBpoint.setLocation(clus[ind].centroid.getr(),clus[ind].centroid.getg(),clus[ind].centroid.getb());
      
      }
    } 
    return ClusterRGBpoint;
  }


  public static void AddPoint(Point3 po, ClusterRGB[] clus){
   
    Point3 m = FindClusterRGBPoint(po,clus);

    for(int ind=0;ind<clus.length;ind++){
   
      if(clus[ind].centroid.equals(m)){
      
        clus[ind].allp.add(clus[ind].nump,po);     
        clus[ind].nump++;
      
      } 
    }
  }


public static void UpdateClusterRGB(ClusterRGB[] clus){
    
    for(int ind=0;ind<clus.length;ind++){
      
      int x=0,y=0,z=0;
      int count;

      for(count=0;count<clus[ind].nump;count++){
        x+=clus[ind].allp.get(count).getr();
        y+=clus[ind].allp.get(count).getg();
        z+=clus[ind].allp.get(count).getb();
      }
      
      if(count!=0){ 
        x/=count;
        y/=count;
        z/=count;
      }
      clus[ind].centroid.setLocation(x,y,z); 
    }
}

public static boolean StopCheck(ClusterRGB[] clus, Point3[] p){
    boolean flag = true;
    
    for(int ind=0;ind<clus.length;ind++){
      if(!clus[ind].centroid.equals(p[ind])){       
          flag = false;      
      }
    }
    return flag;
  }


  public static void main(String[] args) {
     imageReaderRGB ir = new imageReaderRGB();
	   ir.showImage(args);
		
  }


}

