/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VibrateTool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author smile
 */
public class VibrateVisual {
    // only only member
    private Graphics2D g2 = null;
    private int canvasWidth = 0;
    private int canvasHeight = 0;
    private final FIFO storage = new FIFO();
    
    // color use
    private final Color textColor = new Color(47, 136, 205);
    private final Color xWaveColor = Color.green;
    private final Color yWaveColor = Color.blue;
    private final Color zWaveColor = Color.red;
    
    // ruler use
    private int xStart = 0;
    private int xEnd = 0;
    private int yStart = 0;
    private int yEnd = 0;
    
    private int xRulerStart = 0;
    private int xRulerStop = 0;
    private int yRulerStart = 0;
    private int yRulerStop = 0;
    
    private int xIncreament = 0;
    private int xTurns = 0;
    private int yIncreament = 0;
    private int yTurns = 0;
    
    private int xMax = 0;
    private int yMax = 0;

    // here just wite a test use constructor
    public VibrateVisual(){
        // init the data first
        int len = storage.getCapacity();
        Random generator = new Random();
        DataNode test = null;
        int x,y,z;
        for(int i=0; i<len; i++){
//            x = generator.nextInt(60);
//            y = generator.nextInt(60);
//            z = generator.nextInt(60);
            x = 0;
            y = 0;
            z = 0;
            test = new DataNode(x, y, z);
            storage.push(test);
            //System.out.println(" "+x+" "+y+" "+z);
        }
    }
    
    // set g2
    public void setG2(Graphics2D g2) {
        this.g2 = g2;
    }

    public void setCanvasWidth(int canvasWidth) {
        this.canvasWidth = canvasWidth;
    }

    public void setCanvasHeight(int canvasHeight) {
        this.canvasHeight = canvasHeight;
    }
    
    public void setArea(int width, int height){
        setCanvasWidth(width);
        setCanvasHeight(height);
        // set ruler properties
        setRulerProperties();
    }
    
    private void setRulerProperties(){
        // set start and end
        xStart = 20;
        xEnd = canvasWidth-35;
        yStart = canvasHeight-10;
        yEnd = 10;
        
        // set others
        // in the add ruler method
        // the custom may define this
    }
    
    public void addData(int x, int y, int z){
        // create new node
        DataNode newNode = new DataNode();
        // set data
        newNode.x = x;
        newNode.y = y;
        newNode.z = z;
        // update queue
        storage.push(newNode);
    }
    
    // use to draw the coordinator
    /*
        @ simple like this
    
            gravity
        ^
        |
        |
        |
        |-----------------------------------> time fly
        |
        |
        |
    */
    private void drawCoordinator(){
        
        int deta = 100;
        // use g2 to draw this
        // set color first
        g2.setPaint(new Color(39, 136, 157));
        
        // draw x with rulers
        drawArrayLine(xStart, canvasHeight-20, xEnd, canvasHeight-20, g2);
        addRulers(false, 40, canvasWidth-35-10, canvasHeight-20, 1, 13);
        // draw y with rulers
        drawArrayLine(40, yStart, 40, yEnd, g2);
        addRulers(true, canvasHeight-20, 10+10, 40, 10, 60);
                 
        // draw axis label
        g2.setPaint(textColor);
        g2.drawString(" Gravity/g", 45, 15);
        g2.drawString(" Time/s", canvasWidth-50, canvasHeight-5);
        
        // draw wave laybels
        drawLabel(deta+55, 7, "x Axis",  xWaveColor, g2);
        drawLabel(deta+110, 7, "y Axis",  yWaveColor, g2);
        drawLabel(deta+165, 7, "z Axis",  zWaveColor, g2);
    }
    
    private void drawLabel(int x, int y, String text, Color color, Graphics2D g2){
        int deta = 10;
        
        // line color
        g2.setPaint(color);
        g2.fillRect(x, y, deta, deta);
        
        // text color
        g2.setPaint(textColor);
        g2.drawString(" "+text, x+deta, y+deta);
    }
    
