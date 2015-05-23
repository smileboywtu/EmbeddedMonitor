/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BitMap;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/**
 *
 * @author smileboy
 */
public class BitMap {

    // private variable
    private final int width;
    private final int height;
    private final BitMapFileHeader bitMapFileHeader;
    private final BitMapInfoHeader bitMapInfoHeader;

    private final static int RGB16_MASK_RED = 0x0000_F800;
    private final static int RGB16_MASK_GREEN = 0x0000_07E0;
    private final static int RGB16_MASK_BLUE = 0x0000_001F;

    public BitMap(int nWidth, int nHeight) {
        this.width = nWidth;
        this.height = nHeight;
        bitMapFileHeader = new BitMapFileHeader(nWidth, nHeight);
        bitMapInfoHeader = new BitMapInfoHeader(nWidth, nHeight);
    }

    public void createBitMapFile(Path hex, Path bmp) throws FileNotFoundException, IOException {
        // open the hex file to read
        FileReader reader = new FileReader(hex.toString());
        // use scanner to read format file
        Scanner hexReader = new Scanner(reader);
        // open the new file and use flag
        FileOutputStream writer = new FileOutputStream(bmp.toString());
        // writer BitMapFileHeader
        writer.write(bitMapFileHeader.toBytes());
        // writer BitMapInfoHeader
        writer.write(bitMapInfoHeader.toBytes());
        // write RGB info
        int rgb16 = 0;
        byte redColor = 0;
        byte greenColor = 0;
        byte blueColor = 0;
        int counter = width * height;
        // read and write
        while (counter-- > 0) {
            rgb16 = hexReader.nextInt(16);
            rgb16 <<= 8;
            rgb16 |= hexReader.nextInt(16);
            // get division
            redColor = (byte) (((rgb16 & RGB16_MASK_RED)) >> 11);
            greenColor = (byte) (((rgb16 & RGB16_MASK_GREEN)) >> 5);
            blueColor = (byte) (((rgb16 & RGB16_MASK_BLUE)));
            // Convert RGB16 to RGB24
            redColor = (byte) ((redColor >> 2) | (redColor << 3));
            greenColor = (byte) ((greenColor >> 4) | (greenColor << 2));
            blueColor = (byte) ((blueColor >> 2) | (blueColor << 3));
            // write to new file
            writer.write(redColor);
            writer.write(greenColor);
            writer.write(blueColor);
        }
        // close file
        hexReader.close();
        reader.close();
        writer.close();
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
//    public static void main(String[] args) throws IOException {
//        // TODO code application logic here
//        BitMap createFile = new BitMap(320, 240, "realpicture.txt");
//        createFile.createBitMapFile();
//    }

}

class BitMapFileHeader {
    // Member Variable
    private final int bfSize;
    private final int bfOffSetBits;
    private final short bfType;
    private final short bfReserved1;
    private final short bfReserved2;

    public BitMapFileHeader(int nWidth, int nHeight) {
        // Init bfType
        bfType = 0x4D42;
        // Init reserved words
        bfReserved1 = 0;
        bfReserved2 = 0;
        // Init size and offset
        bfSize = 3 * nWidth * nHeight + 54;
        bfOffSetBits = 54;
    }

    public short getBfType() {
        return bfType;
    }

    public short getBfReserved1() {
        return bfReserved1;
    }

    public short getBfReserved2() {
        return bfReserved2;
    }

    public int getBfSize() {
        return bfSize;
    }

    public int getBfOffSetBits() {
        return bfOffSetBits;
    }

    private void write4Bytes2Stream(ByteArrayOutputStream o, int data) {
        // opposite order
        o.write(data & 0x000000FF);
        o.write((data & 0x0000FF00) >> 8);
        o.write((data & 0x00FF0000) >> 16);
        o.write((data & 0xFF000000) >> 24);
    }

    private void write2Bytes2Stream(ByteArrayOutputStream o, short data) {
        o.write(data & 0x00FF);
        o.write((data & 0xFF00) >> 8);
    }

    public byte[] toBytes() {
        // write in memory
        ByteArrayOutputStream bitMapFileHeader = new ByteArrayOutputStream(14);
        // write bfType
        write2Bytes2Stream(bitMapFileHeader, bfType);
        // write Size
        write4Bytes2Stream(bitMapFileHeader, bfSize);
        // wirte reserved
        write2Bytes2Stream(bitMapFileHeader, bfReserved1);
        write2Bytes2Stream(bitMapFileHeader, bfReserved2);
        // write offset
        write4Bytes2Stream(bitMapFileHeader, bfOffSetBits);
        // return
        return bitMapFileHeader.toByteArray();
    }
}

class BitMapInfoHeader {

    // Variable
    private final int biSize = 40;
    private final int width;
    private final int height;               // always be zero
    private final short biPlanes = 0;       // always be zero
    private final short biBitCount = 24;
    private final int biCompression = 0;
    private final int biSizeImage;
    private final int biXPelsPerMeter = 0;
    private final int biYPelsPerMeter = 0;
    private final int biClrUsed = 0;
    private final int biClrImportant = 0;
    private final int rowSize;

    public BitMapInfoHeader(int width, int height) {
        this.width = width;
        this.height = 0 - height;
        this.rowSize = ((this.biBitCount * this.width + 31) / 32) * 4;
        this.biSizeImage = this.rowSize * height;
    }

    public int getBiSize() {
        return biSize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public short getBiPlanes() {
        return biPlanes;
    }

    public short getBiBitCount() {
        return biBitCount;
    }

    public int getBiCompression() {
        return biCompression;
    }

    public int getBiSizeImage() {
        return biSizeImage;
    }

    public int getBiXPelsPerMeter() {
        return biXPelsPerMeter;
    }

    public int getBiYPelsPerMeter() {
        return biYPelsPerMeter;
    }

    public int getBiClrUsed() {
        return biClrUsed;
    }

    public int getBiClrImportant() {
        return biClrImportant;
    }

    private void write4Bytes2Stream(ByteArrayOutputStream o, int data) {
        // opposite order
        o.write(data & 0x000000FF);
        o.write((data & 0x0000FF00) >> 8);
        o.write((data & 0x00FF0000) >> 16);
        o.write((data & 0xFF000000) >> 24);
    }

    private void write2Bytes2Stream(ByteArrayOutputStream o, short data) {
        o.write(data & 0x00FF);
        o.write((data & 0xFF00) >> 8);
    }

    public byte[] toBytes() {
        ByteArrayOutputStream bitMapInfoHeader = new ByteArrayOutputStream(40);
        // write header size
        write4Bytes2Stream(bitMapInfoHeader, biSize);
        // write width
        write4Bytes2Stream(bitMapInfoHeader, width);
        // write height
        write4Bytes2Stream(bitMapInfoHeader, height);
        // write panels
        write2Bytes2Stream(bitMapInfoHeader, biPlanes);
        // write bitCounts
        write2Bytes2Stream(bitMapInfoHeader, biBitCount);
        // write compression
        write4Bytes2Stream(bitMapInfoHeader, biCompression);
        // write image size
        write4Bytes2Stream(bitMapInfoHeader, biSizeImage);
        // write x
        write4Bytes2Stream(bitMapInfoHeader, biXPelsPerMeter);
        // write y
        write4Bytes2Stream(bitMapInfoHeader, biYPelsPerMeter);
        // write color use
        write4Bytes2Stream(bitMapInfoHeader, biClrUsed);
        // write important color
        write4Bytes2Stream(bitMapInfoHeader, biClrImportant);
        // return
        return bitMapInfoHeader.toByteArray();
    }

}