import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.*;
import org.jtransforms.dct.FloatDCT_2D;


public class myDecoder implements MouseMotionListener,MouseListener {
	private int scale =8;
	private int block_size = scale*scale; 
	boolean pauseclick = true;
	int aha = 0;
	
	class Block{
		int[] r = new int[block_size];
		int[] g = new int[block_size];
		int[] b = new int[block_size];
		int flag;
		int block_no;
		
		public int[] getR() {
			return r;
		}
		public void setR(int[] r) {
			this.r = r;
		}
		public int[] getG() {
			return g;
		}
		public void setG(int[] g) {
			this.g = g;
		}
		public int[] getB() {
			return b;
		}
		public void setB(int[] b) {
			this.b = b;
		}
		public int getFlag() {
			return flag;
		}
		public void setFlag(int flag) {
			this.flag = flag;
		}
		public int getBlock_no() {
			return block_no;
		}
		public void setBlock_no(int block_no) {
			this.block_no = block_no;
		}
	}
	
public void readCoeff(String filename,int forequan,int backquan ){

    File file = new File(filename);
    myDecoder decoder = new myDecoder();
   
    String [] oneBlock = new String[193];
    BufferedInputStream fis;  
    BufferedReader reader;
    int flag;
    ArrayList<Block> block_list = new ArrayList<Block>();	
	
    JFrame frame = new JFrame();
    Container pane = new Container();
    JButton start = new JButton("Play");
    JButton pause = new JButton("Pause");
    //JButton stop = new JButton("Stop");   
    pane.setLayout(new FlowLayout());
    pane.add(start);
    pane.add(pause);
    //pane.add(stop);
    JLabel content = new JLabel();
    frame.getContentPane().add(content, BorderLayout.CENTER);
	frame.getContentPane().add(pane, BorderLayout.PAGE_END);
	pause.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  			
					aha = 10000;
					pauseclick = false;
        }  
    });   
    //ji -------------------------------------------------------
    frame.getContentPane().addMouseMotionListener(this);
    frame.getContentPane().addMouseListener(this);
    //-------------------------------------------------------

    try{
      
      String line = null;
      int row_index = 0;
      
      while(true){
    	  fis= new BufferedInputStream(new FileInputStream(file));  
          reader= new BufferedReader(new InputStreamReader(fis,"utf-8"),8*1024*1024);  
        
      while ((line = reader.readLine())!=null) {
    	int m = 0;
        int n = 0;
        int x = 0;
        oneBlock = line.split(" "); 
        int[] r = new int[block_size];
        int[] g = new int[block_size];
        int[] b = new int[block_size];
        flag = Integer.parseInt(oneBlock[0]);
        for(int i =1; i<block_size+1;i++){
        	r[x] = Integer.parseInt(oneBlock[i]); 
        	x++;
        }
        for(int i =block_size+1; i<block_size*2+1;i++){
           
        	g[m] = Integer.parseInt(oneBlock[i]); 
        	m++;
        }
        for(int i =block_size*2+1; i<block_size*3+1;i++){
        	b[n] = Integer.parseInt(oneBlock[i]); 
        	n++;
        }
        Block block = new Block();
        block.setFlag(flag);
		block.setR(r);
		block.setG(g);
		block.setB(b);
		
		block_list.add(block);
		
        if(row_index == 8159){
        	
        	BufferedImage image = new BufferedImage(960, 544, BufferedImage.TYPE_INT_RGB);
        	
        	//ji---------------------------------------------------------
    		int[] gazed_blocks = {};
    		if(1 == gazeControl){
	    		if(mouse_x <= 959 && mouse_y <= 543 && mouse_x >=0 && mouse_y >= 0){
	    			gazed_blocks = getFrameGazed(mouse_x,mouse_y);
	    		}
    		}
    		image = decoder.quantitize(block_list,forequan,backquan,gazed_blocks);
        	//------------------------------------------------------------
    		
        	content.setIcon(new ImageIcon(image));
        	frame.pack();
            frame.setVisible(true);      
        	row_index = -1;
        	block_list.clear();
        }
        //pause
        if(aha>0){
        	while(true){
        		aha = aha-1;
        		start.addActionListener(new ActionListener(){  
                    public void actionPerformed(ActionEvent e) {                
                 	   pauseclick = true;
                 	   aha = 0;           	   
                     }  
                 }); 
        		if(pauseclick){
        			break;
        		}
        	} 	         	
        }
        
        row_index++;
      }
      reader.close();
      }
    } catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
    
}

