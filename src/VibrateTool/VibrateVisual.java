/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VibrateTool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.time.Clock;
import java.util.ArrayList;

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
        drawArrayLine(20, canvasHeight-20, canvasWidth-35, canvasHeight-20, g2);
        addRulers(false, 40, canvasWidth-35-10, canvasHeight-20, 1, 13);
        // draw y with rulers
        drawArrayLine(40, canvasHeight-10, 40, 10, g2);
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
          // the rely is the x axis
          for(int i=0; i<t; i++){
              text = increamentValue*i;
              length = -increamentLength*i;
              if(0 == i){
                  // only draw text
                  g2.drawString(""+text, rely-20, start+length-5);
              }else{
                  // draw text
                  g2.drawString(""+text, rely-20, start+length-5);
                  // draw mark
                  g2.drawLine(rely, start+length, rely-3, start+length);
              }
          }  
        }else{
            // the rely is the y axis
            for (int i=0; i<t; i++) {
                text = increamentValue * i;
                length = increamentLength * i;
                if (0 == i) {
                    // only draw text
                    g2.drawString("" + text, start+length-10, rely+15);
                } else {
                    // draw text
                    g2.drawString("" + text, start+length-10, rely+15);
                    // draw mark
                    g2.drawLine(start + length, rely, start + length, rely + 3);
                }
            }
        }
    }
    // draw wave of x, y, z
    private void drawXYZWave(){
        
    }
    
    // draw all the things
    public void drawVisualWave(){
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
    
    // test the function of fifo
    public static void main(String[] args){
        // test FIFO
        FIFO test = new FIFO();
        test.push(new DataNode(2,3,4));
        DataNode temp = test.read();
        System.out.println(temp.x+" "+temp.y+" "+temp.z);
    }
}

// create your own data structure
class DataNode{
    public int x = 0;
    public int y = 0;
    public int z = 0;

    // default
    public DataNode(){
        
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
    private int capacity = 12;
    private int readPointer = 0;
    private int bufferPointer = 0;
    private ArrayList<DataNode> buffer = new ArrayList<>();

    public void setCapacity(int capacity) {
        // record first
        this.capacity = capacity;
    }
    
    public void push(DataNode e){
        // shift left
        if(bufferPointer>=1)
            buffer = (ArrayList<DataNode>) buffer.subList(1, bufferPointer-1);
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
    public DataNode read(){
        // reset the pointer
        if(capacity == readPointer)
            readPointer = 0;
        
        // return 
        if((readPointer < bufferPointer) && (bufferPointer > 0)){
            return buffer.get(readPointer++);
        }
        else{
            return new DataNode(0,0,0);
        }
    }
}