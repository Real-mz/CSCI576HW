import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


public class VideoPlayer {
	
  public void showVideo(String[] args){
	  
    int width = Integer.parseInt(args[1]);
	int height = Integer.parseInt(args[2]);
    int frameRate = Integer.parseInt(args[3]);
 
    ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
   
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
        while (index+height*width*2 < len) {
            
        	BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            for (int y = 0; y < height; y++) {
    			for (int x = 0; x < width; x++) {
    				byte r = bytes[index];
    				byte g = bytes[index+height*width];
    				byte b = bytes[index+height*width*2]; 
    				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    				image.setRGB(x,y,pix);
    				index++;
    			}
    		}    
            images.add(image);
            index += width*height*2;
        }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Create frame and label to display video
    JFrame frame = new JFrame();
    JLabel content = new JLabel(new ImageIcon(images.get(0)));
    frame.getContentPane().add(content, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
   
    for (int i = 1; i < images.size(); i++) {
      content.setIcon(new ImageIcon(images.get(i)));
      try {
    	  if (i==images.size()-1){
          	i=0;
          }
        Thread.sleep(1000/frameRate);        
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    
  }
  public static void main(String[] args) {
     VideoPlayer vp = new VideoPlayer();
	 vp.showVideo(args);
		
  }
}
