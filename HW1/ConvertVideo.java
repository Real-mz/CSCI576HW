import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.io.FileOutputStream;


public class ConvertVideo {
  
  public void convVideo(String[] args){
    

    String op = args[2];
    int anti = Integer.parseInt(args[3]);
 
    ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
   
    try {
    
      File file = new File(args[0]);
      InputStream is = new FileInputStream(file);
      File outfile= new File(args[1]);
      FileOutputStream fop = new FileOutputStream(outfile);

  
      long len = file.length();
      byte[] bytes = new byte[(int)len];
      int offset = 0;
        int numRead = 0;
        
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
     
      int index = 0;

      if(op.equals("HD2SD")){
        
        int width = 960;
        int height = 540;

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

            BufferedImage scaledImage = null;

            scaledImage = ScaledHD(image,176,144);
                  
            int w = scaledImage.getWidth();  
            int h = scaledImage.getHeight();      
            int[] pix = new int[w*h]; 
            int[] newpix = new int[w*h];
            scaledImage.getRGB(0, 0, w, h, pix, 0, w); 
            
            if (anti==1){                       
              newpix = avrFiltering(pix, w, h);  
              scaledImage.setRGB(0, 0, w, h, newpix, 0, w); 
            }else{
              newpix = pix;
            }      
          
            if (!outfile.exists()) {
              outfile.createNewFile();
            }
            byte[] newr = new byte[w*h];
            byte[] newg = new byte[w*h];
            byte[] newb = new byte[w*h];
          
            for (int a=0; a < w*h; a++){
               Color myColor = new Color(newpix[a]); 
          
               newr[a] = (byte)myColor.getRed(); 
               newg[a] = (byte)myColor.getGreen();
               newb[a] = (byte)myColor.getBlue();
            }
            
            fop.write(newr);
            fop.write(newg); 
            fop.write(newb);
    
            fop.flush();
          
            images.add(scaledImage);
            index += width*height*2;
        }
        fop.close(); 
      }else{
        int width = 176;
        int height = 144;

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

            BufferedImage scaledImage = null;

            scaledImage = ScaledSD(image,960,540);
            int w = scaledImage.getWidth();  
            int h = scaledImage.getHeight();      
            int[] pix = new int[w*h]; 
            int[] newpix = new int[w*h];
            scaledImage.getRGB(0, 0, w, h, pix, 0, w); 
            
            if (anti==1){                       
              newpix = avrFiltering(pix, w, h);  
              scaledImage.setRGB(0, 0, w, h, newpix, 0, w); 
            }else{
              newpix = pix;
            }      
          
            if (!outfile.exists()) {
              outfile.createNewFile();
            }
            byte[] newr = new byte[w*h];
            byte[] newg = new byte[w*h];
            byte[] newb = new byte[w*h];
          
            for (int a=0; a < w*h; a++){
               Color myColor = new Color(newpix[a]); 
          
               newr[a] = (byte)myColor.getRed(); 
               newg[a] = (byte)myColor.getGreen();
               newb[a] = (byte)myColor.getBlue();
            }
            
            fop.write(newr);
            fop.write(newg); 
            fop.write(newb);
    
            fop.flush();

            images.add(scaledImage);
            index += width*height*2;
         }
         fop.close();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Create frame and label to display video
    // JFrame frame = new JFrame();
    // JLabel content = new JLabel(new ImageIcon(images.get(0)));
    // frame.getContentPane().add(content, BorderLayout.CENTER);
    // frame.pack();
    // frame.setVisible(true);
   
    // for (int i = 1; i < images.size(); i++) {
    //   content.setIcon(new ImageIcon(images.get(i)));
    //   try {
    //     if (i==images.size()-1){
    //         i=0;
    //       }
    //     Thread.sleep(1000/10);        
    //   } catch (InterruptedException e) {
    //     Thread.currentThread().interrupt();
    //   }
    // }
    
  }

  public static BufferedImage ScaledHD(BufferedImage original, int width, int height) {
    
    double yscale = (double)height/(double)original.getHeight();
    double xscale = (double)width/(double)original.getWidth();

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      int yOrig = (int)((double)y / yscale);
      for (int x = 0; x < width; x++) {
        int xOrig = (int)((double)x / xscale);
        int pix = original.getRGB(xOrig, yOrig);
        image.setRGB(x, y, pix);
      }
    }
    return image;
  }

  public static BufferedImage ScaledSD(BufferedImage image, int width,int height) throws IOException {

    double scaleX = (double)width/(double)image.getWidth();
    double scaleY = (double)height/(double)image.getHeight();
    AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);

    AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);
    //AffineTransformOp bicubicScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BICUBIC);
    //AffineTransformOp nearestScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    
    return bilinearScaleOp.filter(image,new BufferedImage(width, height, image.getType()));
    
  }

  public static int[] avrFiltering(int pix[], int w, int h) {  
        int newpix[] = new int[w*h];  
        ColorModel cm = ColorModel.getRGBdefault();  
        int r = 0; 
        int g = 0;
        int b = 0; 
        for(int y=0; y<h; y++) {  
            for(int x=0; x<w; x++) {  
                if(x!=0 && x!=w-1 && y!=0 && y!=h-1) {  
                    
                    r = (cm.getRed(pix[x-1+(y-1)*w]) + cm.getRed(pix[x+(y-1)*w])+ cm.getRed(pix[x+1+(y-1)*w])  
                        + cm.getRed(pix[x-1+(y)*w]) +  cm.getRed(pix[x+1+(y)*w])  
                        + cm.getRed(pix[x-1+(y+1)*w]) + cm.getRed(pix[x+(y+1)*w]) + cm.getRed(pix[x+1+(y+1)*w]))/8;  
                    g = (cm.getGreen(pix[x-1+(y-1)*w]) + cm.getGreen(pix[x+(y-1)*w])+ cm.getGreen(pix[x+1+(y-1)*w])  
                        + cm.getGreen(pix[x-1+(y)*w]) +  cm.getGreen(pix[x+1+(y)*w])  
                        + cm.getGreen(pix[x-1+(y+1)*w]) + cm.getGreen(pix[x+(y+1)*w]) + cm.getGreen(pix[x+1+(y+1)*w]))/8;  
                    b = (cm.getBlue(pix[x-1+(y-1)*w]) + cm.getBlue(pix[x+(y-1)*w])+ cm.getBlue(pix[x+1+(y-1)*w])  
                        + cm.getBlue(pix[x-1+(y)*w]) + cm.getBlue(pix[x+1+(y)*w])  
                        + cm.getBlue(pix[x-1+(y+1)*w]) + cm.getBlue(pix[x+(y+1)*w]) + cm.getBlue(pix[x+1+(y+1)*w]))/8;  
                    newpix[y*w+x] = 255<<24 | r<<16 | g<<8 |b;  
                      
                } else {  
                    newpix[y*w+x] = pix[y*w+x];  
                }  
            }  
        }  
        return newpix;  
  }  

  public static void main(String[] args) {
     ConvertVideo cv = new ConvertVideo();
     cv.convVideo(args);
    
  }
}