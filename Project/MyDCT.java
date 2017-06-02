import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.jtransforms.dct.FloatDCT_2D;

public class MyDCT {
	
	//constant numbers
	private int scale =8;
	private int block_size = scale*scale; 
	int frame_width = 960;
	int frame_height = 540;
	
	class Block{
		float[] r = new float[block_size];
		float[] g = new float[block_size];
		float[] b = new float[block_size];
		int block_no;
		
		public float[] getR() {
			return r;
		}
		public void setR(float[] r) {
			this.r = r;
		}
		public float[] getG() {
			return g;
		}
		public void setG(float[] g) {
			this.g = g;
		}
		public float[] getB() {
			return b;
		}
		public void setB(float[] b) {
			this.b = b;
		}
		public int getBlock_no() {
			return block_no;
		}
		public void setBlock_no(int block_no) {
			this.block_no = block_no;
		}
	}
	
	
	/**
	 * @author ji
	 * @param block_no
	 * @param x
	 * @param y
	 * @return
	 */
	private int expand_8(int block_no, int x, int y){
		
		
		int every_row_block_no = (int) (Math.ceil(frame_width / (float)scale)) ; //120 correct
		int every_column_block_no = (int) (Math.ceil(frame_height / (float)scale)) ;//68 correct
		boolean row_expand = !(0==(frame_width%scale));//false correct
		boolean column_expand = !(0==(frame_height%scale));//true correct
		
		int block_first_pixel = (block_no/every_row_block_no)*frame_width*scale+(block_no%every_row_block_no)*scale;
		//System.out.println("block_first_pixel: "+block_first_pixel);//correct
		int pixel = 0;
		
		if((0==((block_no+1)%every_row_block_no))&&(block_first_pixel<(every_column_block_no-1)*8*frame_width)){
			if(row_expand){
				int exist_row = frame_width%scale;
				if(x<exist_row){
					pixel = block_first_pixel + frame_width * y + x;
				}else if(x>=exist_row){
					pixel = block_first_pixel + frame_width * y + (exist_row-1);
				}
			}else{
				pixel = block_first_pixel + frame_width * y + x;
				//System.out.println("last column of every row but not expand: "+block_no);
			}
		}
		
		else if((block_first_pixel>=(every_column_block_no-1)*8*frame_width)&&(0!=((block_no+1)%every_row_block_no))){
			if(column_expand){
				int exist_column = frame_height%scale;
				if(y<exist_column){
					pixel = block_first_pixel + frame_width * y + x;
					//System.out.println("last row of block but no expand: "+block_no);
				}else if(y>=exist_column){
					pixel = block_first_pixel + frame_width * (exist_column-1) + x;
					//System.out.println("last row of block & expand: "+block_no+" ,pixel: "+pixel);
				}
			}else{
				pixel = block_first_pixel + frame_width * y + x;
			}
		}
		
		else if((0==((block_no+1)%every_row_block_no))&&(block_first_pixel>=(every_column_block_no-1)*8*frame_width)){
			if(row_expand){
				int exist_row = frame_width%scale;
				if(x<exist_row){
					pixel = block_first_pixel + frame_width * y + x;
				}else if(x>=exist_row){
					pixel = block_first_pixel + frame_width * y + (exist_row-1);
				}
			}else if(column_expand){
				int exist_column = frame_height%scale;
				if(y<exist_column){
					pixel = block_first_pixel + frame_width * y + x;
					//System.out.println("last block & not expand column: "+block_no);
				}else if(y>=exist_column){
					pixel = block_first_pixel + frame_width * (exist_column-1) + x;
					//System.out.println("last block & expand column: "+block_no);
				}
			}else{
				pixel = block_first_pixel + frame_width * y + x;
			}
		}
		
		else{
			pixel = block_first_pixel + frame_width * y + x;
			//System.out.println("normal ones: "+block_no);
		}
		
		
		return pixel;
	}
	