    private void addRulers(boolean vertical,
            int start, int end, int rely,
            int deta, int maxValue) {
        // use black color
        //g2.setPaint(Color.black);
        // calculate the value
        int r = maxValue%deta;
        int t = maxValue/deta;   
        int increamentLength = (0==(Math.abs(start-end)))? Math.abs(start-end)/t : Math.abs(start-end)/t+1;
        int increamentValue = (0==r)? (maxValue/t) : (maxValue/t+1);
        int text = 0;
        int length = 0;
        
        // vertical or horizen
        if(vertical){
          // set ruler properties
          yTurns = t;
          yMax = increamentValue*t;
          yIncreament = increamentLength;
          yRulerStart = start;
          yRulerStop = end;
          // the rely is the x axis
          for(int i=0; i<yTurns; i++){
              text = increamentValue*i;
              length = -yIncreament*i;
              // draw text
              g2.drawString("" + text, rely - 20, start + length - 5);
              // draw mark
              if (0 != i) {
                  g2.drawLine(rely, start + length, rely - 3, start + length);
              }
          }  
        }else{
            // set ruler properties
            xTurns = t;
            xMax = increamentValue*t;
            xIncreament = increamentLength;
            xRulerStart = start;
            xRulerStop = end;
            // the rely is the y axis
            for (int i=0; i<xTurns; i++) {
                text = increamentValue * i;
                length = xIncreament * i;
                // draw text
                g2.drawString("" + text, start + length - 10, rely + 15);
                // draw mark
                if (0 != i) {
                    g2.drawLine(start + length, rely, start + length, rely + 3);
                }
            }
        }
    }
    // draw wave of x, y, z
    private void drawXYZWave(){
        // the length of square
        int deta = 4;
        // time line
        int time = 0;
        // capacity
        int len = storage.getCapacity();
        // preDataNode
        int pX = 0;
        int[] pY = new int[3];
        // current node
        DataNode cNode = null;
        
        int tData = 0;
        
        int cX = 0;
        int[] cY = new int[3];
        
        for(int i=0; i<len; i++){
            // read a data
            cNode = storage.read(i);
            // calculate x coordinator
            cX = xRulerStart+xIncreament*i-deta/2;
            // do not link just draw a point
            // use a squre is more good
            // use time as x
            // and data with y
            g2.setPaint(new Color(156, 77, 193));
            // filter the data
            cNode.x = cNode.x<=yMax? cNode.x : yMax;
            cNode.y = cNode.y<=yMax? cNode.y : yMax;
            cNode.z = cNode.z<=yMax? cNode.z : yMax;
            // first draw x axis
            tData = cNode.x;
            // use tData calculate y
            cY[0] = calculateYAxis(tData, deta);
            g2.fillRect(cX, cY[0], deta, deta);
            tData = cNode.y;
            // draw y axis
            cY[1] = calculateYAxis(tData, deta);
            g2.fillRect(cX, cY[1], deta, deta);
            tData = cNode.z;
            // draw z axis
            cY[2] = calculateYAxis(tData, deta);
            g2.fillRect(cX, cY[2], deta, deta);
            
            // draw link
            if(0 != i){
                int inc = deta/2;
                // draw x link
                g2.setPaint(xWaveColor);
                g2.drawLine(pX+inc, pY[0]+inc, cX+inc, cY[0]+inc);
                // draw y link
                g2.setPaint(yWaveColor);
                g2.drawLine(pX+inc, pY[1]+inc, cX+inc, cY[1]+inc);
                // draw z link
                g2.setPaint(zWaveColor);
                g2.drawLine(pX+inc, pY[2]+inc, cX+inc, cY[2]+inc);
            }
            
            // update pre node 
            pX = cX;
            System.arraycopy(cY, 0, pY, 0, 3);
            // update time
            time++;
        }
    }
    
    private int calculateYAxis(int t, int deta){
        int y = 0;
        y = yRulerStart-
                ((t/(yMax/yTurns))*yIncreament + 
                (t%(yMax/yTurns)*yMax)/Math.abs(yRulerStart-yRulerStop));    
        y -= deta/2;
        // return 
        return y;
    }
    
