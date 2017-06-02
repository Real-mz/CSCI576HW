
import java.io.*;
import java.util.ArrayList;


public class myEncoder {
    int width = 960;
    int height = 540;
    int pixelPerChannelPerFrame = width * height;
    int pixelPerFrame3Channels = pixelPerChannelPerFrame *3;
    int totalPixelPerFrame = 960 * (540+4);
    int pixelPerLargeBlock = 16*16;
    int k = 8;
    int totalBlockNumber8_8 = 8160;


    //ji for foreground/background
    static ArrayList<byte[]> ground_flag = new ArrayList<byte[]>();


    public byte[] oneBlock (byte[] YChannelPerFrame, int blockNo){ //Block = 0,1,2...960*(540+12)/256 -1

        int firstPixelIndexPerBlock = (blockNo/60)*(960*16) + (blockNo%60)*16;
        int pixel;
        byte[] oneBlock = new byte[pixelPerLargeBlock];  //0-256
        // 16*16 block
        for (int i = 0; i<256; i++){
            pixel = firstPixelIndexPerBlock + 960*(i/16) + (i%16);
            //i/16 == the i-th row, i%16 == the i-th column
            oneBlock[i] = YChannelPerFrame[pixel];
        }
        return oneBlock;
    }


    public int SAD(byte[] oneBlock, byte[] searchArea){
        int SAD=0;
        for (int i =0; i < 256; i++){
            SAD=SAD+Math.abs((int)oneBlock[i] - (int)searchArea[i]);
        }
        return SAD;
    }


    public int[] motionVector(byte[] oneBlock, int blockNo, byte[] previousYChannelPerFrame){
        int firstPixelIndexPerBlock = (blockNo/60)*(960*16) + (blockNo%60)*16;
        int lowestSAD = 10000000;// a random large number
        int mark = firstPixelIndexPerBlock;
        int markMotionVectorValue = 10000;
        int previousMark = firstPixelIndexPerBlock;
        byte[] searchArea = new byte[pixelPerLargeBlock];
        int[] motionVector = new int[2];

        if ((blockNo<=59) || (blockNo>=1980) || (blockNo%60 == 0) || (blockNo%60 == 59)){ // boundary conditions
            motionVector[0] = firstPixelIndexPerBlock;
            motionVector[1] = firstPixelIndexPerBlock;
        }

        for (int i = k*(-1); i<=k; i++ ){// the i-th line of the block, compared to the firstPixelIndexPerBlock
            for (int j = k*(-1); j<=k; j++){// the j-th column of the block, compared to the firstPixelIndexPerBlock
                int p = firstPixelIndexPerBlock+j+960*i; // p == the first pixel in each search area.
                for(int m = 0; m < 256; m++){
                    searchArea[m] = previousYChannelPerFrame[p + m%16 + (m/16)*960];
                }
                int newSAD = SAD(oneBlock, searchArea);
                if (newSAD < lowestSAD) {
                    lowestSAD = newSAD;
                    mark = p;
                    markMotionVectorValue = Math.abs(i) + Math.abs(j);

                } else if (newSAD == lowestSAD){
                    int currentMotionVectorValue = Math.abs(i) + Math.abs(j);
                    if (currentMotionVectorValue < markMotionVectorValue){
                        mark = p;
                        markMotionVectorValue = currentMotionVectorValue;
                    } // if >: no reaction // if ==: no reaction
                }
            }
        }  // total search area == (2k+1)^2

        motionVector[0] = mark;
        motionVector[1] = firstPixelIndexPerBlock;

        return motionVector;
    }



    public double motionVectorValue(int[] motionVector){
        double y = Math.abs((motionVector[0] / 960) - (motionVector[1] / 960));
        double x = Math.abs((motionVector[0] % 960) - (motionVector[1] % 960));
        return Math.sqrt(x*x + y*y);
    }