	/**
	 * divide 8*8 blocks
	 * @author ji
	 * @param origin_frame_bytes
	 * @return
	 */
	private ArrayList<Block> divideBlock_8(byte[] origin_frame_bytes) {
		
		int block_total_no = (int) (Math.ceil(frame_width/(float)scale)*Math.ceil(frame_height/(float)scale));//120*68
		
		ArrayList<Block> block_list = new ArrayList<Block>();	
		
		//variables just for loop
		int ind = 0;
		int block_no = 0;
		
		while(block_no<block_total_no){
			
			Block block = new Block();
			float[] r = new float[block_size];
			float[] g = new float[block_size];
			float[] b = new float[block_size];
			
			
			for(int y = 0; y < scale; y++){
				for(int x = 0; x < scale; x++){
					
					int pixel;
					pixel = expand_8(block_no,x,y);
					
					float r_tmp = origin_frame_bytes[pixel] & 0xff;
					float g_tmp = origin_frame_bytes[pixel+frame_height*frame_width] & 0xff;
					float b_tmp = origin_frame_bytes[pixel+frame_height*frame_width*2] & 0xff; 
					
					
					r[ind] = r_tmp;
					g[ind] = g_tmp;
					b[ind] = b_tmp;
					
					
					ind++;
				}
			}
			
			block.setBlock_no(block_no);
			block.setR(r);
			block.setG(g);
			block.setB(b);
			block_list.add(block);
			
			ind=0;
			block_no = block_no +1;
			
		}
		
		return block_list;
	}
		
	
	void step2(String[] args, ArrayList<byte[]> ground_flag) {
		
		// read the file first and send file into a byte[]
		File file = new File(args[0]);
		String filename = (args[0].split("\\."))[0];
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	

		// define the output file
		FileWriter fw = null;
		try {
			fw = new FileWriter(filename+".cmp");
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		
		//define variables
		int numRead = 0;//the bytes read from a frame size
		int frame_byte = frame_width*frame_height*3;//size of a frame
		byte[] origin_frame_bytes = new byte[frame_byte];
		
		
		//main process
		try {
			int ground_flag_index = 0;
			
			while((numRead=is.read(origin_frame_bytes, 0, frame_byte)) >= 0){	//put one-frame-size of stream into the cache, only read from cache
				//divide one frame r+g+b into blocks
				ArrayList<Block> one_frame_blocks = new ArrayList<Block>();
				one_frame_blocks=divideBlock_8(origin_frame_bytes);
				
				//dct
				ArrayList<float []> one_frame_coeff = new ArrayList<float[]>();
				FloatDCT_2D dct = new FloatDCT_2D(8,8);
				
				for(Block block : one_frame_blocks){
					float[] dct_coeff = new float[block_size * 3];
					
					float[] r;
					r = block.getR();
					dct.forward(r, true);
					
					float[] g;
					g = block.getG();
					dct.forward(g, true);
					
					float[] b;
					b = block.getB();
					dct.forward(b, true);
					
					System.arraycopy(r, 0, dct_coeff, 0, r.length);
					System.arraycopy(g, 0, dct_coeff, r.length, g.length);
					System.arraycopy(b, 0, dct_coeff, r.length+g.length, b.length);
					
					one_frame_coeff.add(dct_coeff);
				}
				
				//identify the foreground/background
				byte[] frame_ground_flag = ground_flag.get(ground_flag_index);
				ground_flag_index++;
			
				//output one frame
				int frame_ground_flag_index = 0;
				
				for(float[] one_block_coeff : one_frame_coeff){
					
					//add foreground/background flag
					try {
						bw.write(frame_ground_flag[frame_ground_flag_index]+" ");
						frame_ground_flag_index++;
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					//add r,g,b coeffs
					for(float every_coeff : one_block_coeff){
						try {
							bw.write((int)every_coeff+" ");
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					//change a line
					bw.write("\r\n");
					
					
				}
				//end output
				
				
			}
			bw.flush();
			bw.close();
			//end while one-frame extraction
		} catch (IOException e) {
			e.printStackTrace();
		}
		//end main process
		
	}

}