/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * *****************************************************************************
 * @Reverse Time: 1. Add send start request command, and allow the pc program
 * start after the coordinator started, this is very flexiable for the user to
 * use the system. -- 2015/4/30 2. Add the topology and the address select mode
 * for the Wireless LCD system, this very really make the sytem a useful and
 * practical system. -- 2015/4/30 3. Here you shuld also add a status bar for
 * the user to check the information -- 2015/4/30 4. Add topology tree for the
 * program -- 2015/5/1 5. Add Device Node in the program and set he location of
 * the device use a "null" layout. -- 2015/5/1 6. when new node add to the
 * topology tree, just calculate the location and resize the coordinator with
 * the max negative x axis. -- 2015/5/1 7. the topology just hold the structure
 * of the network and do not init any swing component for the program --
 * 2015/5/1 8. the topology should hold the position information, this will make
 * the program more easier to find the real position, and do not need to
 * calculate is every time even though they are the same -- 2015/5/1 9. add
 * process coordinator information method, when the coordinator send a
 * information to the PC, we will analyse the message, if we get ropology
 * message, we will update the topology and resize the canvas. -- 2015/5/1 10.
 * Here make the clue clear: when the topology information has come, the event
 * dispatch thread only need to analyse the data, and a swing worker will be
 * used to add the new node in the topology tree, when inserted, the swing woker
 * will set the size of the canvas to make it bigger enough to hold all the
 * information in the scrollpane, and then notice the scrollpane it has
 * changged, and the canvas will repaint itself, and now all the nodes hava to
 * be draw using the drawTopology method. all things done, wait another topology
 * message to come -- 2015/5/1 11. Here I have get some problems when create the
 * graph, why can't I just draw a graph beyond the paint method -- 2015/5/1 12.
 * Create link between the device, you'd batter calculate the length before draw
 * the array line. -- 2015/5/1 13. Use state machine to receive the continous
 * message, especially for the message send one byt one and the PC can generate
 * each event for the serial port -- 2015/5/1 14. Add buffer size to the serial
 * port, to make the program batter -- 2015/5/2 15. Remove the buffer, here do
 * not need a buffer for this program -- 2015/5/2 16. can't deal with the
 * graphics repaint -- 2015/5/2 17. use two vector hold information to redraw the
 * the graphic  -- 2015/5/2 18. send the data use the real data address --2015/5/4
 * 19. Do not add the same node again and again -- 2015/5/6
 * @Auther: smile boy wtu
 */
package wirelesslcdsystem;

import BitMap.BitMap;
import VibrateTool.VibrateVisual;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 *
 * @author smileboy
 */