    public byte[] segmentation(double[] motionVectorValueArray, byte[] motionVectorDirectionArray){
        byte[] part1results_16_16 = new byte[2040];
        byte[] revisedPart1results_16_16 = new byte[2040];

        double[] p = new double[9];
        for (int j =0; j<2040;j++){
            if (!((j<=59) || (j>=1980) || (j%60 == 0) || (j%60 == 59))){
                p[motionVectorDirectionArray[j]]++;
            } else part1results_16_16[j] = 0;
        }
        double max = 0;
        byte mark = 0;
        for (byte i = 0; i<=8; i++){
            p[i] /= 1856;
            if (p[i] > max){
                max = p[i];
                mark = i;
            }
        }

        //ji
       /* System.out.println("mark= "+ mark);
        System.out.println("max= "+ max);

        for (byte i = 0; i<=8; i++){
            System.out.println(i+ " : " + p[i]);
        }*/


        double sum=0;
        for (int i =0; i< motionVectorValueArray.length;i++){
            sum += motionVectorValueArray[i];
        }
        double averageMotionVectorValue = sum/1856;
        //ji
        //System.out.println(averageMotionVectorValue);


        //consider motionVectorValues only, for non-moving cameras

            for (int i = 0; i<2040; i++){
                if (!((i<=59) || ( i>=1980) || (i%60 == 0) || (i%60 == 59))){
                    double currentValue = motionVectorValueArray[i];
                    byte currentDirection = motionVectorDirectionArray[i];
                    if (currentValue< averageMotionVectorValue-1 || currentValue > averageMotionVectorValue+1){   //value >1
                        part1results_16_16[i] = 1;
                    } else
                        part1results_16_16[i] = 0;
                }
            }





        // the camera does not move
//        if (averageMotionVectorValue <0.6){
//            for (int i = 0; i<2040; i++){
//                double currentValue = motionVectorValueArray[i];
//                byte currentDirection = motionVectorDirectionArray[i];
//                if (currentValue>=1 && (currentDirection == 1 || currentDirection == 5) ){   //value >1
//                    part1results_16_16[i] = 1;
//                } else
//                    part1results_16_16[i] = 0;
//            }
//        }
//        // the camera moves
//        else if (averageMotionVectorValue >= 0.6 && averageMotionVectorValue < 1.6){
//            for (int i = 0; i<2040; i++){
//                double currentValue = motionVectorValueArray[i];
//                byte currentDirection = motionVectorDirectionArray[i];
//                if (((currentValue >=2))  && (currentDirection == 1 || currentDirection == 5)){   //value >1
//                    part1results_16_16[i] = 1;
//                } else
//                    part1results_16_16[i] = 0;
//            }
//        }
//        else if (averageMotionVectorValue >= 1.6 && averageMotionVectorValue < 2.6){
//            for (int i = 0; i<2040; i++){
//                double currentValue = motionVectorValueArray[i];
//                byte currentDirection = motionVectorDirectionArray[i];
//                if (((currentValue <=1) || (currentDirection >=3.6))  && (currentDirection == 1 || currentDirection == 5) ){   //value >1
//                    part1results_16_16[i] = 1;
//                } else
//                    part1results_16_16[i] = 0;
//            }
//        }
//        else if (averageMotionVectorValue >= 2.6 && averageMotionVectorValue < 3.6){
//            for (int i = 0; i<2040; i++){
//                double currentValue = motionVectorValueArray[i];
//                byte currentDirection = motionVectorDirectionArray[i];
//                if (((currentValue <=1) || (currentDirection >=4))  && (currentDirection == 1 || currentDirection == 5) ){   //value >1
//                    part1results_16_16[i] = 1;
//                } else
//                    part1results_16_16[i] = 0;
//            }
//        }
//        else if (averageMotionVectorValue >= 3.6 && averageMotionVectorValue < 4.6){
//            for (int i = 0; i<2040; i++){
//                double currentValue = motionVectorValueArray[i];
//                byte currentDirection = motionVectorDirectionArray[i];
//                if (((currentValue <=1) || (currentDirection >=5))  && (currentDirection == 1 || currentDirection == 5) ){   //value >1
//                    part1results_16_16[i] = 1;
//                } else
//                    part1results_16_16[i] = 0;
//            }
//        }




//        if (mark ==5){ // camera moves tonthenleft
//            for (int i = 0; i<2040; i++){
//                if (!((i<=59) || (i>=1980) || (i%60 == 0) || (i%60 == 59))){
//                    double currentValue = motionVectorValueArray[i];
//                    byte currentDirection = motionVectorDirectionArray[i];
//                    if (((currentDirection == 0 || currentDirection ==1)&& currentValue >=2) || ((currentDirection ==5) &&(currentValue == 0))){
//                        part1results_16_16[i] = 1;
//                    } else
//                        part1results_16_16[i] = 0;
//                }
//            }
//        } else if (mark ==1){ // camera moves tonthenleft
//            for (int i = 0; i<2040; i++){
//                if (!((i<=59) || (i>=1980) || (i%60 == 0) || (i%60 == 59))){
//                    double currentValue = motionVectorValueArray[i];
//                    byte currentDirection = motionVectorDirectionArray[i];
//                    if (((currentDirection == 0 || currentDirection ==5)&&(currentValue>=2)) || (currentDirection ==1) &&(currentValue <=2)){
//                        part1results_16_16[i] = 1;
//                    } else
//                        part1results_16_16[i] = 0;
//                }
//            }
//        }else if (mark ==0){ // camera moves tonthenleft
//            for (int i = 0; i<2040; i++){
//                if (!((i<=59) || (i>=1980) || (i%60 == 0) || (i%60 == 59))){
//                    double currentValue = motionVectorValueArray[i];
//                    byte currentDirection = motionVectorDirectionArray[i];
//                    if (((currentDirection ==1) || (currentDirection ==5)) && (currentValue >=2)){
//                        part1results_16_16[i] = 1;
//                    } else
//                        part1results_16_16[i] = 0;
//                }
//            }
//        } else{
//            for (int i = 0; i<2040; i++){
//                if (!((i<=59) || (i>=1980) || (i%60 == 0) || (i%60 == 59))){
//                    double currentValue = motionVectorValueArray[i];
//                    byte currentDirection = motionVectorDirectionArray[i];
//                    if ((currentDirection ==1) || (currentDirection ==5)){
//                        part1results_16_16[i] = 1;
//                    } else
//                        part1results_16_16[i] = 0;
//                }
//            }
//        }






//        else {
//            for (int i = 0; i<2040; i++){
//                double currentValue = motionVectorValueArray[i];
//                byte currentDirection = motionVectorDirectionArray[i];
//                if
//                        (((currentValue <= averageMotionVectorValue-2) || currentValue >= averageMotionVectorValue+2) //-2 , +2
//                        && (currentDirection ==1 || currentDirection ==5))
//
//
//                    part1results_16_16[i] = 1;
//                else
//                    part1results_16_16[i] = 0;
//            }
//        }








        // filter those blocks which are not adjacent to large macroblocks
        revisedPart1results_16_16 = part1results_16_16.clone();
        for (int j = 0; j<2040; j++){
            int sum1;
            if (part1results_16_16[j] == 1 && (!((j<=119) || (j>=1920) || (j%60 == 0) || (j%60 == 59)))){
                sum1 = part1results_16_16[j-120-1] + part1results_16_16[j-120] + part1results_16_16[j-120+1]
                        + part1results_16_16[j-60-1]  + part1results_16_16[j-60] + part1results_16_16[j-60+1]
                        + part1results_16_16[j-1]  + part1results_16_16[j]  + part1results_16_16[j+1]
                        + part1results_16_16[j+60-1]  + part1results_16_16[j+60] + part1results_16_16[j+60+1]
                        + part1results_16_16[j+120-1] + part1results_16_16[j+120] + part1results_16_16[j+120+1];
                if (sum1 < 4 || sum1 == 15)
                    revisedPart1results_16_16[j] = 0;
            }
        }
        part1results_16_16 = revisedPart1results_16_16.clone();
        for (int j = 0; j<2040; j++) {
            int sum1;
            if (part1results_16_16[j] == 1 && (!((j % 60 <= 5) || (j % 60 == 59)))) {
                sum1 = revisedPart1results_16_16[j - 4] + revisedPart1results_16_16[j - 3] + revisedPart1results_16_16[j - 2]
                        + revisedPart1results_16_16[j - 1] + revisedPart1results_16_16[j];
                if (sum1 == 5) {
                    part1results_16_16[j] = 0;
                }
            }
        }
        revisedPart1results_16_16=part1results_16_16.clone();
        for (int j = 0; j<2040; j++) {
            int sum1;
            if (part1results_16_16[j] == 1 && (!((j % 60 >=54) || (j % 60 == 0)))) {
                sum1 = part1results_16_16[j + 4] + part1results_16_16[j + 3] + part1results_16_16[j + 2]
                        + part1results_16_16[j + 1] + part1results_16_16[j];
                if (sum1 == 5) {
                    revisedPart1results_16_16[j] = 0;
                }
            }
        }










        return revisedPart1results_16_16;
    }