    // draw all the things
    public void drawVisualWave(int x, int y, int z){
        // update data
        addData(x, y, z);
        
        // draw coordinator first
        drawCoordinator();
        
        // draw wave
        drawXYZWave();
    }
    
    // draw array line
    private void drawArrayLine(
            int sx,
            int sy,
            int ex,
            int ey,
            Graphics2D g2) {

        double H = 10;
        double L = 4;
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        double awrad = Math.atan(L / H);
        double arraow_len = Math.sqrt(L * L + H * H);
        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        double x_3 = ex - arrXY_1[0]; // (x3,y3)
        double y_3 = ey - arrXY_1[1];
        double x_4 = ex - arrXY_2[0]; // (x4,y4)
        double y_4 = ey - arrXY_2[1];

        Double X3 = x_3;
        x3 = X3.intValue();
        Double Y3 = y_3;
        y3 = Y3.intValue();
        Double X4 = x_4;
        x4 = X4.intValue();
        Double Y4 = y_4;
        y4 = Y4.intValue();

        g2.drawLine(sx, sy, ex, ey);

        GeneralPath triangle = new GeneralPath();
        triangle.moveTo(ex, ey);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x4, y4);
        triangle.closePath();
        g2.fill(triangle);
        //g2.draw(triangle);  
    }

    // sum rotateVec
    private double[] rotateVec(
            int px,
            int py,
            double ang,
            boolean isChLen,
            double newLen) {

        double mathstr[] = new double[2];
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
    }
    
    // test the function of FIFO
    // test ok
    public static void main(String[] args) throws IOException{
        // test FIFO
//        FIFO test = new FIFO();
//        test.push(new DataNode(2,3,4));
//        test.push(new DataNode(2,3,4));
//        test.push(new DataNode(2,3,5));
//        DataNode temp = test.read(3);
//        System.out.println(temp.x+" "+temp.y+" "+temp.z);
        // test random integer
//        Random test = new Random();
//        for(int i=0; i<10; i++)
//        System.out.println(test.nextInt(60));
        // test memory
//        byte a =(byte)0xfd;
//        int b = 0;
//        if(a < 0){
//            b = (a&0x7F)|(1<<7);
//        }
//        System.out.println(b);
        // test toString method
        byte[] test = new byte[3];
        test[0] = (byte)0x00;
        test[1] = (byte)0x01;
        test[2] = (byte)0x02;
        //File f = new File("./src/cc.txt");
//        FileWriter Writer= new FileWriter(new File("./src/cc.txt"));
//        BufferedWriter out = new BufferedWriter(Writer);
//        out.write(Arrays.toString(test));
//        out.flush();
//        out.close();
        
        FileReader reader = new FileReader(new File("./src/cc.txt"));
        Scanner in = new Scanner(reader);
        System.out.println(in.nextInt());
        System.out.println(in.nextInt());
    }
}

// create your own data structure
class DataNode{
    public int x = 0;
    public int y = 0;
    public int z = 0;

    // default
    public DataNode(){
        this.x = 1;
        this.y = 2;
        this.z = 3;
    }
    // use for specific
    public DataNode(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

}

// data structure to save the data
class FIFO{
    // capacity
    private int capacity = 13;
    private int bufferPointer = 0;
    private ArrayList<DataNode> buffer = new ArrayList<>();

    public void setCapacity(int capacity) {
        // record first
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }
    
    public void push(DataNode e){
        // shift left
        if(bufferPointer>=capacity){
            ArrayList<DataNode> buffer_ = new ArrayList<>();
            // copy data first
            for(int i=0; i<capacity-1; i++){
                buffer_.add(buffer.get(i));
            }
            // redirection
            buffer = buffer_;
        }
        // push data
        buffer.add(e);
        // update buffer pointer
        if (bufferPointer < capacity) {
            bufferPointer++;
        }
    }
    
    public void pop(){
        
    }
    
    // this should auto shift from head to tail
    public DataNode read(int index){

        // return 
        if((index < bufferPointer) && (bufferPointer > 0)){
            return buffer.get(index);
        }
        else{
            return new DataNode(0,0,0);
        }
    }
}