public class WirelessLCDSystem implements ActionListener,
        SerialPortEventListener {

    // OFFSET AND FIXED VALUE
    private final int SOF_VALUE = 0XFE;
    private final int SOF_OFFSET = 0;
    private final int LEN_OFFSET = 1;
    private final int CMD1_OFFSET = 2;
    private final int CMD2_OFFSET = 3;
    private final int DST1_OFFSET = 4;
    private final int DST2_OFFSET = 5;
    private final int DATA_OFFSET = 6;

    // LCD 
    private final int CLASS_CMD = 3;
    private final int SUBJECT_CMD = 2;
    private final int TEACHER_CMD = 4;
    private final int PEOPLE_CMD = 5;
    private final int TIME_CMD = 6;

    // ACK, START, TOPOLOGY
//    private final int TOPOLOGY_CMD = 0x0001;
//    private final int START_CMD = 0x0000;
//    private final int ACK_CMD   = 0xFFFF;
    // DEVICE TYPE
    private final int COORDINATOR = 0x0001;
    private final int ROUTER = 0x0002;
    private final int LCD = 0x0003;

    // Constants
    private final byte[] START_REQ = {
        (byte) 0xFE, (byte) 0x00,
        (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00,
        (byte) 0x00};

//    private final byte[] ACK = {
//        (byte) 0xFE, (byte) 0x00,
//        (byte) 0xFF, (byte) 0xFF,
//        (byte) 0x00, (byte) 0x00,
//        (byte) 0x00};

    // Command
    private final String STARTCOMMAND = "START";
    private final String SENDCOMMAND = "SEND";
    private final String STOPCOMMAND = "STOP";

    // TextField
    private JTextField classTextField;
    private JTextField subjectTextField;
    private JTextField teacherTextField;
    private JTextField peopleTextField;
    private JFormattedTextField timeTextField;

    // control buttons
    private JButton start;
    private JButton send;
    private JButton stop;

    // handler for topology and message status bar
    private JPanel canvas = null;
    private JTextArea message = null;

    // Value save
    private String className;
    private String subjectName;
    private String teacherName;
    private String peopleNumber;
    private String timeDuration;

    // RS232 controls
    private JComboBox comList;
    private JComboBox baudrateList;
    private JComboBox addrList;

    // RS232 handler
    private SerialPort serialPort;
    private CommPortIdentifier portIdentifier;
    
    // Thread for read the serial
    // should be initialized when serial is opened
    private Thread serialReadThread = null;
    // variables for state machine
    private int fcs = 0;
    private int length = 0;
    private int recvLen = 0;
    private byte[] incomingFrameBuffer = null;
    private int state = 1;

    // files to read and write
    private int counter = 0;
    private OutputStream fileWriter = null;
    private int tempFilePointer = 1;    // this means use buffer 1
    private final Path colorHex1 = Paths.get("./src/image/colorHex1.tmp");
    private final Path colorHex2 = Paths.get("./src/image/colorHex2.tmp");
    private final Path bitMapFile = Paths.get("./src/image/smileboy.bmp");
    
    // params for the bmp file
    private final int imageHeight = 240;
    private final int imageWidth  = 320;
    private final int colorTotalBytes = 153600;

    // Buffer to save the data
    private InputStream serialIn = null;
    private OutputStream serialOut = null;

    // counter for receive and send
    private static long rxCounter = 0;
    private static long txCounter = 0;

    // Large Buffer use to store the bytes
    private ByteArrayOutputStream buffer = null;
    
    // Buffer for the ADXL345
    private final byte[] ADXL345 = new byte[6];
    
    // pane for vibrate curve
    private JPanel curve = null;
    private final VibrateVisual visualTool = new VibrateVisual();
    
    // pane for picture
    private JPanel image = null;

    // save the topology
    private final TopologyTree topology = new TopologyTree();

    // comport list
    private final String comListStr[] = {
        "COM1",
        "COM2",
        "COM3",
        "COM4",
        "COM5"};

    private final String baudrateListStr[] = {
        "115200",
        "38400"
    };

    private final ArrayList<String> defaultAddrList = new ArrayList<>();
    
    public WirelessLCDSystem(){
        // only for test
        // test ok
//        Random generator = new Random();
//        ADXL345[0] = (byte)generator.nextInt(60);
//        ADXL345[1] = 0x00;
//        ADXL345[2] = (byte)generator.nextInt(60);
//        ADXL345[3] = 0x00;
//        ADXL345[4] = (byte)generator.nextInt(60);
//        ADXL345[5] = 0x00;
    }

    public JPanel createPane() {
        // create top-level pane
        JPanel topLevelPane = new JPanel(new BorderLayout());

        // repose the pane
        JPanel layout1 = new JPanel(new BorderLayout());
        layout1.add(createLCDControlPane(), BorderLayout.PAGE_START);
        layout1.add(createMessagePane(), BorderLayout.PAGE_END);
        // repose the pane
        JPanel layout2 = new JPanel(new BorderLayout());
        layout2.add(createPicturePane(), BorderLayout.PAGE_START);
        layout2.add(createADXL345Pane(), BorderLayout.PAGE_END);
        
        // add the lcd and the log pane
        topLevelPane.add(layout1, BorderLayout.LINE_START);

        // add the topology pane
        topLevelPane.add(createTopologyPane(), BorderLayout.CENTER);
        
        // add the picture and adxl345 pane
        topLevelPane.add(layout2, BorderLayout.LINE_END);

        return topLevelPane;
    }

    private JPanel createPicturePane(){
        // create a contaner
        image = new ImagePane();
        // size the panel
        Dimension area = new Dimension(350, 270);
        image.setPreferredSize(area);
        // custom the background color
        image.setBackground(new Color(223, 239, 239));
        
        // content
        JPanel content = new JPanel();
        content.add(image);
        
        // set a border for it
        content.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("OV7670 Image"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        // return 
        return content;
    }
    
    private JPanel createADXL345Pane(){
        // create adxl345 pane
        curve = new CurvePane();
        // custom the back ground
        //curve.setBackground(new Color(97, 71, 222));
        curve.setBackground(Color.white);
        // set area
        Dimension area = new Dimension(350, 200);
        curve.setPreferredSize(area);
        
        // set the tool properties
        visualTool.setArea(350, 200);
        
        JPanel content = new JPanel();
        // add to the content
        content.add(curve);
        
        // set a border for it
        content.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("ADXL345 Curve"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        // returnt the pane
        return content;
    }
    
    private JScrollPane createTopologyPane() {
        // Create instance of canvas
        canvas = new DrawPane();
        // you must use null layout for absolute location
        canvas.setLayout(null);
        canvas.setBackground(Color.gray);
        //canvas.addPropertyChangeListener(this);

        // Put the canvas in the scroll pane
        JScrollPane scrollCanvas = new JScrollPane(canvas);
        scrollCanvas.setAutoscrolls(true);
        scrollCanvas.setWheelScrollingEnabled(true);

        // set the size before you can scroll
        scrollCanvas.setPreferredSize(new Dimension(500, 500));

        // add a border for the topology canvas
        scrollCanvas.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Topology"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // return
        return scrollCanvas;
    }

    private JScrollPane createMessagePane() {
        // Create instance for the member message
        message = new JTextArea(5, 25);
        message.setEditable(false);
        message.setBackground(new Color(223, 231, 189));

        // Create scroll pane container for the text area
        JScrollPane statusBar = new JScrollPane(message);
        statusBar.setAutoscrolls(true);
        statusBar.setWheelScrollingEnabled(true);

        // add a border for scroll bar
        statusBar.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Message"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // set the preferred size
        statusBar.setPreferredSize(new Dimension(300, 280));

        // return
        return statusBar;
    }

    private JPanel createLCDControlPane() {
        // Create RS232 comport and BaudRate
        comList = new JComboBox(comListStr);
        comList.setSelectedIndex(0);
        comList.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        baudrateList = new JComboBox(baudrateListStr);
        baudrateList.setSelectedIndex(0);
        baudrateList.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // Create address list 
        addrList = new JComboBox();
        // default addr
        addrList.addItem("None");
        addrList.setSelectedIndex(0);
        addrList.setEnabled(false);
        addrList.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // Create Control Pane
        JPanel RS232ControlPane = new JPanel(new GridLayout(1, 2));
        RS232ControlPane.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("RS232 Controls"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        RS232ControlPane.add(comList);
        RS232ControlPane.add(baudrateList);

        // Create class name field
        classTextField = new JTextField("size less than 10", 10);

        // Create subject name field
        subjectTextField = new JTextField("size less than 10", 10);

        // Create teacher field
        teacherTextField = new JTextField("size less than 5", 10);

        // Create people text field
        peopleTextField = new JTextField("size less than 3", 10);

        // Create time text field
        SimpleDateFormat timeDuringFormat = new SimpleDateFormat("hh:mm '-' hh:mm");
        timeTextField = new JFormattedTextField();
        timeTextField.setValue(timeDuringFormat.format(new Date()));

        // Add to label to each of them
        JLabel classLabel = new JLabel("Class: ");
        classLabel.setLabelFor(classTextField);

        JLabel subjectLabel = new JLabel("Subject: ");
        subjectLabel.setLabelFor(subjectTextField);

        JLabel teacherLabel = new JLabel("Teacher: ");
        teacherLabel.setLabelFor(teacherTextField);

        JLabel peopleLabel = new JLabel("People: ");
        peopleLabel.setLabelFor(peopleTextField);

        JLabel timeLabel = new JLabel("Time: ");
        timeLabel.setLabelFor(timeTextField);

        //Lay out the text controls and the labels.
        JPanel textControlsPane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();

        textControlsPane.setLayout(gridbag);
        JLabel[] labels = {
            classLabel,
            subjectLabel,
            teacherLabel,
            peopleLabel,
            timeLabel};
        JTextField[] textFields = {
            classTextField,
            subjectTextField,
            teacherTextField,
            peopleTextField,
            timeTextField};
        addLabelTextRows(labels, textFields, gridbag, textControlsPane);
        textControlsPane.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("LCD Contents"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Create Buttons and open the UART
        start = new JButton("start");
        start.setActionCommand(STARTCOMMAND);
        start.addActionListener(this);

        send = new JButton("send");
        send.setActionCommand(SENDCOMMAND);
        send.addActionListener(this);

        stop = new JButton("stop");
        stop.setActionCommand(STOPCOMMAND);
        stop.addActionListener(this);

        JPanel buttonPane = new JPanel();
        BoxLayout box = new BoxLayout(buttonPane, BoxLayout.X_AXIS);

        buttonPane.add(start);
        buttonPane.add(send);
        buttonPane.add(addrList);
        buttonPane.add(stop);

        // set border for button pane
        buttonPane.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Control"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Here Do not enable the button
        // send and stop
        send.setEnabled(false);
        stop.setEnabled(false);

        // Create container
        JPanel container = new JPanel(new BorderLayout());
        container.add(RS232ControlPane, BorderLayout.PAGE_START);
        container.add(textControlsPane, BorderLayout.CENTER);
        container.add(buttonPane, BorderLayout.PAGE_END);

        // return it
        return container;
    }

    private void addLabelTextRows(JLabel[] labels,
            JTextField[] textFields,
            GridBagLayout gridbag,
            Container container) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        int numLabels = labels.length;

        for (int i = 0; i < numLabels; i++) {
            c.gridwidth = GridBagConstraints.RELATIVE;   //next-to-last
            c.fill = GridBagConstraints.CENTER;          //reset to default
            c.weightx = 0.0;                             //reset to default
            container.add(labels[i], c);

            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            container.add(textFields[i], c);
        }
    }

    private void buildAndSendFrame(int command, String addr,  byte[] data, int len) throws IOException {

        // Fill SOF
        buffer = new ByteArrayOutputStream(len + 7);
        buffer.write(SOF_VALUE);

        // Fill the len and CMD
        buffer.write(len);
        buffer.write(UINT32_LOW(command));
        buffer.write(UINT32_HIGH(command));

        // Fill the dst address, cause here we do not know the address present 
        // so just use 0x0000 for present
        // not just change this, use the real addr to transfer data
        // Demo for the String : 0xFE3D
        // first subString the String  
        addr = addr.substring(2);
        // then get the bytes
        byte[] addrBytes = new byte[2];
        addrBytes = addr.getBytes("GB2312");
        buffer.write(addrBytes, 1, 1);
        buffer.write(addrBytes, 0, 1);

        // Fill the data
        buffer.write(data, 0, len);

        // Fill FCS
        buffer.write(calcuFCS(buffer.toByteArray(), len + 5));

        // use the output stream to send the buffer
        serialOut.write(buffer.toByteArray());

        // updata the txCounter
        txCounter += len + 7;

        // for test
        //System.out.println(Arrays.toString(buffer.toByteArray()));
        message.append("[ " + getHexString(buffer.toByteArray()) + "]");
        message.append("\n");

        // show use tx
        //System.out.println("You hava send: " + txCounter);
        message.append("You hava send: " + txCounter);
        message.append("\n");

    }

    private void startRS232Com() throws NoSuchPortException,
            PortInUseException,
            UnsupportedCommOperationException,
            IOException,
            TooManyListenersException {
        // get the port identifer
        String portName = comListStr[comList.getSelectedIndex()];
        portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            // Wait for 2ms when the COM is Busy
            String baudrate = baudrateListStr[baudrateList.getSelectedIndex()];
            CommPort comPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (comPort instanceof SerialPort) {
                serialPort = (SerialPort) comPort;
                // set the notify on data available
                serialPort.addEventListener(this);
                // use state machine here
                // so do not use the interrupt now
                // first time must open this notify
                serialPort.notifyOnDataAvailable(true); 
                // set params
                serialPort.setSerialPortParams(
                        Integer.parseInt(baudrate),
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                // Do not use flow control
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                // set the out/in stream
                serialIn = serialPort.getInputStream();
                serialOut = serialPort.getOutputStream();
                
                // initialize the read thread here
                // do not need initialize the thread here
                // first time do not initialize this thread
                //serialReadThread = new Thread(new SerialReader(serialIn));
                
            } else {
                System.out.println(
                        "Error: Only serial ports are handled by this example.");
            }
        }
    }

    private void stopRS232Com() throws InterruptedException, IOException {
        // close stream first
        serialIn.close();
        serialOut.close();
        
        // set to null
        serialIn = null;
        serialOut = null;

        // close it
        serialPort.close();

        // Remove the event listenner
        serialPort.removeEventListener();
    }

    // Tools for this program
    private byte UINT32_LOW(int value) {
        return (byte) (value & 0xFF);
    }

    private byte UINT32_HIGH(int value) {
        return (byte) ((value >> 8) & 0xFF);
    }

    private short BUILD_UINT16(byte low, byte high) {
        short x = 0;
        x |= high;
        x <<= 8;
        x |= low;
        // return
        return x;
    }

    private int BUILD_UINT32(byte low, byte high) {
        int x = 0;
        int a = 0;
        int b = 0;
        // filter the data
        if(low < 0){
            a = ((low&0x7F)|(1<<7));
        }
        if(high < 0){
            b = (high&0x7F)|(1<<7);
        }
        // write high byte
        if(0 != b){
            x |= b;
        }else{
            x |= high;
        }
        // shift left
        x <<= 8;
        // write low byte
        if(0 != a){
            x |= a;
        }else{
            x |= low;
        }
        // return
        return x;
    }

    private byte calcuFCS(byte[] val, int len) {
        byte xorResult = 0x00;
        // ignore the sof bit
        while (len > 0) {
            xorResult ^= (byte) val[len];
            len--;
        }
        return xorResult;
    }

    private String getHexString(byte[] pre) {
        String result = "";
        for (int i = 0; i < pre.length; i++) {
            result += "0x"
                    + (Integer.toString((pre[i] & 0xff) + 0x100, 16)).toUpperCase().substring(1)
                    + " ";
        }
        return result;
    }
    
    // use a thread to read data
    class SerialReader implements Runnable{
        // frame type
        private final int TOPOLOGY_CMD = 1;
        private final int CAMERA_START_CMD = 7;
        private final int CAMERA_DATA_CMD = 8;
        private final int VIBRATE_DATA_CMD = 9;
        private final int COORDINATOR_ACK_CMD = 0xFFFF;
        
        // params
        private final int SOF_STATE = 1;
        private final int LEN_STATE = 2;
        private final int CMD1_STATE = 3;
        private final int CMD2_STATE = 4;
        private final int DST1_STATE = 5;
        private final int DST2_STATE = 6;
        private final int DATA_STATE = 7;
        private final int FCS_STATE = 8;
        // constants
        private final int SOF_VALUE = (int)0xFE;
        
        // only one member
        InputStream serialReader = null;
        
        SerialReader(InputStream in){
            serialReader = in;
        }

        @Override
        public void run() {
            try {
                int ch = 0;
                // read and dispath the event
                while(serialReader.available()>0){
                    // here we use state machine to read the data
                    // first to read a data
                    ch = serialReader.read();
                    // check state
                    switch(state){
                        case SOF_STATE:
                            if(SOF_VALUE == ch){
                                // step forward
                                state = LEN_STATE;
                            }
                        break;
                        // check the length
                        case LEN_STATE:
                            // get the data
                            incomingFrameBuffer = new byte[ch+5];
                            // record data, this used for the data check and save
                            length = ch;
                            // reset the receive data length counter
                            recvLen = 0;
                            // this is safe to convert, cause ch is between 0 to 255
                            // here the ch is much less than 128, if acquired correctly
                            incomingFrameBuffer[0] = (byte)ch; 
                            // step to next
                            state = CMD1_STATE;
                        break;   
                        case CMD1_STATE:
                            // save data and to next state
                            // safe to convert
                            incomingFrameBuffer[1] = (byte)ch;
                            state = CMD2_STATE;
                        break;
                        case CMD2_STATE:
                            // safe to convert 
                            // the ch here must be 0x00
                            incomingFrameBuffer[2] = (byte)ch;
                            state = DST1_STATE;
                        break;
                        case DST1_STATE:
                            incomingFrameBuffer[3] = (byte)ch;
                            state = DST2_STATE;
                        break;
                        case DST2_STATE:
                            incomingFrameBuffer[4] = (byte)ch;
                            if(0 == length){
                                // just go fcs state
                                // cause there is no data needed to receive
                                state = FCS_STATE;
                            }else{
                                state = DATA_STATE;
                            }
                        break;
                        case DATA_STATE:
                            // record the current one
                            incomingFrameBuffer[5+recvLen++] = (byte)ch;
                            // read more if enough
                            int available = serialReader.available();
                            // read part or read all data
                            if((length-recvLen) <= available){
                                // read all data
                                serialReader.read(incomingFrameBuffer, 5+recvLen, length-recvLen);
                                // step to the fcs state
                                state = FCS_STATE;
                            }else{
                                // no enough data to read all
                                // just read part and wait another turn to read
                                serialReader.read(incomingFrameBuffer, 5+recvLen, available);
                                // set the current data counter
                                recvLen += available;
                            }
                        break;
                        case FCS_STATE:
                            fcs = ch;
                            // calculate fcs and go forther
                            if(ch == (calculateFCS(incomingFrameBuffer, length+5))){
                                // check ok but with different cmd
                                pendingIncomingFrame(incomingFrameBuffer, true);
                            }else{
                                // bad frame
                                pendingIncomingFrame(incomingFrameBuffer, false);
                            }
                            // here you should free the data
                            // whereever the data is good or demaged
                            // reset the step
                            state = SOF_STATE;
                            incomingFrameBuffer = null;
                        break;
                    }// end switch
                }// end while
                
                // open the notify
                serialPort.notifyOnDataAvailable(true);
            } catch (IOException ex) {
                Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }// end run
        
        // calculate fcd
        private byte calculateFCS(byte[] val, int len) {
            byte xorResult = 0x00;
            // ignore the sof bit
            for(int i=0; i<len; i++){
                xorResult ^= (byte) val[i];
            }
            return xorResult;
        }
        
        // deal with the frame coming in
        private void pendingIncomingFrame(byte[] data, boolean flag) throws FileNotFoundException, IOException{
            // get the command
            int command = BUILD_UINT32(data[1], data[2]);
            // pend
            switch(command){
                case TOPOLOGY_CMD:
                    // just ignore this frame now
                    // so just return
                    // TO-DO here
                    // update the topology
                    if(flag){
                        // update the topology
                        // update the lcd address list
                        ProcessIncomingMessage(data, 0);

                        // update message
                        message.append("new node add to the network\n");
                    }else{
                        // nothing to do with demaged data
                    }
                break;
                case CAMERA_START_CMD:
                    if(flag){
                        // open the file and delete all data
                        // two temp file buffer
                        if(1 == tempFilePointer){
                            fileWriter = Files.newOutputStream(colorHex1,
                                    StandardOpenOption.APPEND,
                                    StandardOpenOption.CREATE_NEW,
                                    StandardOpenOption.TRUNCATE_EXISTING);
                        }else{
                            fileWriter = Files.newOutputStream(colorHex2,
                                    StandardOpenOption.APPEND,
                                    StandardOpenOption.CREATE_NEW,
                                    StandardOpenOption.TRUNCATE_EXISTING);
                        }
                        // update message
                        message.append("start to receive new image...\n");
                    }else{
                        // do nothing
                        
                        // update message
                        message.append("waiting start signal of new image...\n");
                    }
                break;
                case CAMERA_DATA_CMD:
                    if(flag){
                       // append to the file 
                       // check if we have received 153600 bytes 
                       // if not, just receive bytes and write to colorHex file
                       if(colorTotalBytes != counter){
                           // the content start at pointer 5
                           // the length of the content saved in data[0]
                           fileWriter.write(data, 5, data[0]);
                           // update the counter
                           // data[0] must be a number between 0 ~ 255
                           counter += (int)data[0];
                       }
                       else{
                           // close the hex file writer
                           fileWriter.close();
                           // counter reset
                           counter = 0;
                           // pack the hex and make a bmp file
                           if (1 == tempFilePointer) {
                               // pack buffer 1
                               // here use thread
                               (new Runnable() {
                                   @Override
                                   public void run() {
                                       try {
                                           packAndBuildBmpFile(colorHex1, bitMapFile);
                                       } catch (IOException ex) {
                                           Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
                                       }
                                   }
                               }).run();
                               // notify the program to use buffer 2
                               tempFilePointer = 2;
                           } else {
                               // pack buffer 2
                               // use thread here
                               (new Runnable() {
                                   @Override
                                   public void run() {
                                       try {
                                           packAndBuildBmpFile(colorHex2, bitMapFile);
                                       } catch (IOException ex) {
                                           Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
                                       }
                                   }
                               }).run();
                               // notify the program to use buffer 1
                               tempFilePointer = 1;
                           }
                       }
                    }else{
                       // destory the file, wait another start signal
                        if (1 == tempFilePointer) {
                            if (Files.exists(colorHex1)) {
                                Files.delete(colorHex1);
                            }
                        } else {
                            if (Files.exists(colorHex2)) {
                                Files.delete(colorHex2);
                            }
                        }
                        message.append("discard previous image data\n");
                    }
                break;
                case VIBRATE_DATA_CMD:
                    // just ignore here
                    // update the data buffer is ok
                    if(flag){
                        // update buffer
                        for(int i=5; i<11; i++){
                            // only copy 6 bytes
                            ADXL345[i-5] = data[i];
                        }
                    }else{
                        // do nothing here
                    }
                    message.append("receive adxl345 data\n");
                break;
                case COORDINATOR_ACK_CMD:
                    // this should be the start signal for the application
                    if(flag){
                        // this usually work 
                        // at the start of the application
                        start.setEnabled(false);
                        send.setEnabled(true);
                        stop.setEnabled(true);

                        // list enable
                        addrList.setEnabled(true);
                        comList.setEditable(false);
                        baudrateList.setEditable(false);
                        // update message
                        message.append("Coordinator ACK\n");
                    }
                break;
                default:
                break;
            }// end switch
            
            // update byte counter
            rxCounter += (int)data[0]+7;
            // update message
            message.append("You have received: " + rxCounter + "\n");
        }// end method   
        
        private void packAndBuildBmpFile(Path hex, Path bmp) throws IOException{
             // use bit map to do this
             BitMap createFile = new BitMap(imageWidth, imageHeight);
             createFile.createBitMapFile(hex, bmp);
             
             // when done call method to repaint the pane
             image.repaint();
        }// end methods
        
    }// end class
    
    // listen for the rs232 port
    @Override
    public void serialEvent(SerialPortEvent spe) {
        if (SerialPortEvent.DATA_AVAILABLE == spe.getEventType()) {
            // avoid the high frequency of the notifying
            // just check the read thread first 
            // and then choose to restart or ignore the notity
            if((null != serialReadThread) && serialReadThread.isAlive()){
                // do nothing here
                // or you can close the notify here
                // and open when the thread is done
                serialPort.notifyOnDataAvailable(false);
            }else{
                // you must re-run the thread
                // here you need only one method but using thread.start() with two
                // same thread
                // here you can use start, because we already know that the 
                // thread is dead
                // here you can't use the start method again 
                // the data is already exist,
                // so just use run to make it run again
                //srialReadThread.start();
                // you can never restart a thread again
                
                serialReadThread = new Thread(new SerialReader(serialIn)); 
                // start it
                serialReadThread.start();
            }
            /*try {
                // receive two kind of data frastructure,
                // so just make the thing easier
                int len = serialIn.available();
                byte[] MessageBuffer = new byte[len];
                // read data out
                serialIn.read(MessageBuffer, 0, len);
                // update Counter
                rxCounter += len;

                // size > 7 ACK + Topology
                if (len <= 7) {     // ACK
                    if (true == Arrays.equals(ACK, MessageBuffer)) {
                        // update message
                        message.append("Coordinator ACK\n");
                    }
                } else if( len <= 11 ){ // TOPOLOGY
                    // topology send by other nodes
                    ProcessIncomingMessage(MessageBuffer, 0);
                }else if( len > 11 ){   // ACK + TOPOLOGY
                    // enable button
                    start.setEnabled(false);
                    send.setEnabled(true);
                    stop.setEnabled(true);

                    // list enable
                    addrList.setEnabled(true);
                    comList.setEditable(false);
                    baudrateList.setEditable(false);

                    // update message
                    message.append("Coordinator Start\n");

                    // get out of the first 7 byte
                    ProcessIncomingMessage(MessageBuffer, 7);
                }
                // update message
                message.append("You have received: " + rxCounter + "\n");
            } catch (IOException ex) {
                Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
        }// end if
    }

    // process incoming message
    private void ProcessIncomingMessage(byte[] pData, int preOffSet) {

        // deal with the cluster
        String srcAddr = "";
        String parentAddr = "";
        String device = "";
        int deviceCMD = 0;

        // build source address
        srcAddr += (Integer.toString((pData[preOffSet+5] & 0xFF) + 0x100, 16)).toUpperCase().substring(1);
        srcAddr += (Integer.toString((pData[preOffSet+4] & 0xFF) + 0x100, 16)).toUpperCase().substring(1);
        srcAddr = "0x" + srcAddr;

        // build device 
        deviceCMD = BUILD_UINT32(pData[preOffSet+6], pData[preOffSet+7]);

        // huild parent addr
        parentAddr += (Integer.toString((pData[preOffSet+9] & 0xFF) + 0x100, 16)).toUpperCase().substring(1);
        parentAddr += (Integer.toString((pData[preOffSet+8] & 0xFF) + 0x100, 16)).toUpperCase().substring(1);
        parentAddr = "0x" + parentAddr;

        // use the cmd get the device name
        switch (deviceCMD) {
            case COORDINATOR:
                device = "Coord";
                break;
            case ROUTER:
                device = "Router";
                break;
            case LCD:
                device = "LCD";
                break;
        }

        // add the node to the topology
        // this method will use much time, so here you need to use a swing 
        // worker to do this
        //topology.addNode(parentAddr, srcAddr, device);
        (new UpdateTopology(parentAddr, srcAddr, device)).execute();

        // update the address list
        if (device.equals("LCD")) {
            if(false == defaultAddrList.contains(srcAddr)){
                defaultAddrList.add(srcAddr);
                // show message to the user
                message.append(srcAddr + " join the network" + "\n");
            }
            addrList.removeAllItems();
            // if the address already in the list then exit
            defaultAddrList.stream().forEach((i) -> {
                addrList.addItem(i);
            });
        }
    }

    // listen for the buttons
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        // Deal with the command
        switch (command) {
            // start
            case STARTCOMMAND:
                try {
                    startRS232Com();
                    // send start request to coordinator
                    // here is the first place to send the data
                    serialOut.write(START_REQ, 0, START_REQ.length);

                    // disable the start button
                    start.setEnabled(false);
                    // here you should enable the stop button for 
                    // user to exit
                    stop.setEnabled(true);

                    // enable or disable the combobox
                    comList.setEnabled(false);
                    baudrateList.setEnabled(false);

                    // clear the message log
                    message.setText("");
                } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException | TooManyListenersException ex) {
                    Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            // send 
            case SENDCOMMAND:
                // return immediately
                (new SendAction()).execute();
                break;
            // stop   
            case STOPCOMMAND:
                // close the rs232
                try {
                    stopRS232Com();
                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                    
                // disable the buttons
                start.setEnabled(true);
                send.setEnabled(false);
                stop.setEnabled(false);

                // enable or disable the combobox
                addrList.setEnabled(false);
                comList.setEnabled(true);
                baudrateList.setEnabled(true);
                
                // clear the topology information
                defaultAddrList.removeAll(defaultAddrList);
                topology.deviceVector.removeAllElements();
                topology.linkVector.removeAllElements();
                break;
        }
    }

    // draw the image 
    class ImagePane extends JPanel{
        
        private BufferedImage handler = null;
        
        @Override
        protected void paintComponent(Graphics g) {
            // inherited from JPanel
            super.paintComponent(g);
            try {
                // read the image
                handler = ImageIO.read(new File(bitMapFile.toUri()));
                // draw the image
                g.drawImage(handler, 15, 15, null);

            } catch (IOException ex) {
                Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    // Curve pane
    class CurvePane extends JPanel{

        @Override
        protected void paintComponent(Graphics g) {
            // inherited from JPanel
            super.paintComponent(g);
            // just use the data int the ADXL345 to draw the curve
            drawCurve(g);
        }
        
        private void drawCurve(Graphics g){
            // set draw handler
            visualTool.setG2((Graphics2D)g);
            // parse the data
            int x = 0;
            int y = 0;
            int z = 0;
            x = BUILD_UINT32(ADXL345[0], ADXL345[1]);
            y = BUILD_UINT32(ADXL345[2], ADXL345[3]);
            z = BUILD_UINT32(ADXL345[4], ADXL345[5]);
            // use vibrate visual tool to draw this
            visualTool.drawVisualWave(x, y, z);
        }// end method
        
    }// end class
    
    // extend the JPanel
    class DrawPane extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            // inherited from JPanel
            super.paintComponent(g);

            // draw my component
            // read the tree structure, then draw nodes and the link
            // all network information is saved in the topology
            ArrayLine l = null;
            TopologyElement p = null;
            if (null != topology.root) {
                for (int i = 0; i < topology.deviceVector.size(); i++) {
                    // get
                    p = topology.deviceVector.elementAt(i);
                    // deal
                    if (null != p) {
                        // draw area
                        p.showElement(g);
                    }// end if
                }// end for
                for (int i = 0; i < topology.linkVector.size(); i++) {
                    // get
                    l = topology.linkVector.elementAt(i);
                    if (null != l) {
                        l.showArrayLine((Graphics2D) g);
                    }
                }
            }// end if
        }
    }

    // here use the swingworker to deal with the incoming message
    // when the message is topology message, we will just update the 
    // the topology and canvas size
    class UpdateTopology extends SwingWorker<Void, Void> {

        private String parentAddr = "";
        private String srcAddr = "";
        private String device = "";

        public UpdateTopology(String parentAddr, String srcAddr, String device) {
            this.parentAddr = parentAddr;
            this.srcAddr = srcAddr;
            this.device = device;
        }

        @Override
        protected Void doInBackground() throws Exception {
            topology.addNode(parentAddr, srcAddr, device);
            // return
            return null;
        }

        @Override
        protected void done() {
            // notify the canvas to repaint
            // resize the canvas
            int width = topology.maxPositiveXAxis - topology.maxNegativeXAxis;
            int height = topology.maxPositiveYAxis - topology.maxNegaticeYAxis;

            // set size
            canvas.setPreferredSize(new Dimension(width, height));

            //Let the scroll pane know to update itself
            //and its scrollbars.
            canvas.revalidate();

            // repaint
            canvas.repaint();

            // add notice here
            // the awt will also use a thread to update the pane
            // so here we do not need to care about that, so just let
            // the awt to deal with it automatically
        }
    }

    // for pass the infromation between two  methods in swing worker
    class Infor {

        public int len;
        public int cmd;
        public String addr;
        public byte[] pMsg;
    }

    // here use swingworker to deal with the calculation
    // here because we will calculate the data so just use a thread to 
    // make the program batter
    class SendAction extends SwingWorker<Void, Infor> {

        @Override
        protected Void doInBackground() throws Exception {
            String data = " ";

            for (int i = 1; i <= 5; i++) {
                // Array sava the data in char[]
                Infor t = new Infor();

                // check each text field for message
                switch (i) {
                    // class
                    case 1:
                        t.cmd = CLASS_CMD;
                        className = classTextField.getText();
                        if ("size less than 10".equals(className)) {
                            className = "";
                        } else {
                            if (className.length() > 10) {
                                className = "Not support";
                            }
                        }
                        data = className;
                        break;
                    // subject
                    case 2:
                        t.cmd = SUBJECT_CMD;
                        subjectName = subjectTextField.getText();
                        if ("size less than 10".equals(subjectName)) {
                            subjectName = "";
                        } else {
                            if (subjectName.length() > 10) {
                                subjectName = "Not support";
                            }
                        }
                        data = subjectName;
                        break;
                    // teacher
                    case 3:
                        t.cmd = TEACHER_CMD;
                        teacherName = teacherTextField.getText();
                        if ("size less than 5".equals(teacherName)) {
                            teacherName = "";
                        } else {
                            if (teacherName.length() > 5) {
                                teacherName = "Not support";
                            }
                        }
                        data = teacherName;
                        break;
                    // people
                    case 4:
                        t.cmd = PEOPLE_CMD;
                        peopleNumber = peopleTextField.getText();
                        if ("size less than 3".equals(peopleNumber)) {
                            peopleNumber = "";
                        } else {
                            if (peopleNumber.length() > 3) {
                                peopleNumber = "Not support";
                            }
                        }
                        data = peopleNumber;
                        break;
                    // time
                    case 5:
                        t.cmd = TIME_CMD;
                        timeDuration = timeTextField.getText();
                        data = timeDuration;
                        break;

                    default:
                        break;
                }
                // get the byte array of the message
                // get addr infor from the addrList
                t.addr = defaultAddrList.get(addrList.getSelectedIndex());
                t.pMsg = data.getBytes("GB2312");
                t.len = t.pMsg.length;

                // just send the message out 
                publish(t);

            }
            // nothing to return
            return null;
        }

        @Override
        protected void process(List<Infor> chunks) {
            chunks.stream().forEach((i) -> {
                try {
                    buildAndSendFrame(i.cmd, i.addr, i.pMsg, i.len);
                } catch (IOException ex) {
                    Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }

    };

    private static void createAndShow() {
        JFrame appFrame = new JFrame("Wireless LCD System");
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        WirelessLCDSystem system = new WirelessLCDSystem();
        appFrame.setContentPane(system.createPane());

        appFrame.pack();
        appFrame.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShow();
            }
        });
    }

}

// save topology hold structure
class TopologyTree {

    // nest class to save the node information
    private class TreeNode {

        // information of itself
        public String Address = "";
        public String Device = "";

        // hold the positon infor the make the program easier
        public Point location = null;

        // Child information
        public TreeNode firstChild = null;
        public TreeNode secondChild = null;
        public TreeNode thirdChild = null;

        public TreeNode(String id, String name) {
            Address = id;
            Device = name;
        }

    }// end treenode

    // member in the topology
    public TreeNode root = null;

    // hold two vector, array and device
    public Vector<TopologyElement> deviceVector = new Vector<>();
    public Vector<ArrayLine> linkVector = new Vector<>();

    // for resize the canvas in scrollpane
    // because the topology tree hold it's own location
    // so just make the variable here
    public int maxNegativeXAxis = 0;
    public int maxPositiveXAxis = 500;
    public int maxNegaticeYAxis = 0;
    public int maxPositiveYAxis = 500;

    private final int preSetCornerSpace = 50;

    // topology link line length
    private final int linkLength = 100;
    private final int linkEmpty = 35*2;   // 50 / 2 + 10
    
    // record the topology for new one
    private TreeNode isAlreadyAdd = null;
    private TreeNode parentHandler = null;
    
    // scale
    private final double xScale = (linkLength + linkEmpty) * Math.cos(Math.PI / 4);
    private final double yScale = (linkLength + linkEmpty) * Math.sin(Math.PI / 4);

    // no constructor need here
    // search for parent use recursion
    // the key in the node is unique
    // so just get one parent
    private void find(TreeNode root, String addr, int aim) {
        // search the tree
        if (null != root) {
            if (root.Address.equals(addr)) {
                if(0 == aim)
                    parentHandler = root;
                else{
                    isAlreadyAdd = root;
                }
            }
            // continue seach the child
            find(root.firstChild, addr, aim);
            find(root.secondChild, addr, aim);
            find(root.thirdChild, addr, aim);
        }
    }

    // add node
    public void addNode(String parent, String addr, String device) {
        
        isAlreadyAdd = null;
        parentHandler = null;
             
        // first search the tree and find the parent 
        // then add the child to it's parent          
        find(root, parent, 0);
        // find the node        
        find(root, addr, 1);
        
        // if exist, just return 
        if(null != isAlreadyAdd){
            return;
        }

        // if find 
        if (null != parentHandler) {
            // create new node for the root tree
            TreeNode newNode = new TreeNode(addr, device);

            // update the location first
            // according to the parent location
            int x = parentHandler.location.x;
            int y = parentHandler.location.y;
            // for the array
            // this is the end
            int ex = x;
            int ey = y;

            // find proper location to insert the node
            // to make the topology more beautiful, we define
            // left is the first place to insert
            // middle is the third place to insert
            // right is the second place to insert
            if (null == parentHandler.firstChild) {
                parentHandler.firstChild = newNode;
                // update the location
                // left one
                // because the linklength has a value of 100 by default
                // we will use this calculate the x and y axis
                /*
                 A
                 \   |
                 \       |
                 \           |
                 */
                x -= (int)xScale;    // 45'=PI/4
                // here becase the most left positon so just care about maxNegativeXAxis
                maxNegativeXAxis = maxNegativeXAxis < x ? maxNegativeXAxis : x;
                y += (int)yScale;
                maxPositiveYAxis = maxPositiveYAxis > y ? maxPositiveYAxis : y;
                // set the location
                newNode.location = new Point(x, y);

            } else if (null == parentHandler.thirdChild) {
                parentHandler.thirdChild = newNode;
                // update the location
                // more information just notice the code block before
                x += (int)xScale;    // 45'=PI/4
                // Here we need to care the maxPositiveXAxis
                maxPositiveXAxis = maxPositiveXAxis > x ? maxPositiveXAxis : x;
                y += (int)yScale;
                maxPositiveYAxis = maxPositiveYAxis > y ? maxPositiveYAxis : y;
                // set the location
                newNode.location = new Point(x, y);

            } else if( null == parentHandler.secondChild){
                // update the location
                parentHandler.secondChild = newNode;
                // just calculate the y axis
                y += (int)yScale;
                maxPositiveYAxis = maxPositiveYAxis > y ? maxPositiveYAxis : y;
                // no care about the x axis
                // just set the location
                newNode.location = new Point(x, y);
            }else{
                // no more can be add to this node just ignore this node
                return;
            }

            // add to the location vector
            TopologyElement i = new TopologyElement(addr, device, newNode.location);
            deviceVector.addElement(i);

            // add array Line
            int sx = x;
            int sy = y;

            // new axis in the coordinator
            // because the start point in the computer is defferent from math
            // so here some thing changes
            ArrayLine newLine = null;
            if( ex != sx){
                double k = (ey - sy) / (ex - sx);
                double angle = Math.atan(k);
                if (k < 0) {
                    sx += (int) (linkEmpty / 2 * Math.cos(angle));
                    sy += (int) (linkEmpty / 2 * Math.sin(angle));

                    ex -= (int) (linkEmpty / 2 * Math.cos(angle));
                    ey -= (int) (linkEmpty / 2 * Math.sin(angle));
                } else {
                    sx -= (int) (linkEmpty / 2 * Math.cos(angle));
                    sy -= (int) (linkEmpty / 2 * Math.sin(angle));

                    ex += (int) (linkEmpty / 2 * Math.cos(angle));
                    ey += (int) (linkEmpty / 2 * Math.sin(angle));
                }
                // create
                newLine = new ArrayLine(new Point(sx, sy), new Point(ex, ey));
            }else{
                newLine = new ArrayLine(new Point(sx, sy-35), new Point(ex, ey+35));
            }
            
            // add to the vector
            linkVector.addElement(newLine);

        } // not find, create the root
        else {
            root = new TreeNode(addr, device);
            // update the defaut coordiantor positon
            // use the center point to indicate the location
            // because the default canvas size is 500*n(n>400)
            // and the rectangler to hold the circle is 50*50 size
            // we'd batter use 250, 225 as the center of the coordinator
            // keep care of this coordinator, this is the center point
            root.location = new Point(250, 220);

            // here you must initialize the parent node
            // if not, it should null, so it's not ready for the draw 
            // parentNode = root;
            // add to the location vector
            TopologyElement i = new TopologyElement(addr, device, root.location);
            deviceVector.addElement(i);
        }
    }

    // remove, not need now
    // more thing to do future
}

class TopologyElement {

    // hold information
    public String text1 = "";
    public String text2 = "";
    public Point location = null;

    public TopologyElement(String addr, String name, Point center) {
        text1 = addr;
        text2 = name;
        location = center;
    }

    public void showElement(Graphics g) {
        g.setColor(Color.green);

        // fill the rectangle
        Rectangle rec = new Rectangle(location.x-25, location.y-25, 50, 50);
        g.fillOval(rec.x, rec.y, rec.width, rec.height);

        // draw name and address
        g.setColor(Color.black);
        //g.setFont(new Font("consolas", Font.PLAIN, 13));
        g.drawString(text1, 5 + rec.x, 25 + rec.y);
        g.drawString(text2, 5 + rec.x, 40 + rec.y);

        // paint border
        g.setColor(Color.white);
        g.drawOval(rec.x, rec.y, rec.width, rec.height);
    }

}

class ArrayLine {

    // hold the information in the point
    private Point start = null;
    private Point stop = null;

    // constructor
    ArrayLine(Point start, Point stop) {
        this.start = start;
        this.stop = stop;
    }

    // user interface
    public void showArrayLine(Graphics2D g2) {
        g2.setColor(Color.white);
        drawArrayLine(start.x, start.y, stop.x, stop.y, g2);
    }

    // deal with the data
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
}