    public byte motionVectorDirection(int[] motionVector){

        double y = (motionVector[1] / 960) - (motionVector[0] / 960);
        double x = (motionVector[1] % 960) - (motionVector[0] % 960);
        byte motionVectorDirection=0;

        if (x == 0){
            if (y==0){
                motionVectorDirection=0;
            } else if(y>0){
                motionVectorDirection=3;
            } else{
                motionVectorDirection=7;
            }
        } else if (x>0){
            if (y>=-3 && y <=3){
                motionVectorDirection=1; // right   +/-1 room for error
            } else if(y>3){
                motionVectorDirection=2;
            } else if (y<-3){
                motionVectorDirection=8;
            }
        } else if (x<0){
            if(y>=-3 && y <=3){
                motionVectorDirection=5;  //left
            } else if(y>3){
                motionVectorDirection=4;
            } else if (y<-3){
                motionVectorDirection =6;
            }
        }
        return motionVectorDirection;
    }



    public void step1(String[] args){
        File file = new File(args[0]);
        byte[] bytes = new byte[pixelPerFrame3Channels];
        byte[] YChannel = new byte[pixelPerChannelPerFrame+ 4*960];
        byte[] previousYChannel = new byte[pixelPerChannelPerFrame+ 4*960];
        int count;
        byte[] oneBlock;
        int[] thisMotionVector;

        for (int i = YChannel.length-3840; i < YChannel.length; i++){
            YChannel[i] = 0;
        } // extra pixels below == 0

        try {
            InputStream is = new FileInputStream(file);
            int numberOfFrames = (int)(file.length())/ pixelPerFrame3Channels;
            for (int i = 0; i < numberOfFrames; i++) {  // i = i th of frames from time 0

                byte[] part1resultsin16_16 = new byte[2040];
                byte[] part1results = new byte[totalBlockNumber8_8];

                int offset = 0;
                int numRead = 0;
                while (offset < pixelPerFrame3Channels && (numRead = is.read(bytes, offset, pixelPerFrame3Channels - offset)) >= 0) {
                    offset += numRead;
                }

                int ind = 0;
                byte Y=0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        Y = (byte) ((bytes[ind]+bytes[ind + height * width]+bytes[ind + height * width * 2])/3);
                        //(r+g+b)* 0.333
                        YChannel[ind] = Y; //960*540
                        ind++;
                    }
                }

                if(i>0) {// start from the 2nd frame
                    double[] motionVectorValueArray = new double[2040];
                    byte[] motionVectorDirectionArray = new byte[2040];
                    for (int j = 0; j < 2040; j++) {//j == blockNo
                        if ((j<=59) || (j>=1980) || (j%60 == 0) || (j%60 == 59)){
                            //part1resultsin16_16[j] = 0;
                            // boundary conditions
                        } else {
                            count = 0; // count == number of foreground
                            oneBlock = oneBlock(YChannel, j);
                            thisMotionVector = motionVector(oneBlock, j, previousYChannel);
                            motionVectorValueArray[j] = motionVectorValue(thisMotionVector);
                            motionVectorDirectionArray[j] = motionVectorDirection(thisMotionVector);
                        }
                    }
                    part1resultsin16_16 = segmentation(motionVectorValueArray, motionVectorDirectionArray);

                }

                for (int q = 0; q<2040; q++){
                    byte currentValue = part1resultsin16_16[q];
                    int firstBlockNoFor8_8 = (q%60)*2 + (q/60)*240;
                    part1results[firstBlockNoFor8_8] = currentValue;
                    part1results[firstBlockNoFor8_8 + 1]= currentValue;
                    part1results[firstBlockNoFor8_8 + 120]= currentValue;
                    part1results[firstBlockNoFor8_8 + 121]= currentValue;
                }

                ground_flag.add(part1results);
                previousYChannel = YChannel.clone();

                //System.out.println("frame " + i + " / 362 is done");
            }

        } catch (FileNotFoundException a) {
            a.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        //test ji
        System.out.println("start grounding........");
        long startTimeg=System.currentTimeMillis();
        //

        myEncoder a = new myEncoder();
        a.step1(args);

        //test ji
        long endTimeg=System.currentTimeMillis();//记录结束时间
        float excTimeg=(float)(endTimeg-startTimeg)/1000;
        System.out.println("total time："+excTimeg+"s");
        //

        //test ji
        System.out.println("start dct........");
        long startTimet=System.currentTimeMillis();
        //

        MyDCT dct= new MyDCT();
        dct.step2(args,ground_flag);

        //test ji
        long endTimet=System.currentTimeMillis();//记录结束时间
        float excTimet=(float)(endTimet-startTimet)/1000;
        System.out.println("total time："+excTimet+"s");
        //
    }
}