//ji
public BufferedImage quantitize(ArrayList<Block> block_list,int forequan,int backquan,int[] gazed_blocks)
    {
        int list_size = block_list.size();
        int i = 0;
        int j = 0;
        float tr;
        float tg;
        float tb;
        int[] r = new int[block_size];
        int[] g = new int[block_size];
        int[] b = new int[block_size];
    	float[] newr = new float[block_size];
        float[] newg = new float[block_size];
    	float[] newb = new float[block_size];
    	BufferedImage image = new BufferedImage(960, 544, BufferedImage.TYPE_INT_RGB);
    	
    	//ji
    	Arrays.sort(gazed_blocks);
    	//
        for (i=0; i<list_size; i++)
        {      	
        	
        	int flag = block_list.get(i).getFlag();       	
        	r = block_list.get(i).getR();
        	g = block_list.get(i).getG();
        	b = block_list.get(i).getB();
        	
        	//ji----------------------------------------------------
            if(flag==0){ 
            	if(Arrays.binarySearch(gazed_blocks,i)>=0){
            		for (j=0; j<block_size; j++){                
	                    newr[j] = r[j];
	                    newg[j] = g[j];
	                    newb[j] = b[j];
	                 }
            		
            	}else{
	                for (j=0; j<block_size; j++){                
	                    tr =  r[j]/ backquan;
	                    tr = Math.round(tr);
	                    tr = tr * backquan;
	                    newr[j] = Math.round(tr);
	                    
	                    tg =  g[j]/ backquan;
	                    tg = Math.round(tg);
	                    tg = tg * backquan;
	                    newg[j] = Math.round(tg);
	                    
	                    tb =  b[j]/ backquan;
	                    tb = Math.round(tb);
	                    tb = tb * backquan;
	                    newb[j] = Math.round(tb);
	                    
	                 }
            		
            	}
                FloatDCT_2D idctr = new FloatDCT_2D(scale,scale);
                FloatDCT_2D idctg = new FloatDCT_2D(scale,scale);
                FloatDCT_2D idctb = new FloatDCT_2D(scale,scale);
           	    idctr.inverse(newr, true);
           	    idctg.inverse(newg, true);
           	    idctb.inverse(newb, true);
           	    
           	 for (int a = 0; a < block_size; a++) {
           		if(newr[a] > 255){
           			newr[a]= 255;
     			}
           		if(newg[a] > 255){
           			newg[a]= 255;
     			}
           		if(newb[a] > 255){
           			newb[a]= 255;
     			}
     			if(newr[a] < 0){
     				newr[a] = 0;
     			}
     			if(newg[a] < 0){
     				newg[a] = 0;
     			}
     			if(newb[a] < 0){
     				newb[a] = 0;
     			}
     			int pix = ((int)newr[a]*256+(int)newg[a])*256+(int)newb[a];
     			
     			int x = a/8;
     			int y = a % 8;
     			int m = i%120;
     			int n = i/120;		
     			image.setRGB(y+m*8,x+n*8,pix);    			  			
     		 }                    
            } if(flag!=0){
            	if(Arrays.binarySearch(gazed_blocks,i)>=0){
            		for (j=0; j<block_size; j++){                
	                    newr[j] = r[j];
	                    newg[j] = g[j];
	                    newb[j] = b[j];
	                 }
            	}else{
               
	                for (j=0; j<block_size; j++){                
	                    tr =  r[j]/ forequan;
	                    tr = Math.round(tr);
	                    tr = tr * forequan;
	                    newr[j] = Math.round(tr);
	                    
	                    tg =  g[j]/ forequan;
	                    tg = Math.round(tg);
	                    tg = tg * forequan;
	                    newg[j] = Math.round(tg);
	                    
	                    tb =  b[j]/ forequan;
	                    tb = Math.round(tb);
	                    tb = tb * forequan;
	                    newb[j] = Math.round(tb);
	                    
	                 }
            	}
                FloatDCT_2D idctr = new FloatDCT_2D(scale,scale);
                FloatDCT_2D idctg = new FloatDCT_2D(scale,scale);
                FloatDCT_2D idctb = new FloatDCT_2D(scale,scale);
           	    idctr.inverse(newr, true);
           	    idctg.inverse(newg, true);
           	    idctb.inverse(newb, true);
           	    
           	 for (int a = 0; a < block_size; a++) {
           		if(newr[a] > 255){
           			newr[a]= 255;
     			}
           		if(newg[a] > 255){
           			newg[a]= 255;
     			}
           		if(newb[a] > 255){
           			newb[a]= 255;
     			}
     			if(newr[a] < 0){
     				newr[a] = 0;
     			}
     			if(newg[a] < 0){
     				newg[a] = 0;
     			}
     			if(newb[a] < 0){
     				newb[a] = 0;
     			}
     			int pix = ((int)newr[a]*256+(int)newg[a])*256+(int)newb[a];
     			
     			int x = a/8;
     			int y = a % 8;
     			int m = i%120;
     			int n = i/120;		
     			image.setRGB(y+m*8,x+n*8,pix);    			  			
     		 }    
                
            }   
        }
        
        return image;
    }

  public static void main(String[] args) {
     
	 myDecoder decoder = new myDecoder();     
     String filename = args[0];
     int quan_n1 = Integer.parseInt(args[1]);
     int quan_n2 = Integer.parseInt(args[2]);
     gazeControl = Integer.parseInt(args[3]);
     long startTimeg=System.currentTimeMillis();
     decoder.readCoeff(filename,quan_n1,quan_n2);
     long endTimeg=System.currentTimeMillis();
  	float excTimeg=(float)(endTimeg-startTimeg)/1000;
    	System.out.println("total time："+excTimeg+"s");
  }
  
  
  //ji---------------------------------------------------------------
  
  @Override
  public void mouseDragged(MouseEvent e) {
  	// TODO Auto-generated method stub
  }


  int mouse_x = -1;
  int mouse_y = -1;
  static int gazeControl = 0;
  
  @Override
  public void mouseMoved(MouseEvent e) {
  	// TODO Auto-generated method stub
	mouse_x = -1;
	mouse_y = -1;
	mouse_x = e.getX();
	mouse_y = e.getY();
  }
  


  private int[] getFrameGazed(int mouse_x, int mouse_y) {
  	// TODO Auto-generated method stub
	 
  	//System.out.println("mouse x: " +mouse_x + " ;mouse y: "+mouse_y);
  	
  	int x = mouse_x;
    int y = mouse_y;

    int block = x/8 + (y/8)*120;
    int count = 0;
    int top = 3;
    int bottom = 4;
    int left = 3;
    int right = 4;
    int column = block%120;
    int row = block/120;

    if (row < 3){
        top = row;
    }
    if (row > 63){
        bottom = 67-row;
    }
    if (column > 115){
        right = 119-column;
    }
    if (column < 3){
        left = column;
    }


    int size = (top+bottom+1)*(left+right+1);
    int[] gazeBlockArray = new int[size];
    for (int j = (-1)*top; j<=bottom; j++){
        for (int i = (-1)*left; i<=right; i++){
            gazeBlockArray[count] = block + i + 120*j;
            count++;
        }
    }

  	return gazeBlockArray;
  }

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
	    // TODO Auto-generated method stub
		mouse_x = -1;
		mouse_y = -1;
	}

}


