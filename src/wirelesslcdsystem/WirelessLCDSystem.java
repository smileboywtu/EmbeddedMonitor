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
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TooManyListenersException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
    private final int GRAPHICS_CMD = 0x000D;
    
    // IMAGE
    private final int IMAGE_REQ_CMD = 0x000C;
    
    // DATA Allow OR Refuse
//    private final int DATA_ALLOW = 0x000E;
//    private final int DATA_REFUSE = 0x000F;
    
    // DEVICE TYPE
    private final int COORDINATOR = 0x0001;
    private final int ROUTER = 0x0002;
    private final int LCD = 0x0003;
    private final int VIBRATE = 0x0004;
    private final int CAMERA = 0x0005;

    // Constants
    private final byte[] START_REQ = {
        (byte) 0xFE, (byte) 0x00,
        (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00,
        (byte) 0x00};
    // Data allow
    private final byte[] DATA_ALLOW = {
        (byte) 0xFE, (byte) 0x00,
        (byte) 0x00, (byte) 0x00,
        (byte) 0x0E, (byte) 0x00,
        (byte) 0x00};
    // Data refuse
    private final byte[] DATA_REFUSE = {
        (byte) 0xFE, (byte) 0x00,
        (byte) 0x00, (byte) 0x00,
        (byte) 0x0F, (byte) 0x00,
        (byte) 0x00};
    
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

    // top level pane
    private JPanel topLevelPane = null;
    
    // handler for topology and message status bar
    private JPanel canvas = null;
    private JTextArea message = null;

    // Value save
    private String className;
    private String subjectName;
    private String teacherName;
    private String peopleNumber;
    private String timeDuration;
    
    // hold data for remote camera
    private final JButton cameraLoading = new JButton("Loading");
    private final JProgressBar cameraWaiting = new JProgressBar();
    
    // hold data for graphics
    private JSlider xAxis = null;
    private JSlider yAxis = null;
    private final Point p1 = new Point(0, 20);
    private final Point p2 = new Point(0, 20);
    private       int   R = 0;
    private final JLabel L1 = new JLabel("P1: (0,20)");
    private final JButton set1 = new JButton("x");
    private final JLabel L2 = new JLabel("P2: (0,20)");
    private final JButton set2 = new JButton("y");
    private final JLabel LR = new JLabel("R: 0");
    private final JButton setR = new JButton("r");
    private final String[] objects = {
                   "POINT",
                   "LINE",
                   "RECTANGLE",
                   "CIRCLE"
    };
    
    private boolean clearState = true;
    private String objectTypeSelected = "point";
    
    private JPanel cards = null;
    private JComboBox shift = null;
    private final String TEXT = "Remote Screen Work With Text";
    private final String GRAPH = "Remote Screen Work With Graph";
    private final String PICTURE = "Remote Camera Control";
    
    private String itemSelected = TEXT;
    
    // RS232 controls
    private JComboBox comList;
    private JComboBox baudrateList;
    private JComboBox addrListForLcd;
    private JComboBox addrListForCamera;

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
    private int receivedImageBytes = 0;
    private ByteArrayOutputStream jpgBuffer = null;
//    private FileOutputStream fileWriter = null;
//    private final String newImage = "camera.jpg";
    
    // params for the bmp file
    private final int imageHeight = 240;
    private final int imageWidth  = 320;
    private int colorTotalBytes = 0;        // this will set by the start frame
                                            // and reset when received done

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
    private ImagePane image = null;
//    private FileWatcher wf = null;

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

    private final ArrayList<String> LCDAddressList = new ArrayList<>();
    private final ArrayList<String> CameraAddressList = new ArrayList();
    
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

    public JPanel createPane() throws IOException, URISyntaxException {
        // create top-level pane
        topLevelPane = new JPanel(new BorderLayout());

        // repose the pane
        JPanel layout1 = new JPanel(new BorderLayout());
        layout1.add(createSerialControlPane(), BorderLayout.PAGE_START);
        layout1.add(createCardPane(), BorderLayout.CENTER);
        layout1.add(createUserControlPane(), BorderLayout.PAGE_END);
        
        JPanel layout2 = new JPanel(new BorderLayout());
        layout2.add(layout1, BorderLayout.PAGE_START);
        layout2.add(createMessagePane(), BorderLayout.PAGE_END);
        
        // repose the pane
        JPanel layout3 = new JPanel(new BorderLayout());
        layout3.add(createPicturePane(), BorderLayout.PAGE_START);
        layout3.add(createADXL345Pane(), BorderLayout.PAGE_END);
        
        JPanel layout4 = new JPanel(new BorderLayout());
        // add the lcd and the log pane
        layout4.add(layout2, BorderLayout.LINE_START);
        // add the topology pane
        layout4.add(createTopologyPane(), BorderLayout.CENTER);
        // add the picture and adxl345 pane
        layout4.add(layout3, BorderLayout.LINE_END);
        
        // add the message pane
        topLevelPane.add(layout4, BorderLayout.CENTER);

        return topLevelPane;
    }
    
    private JPanel createCardPane(){
        // topo pane
        JPanel pane = new JPanel(new BorderLayout());
        
        // create card layout
        cards = new JPanel(new CardLayout());
        // use combo box to select
        JPanel comboPane = new JPanel();
        shift = new JComboBox();
        shift.setEditable(false);
        shift.addItem(TEXT);
        shift.addItem(GRAPH);
        shift.addItem(PICTURE);
        // disable the cards selected initially
        shift.setEnabled(false);
        
        comboPane.add(shift);
        
        comboPane.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Card Chooser"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        // add item change listener
        shift.addItemListener((ItemEvent e) -> {
            CardLayout cl = (CardLayout)(cards.getLayout());
            itemSelected = (String)e.getItem();
            if(TEXT.equals(itemSelected) || GRAPH.equals(itemSelected)){
                send.setEnabled(true);
                addrListForLcd.setEnabled(true);
            }else{
                send.setEnabled(false);
                addrListForLcd.setEnabled(false); 
            }
            cl.show(cards, itemSelected);
        });
        
        // add the two card
        cards.add(createTextPane(), TEXT);
        cards.add(createLcdGraphicsPane(), GRAPH);
        cards.add(createCameraControlPane(), PICTURE);
   
        // set again
        itemSelected = TEXT;
        
        // lay component
        pane.add(comboPane, BorderLayout.PAGE_START);
        pane.add(cards, BorderLayout.CENTER);
        
        return pane;
    }
    
    private JPanel createCameraControlPane(){
        // container
        JPanel containPane = new JPanel();
        // use vertical box
        containPane.setLayout(new BoxLayout(containPane, BoxLayout.PAGE_AXIS));
        
        // first create top half pane
        JPanel topHalf = new JPanel();
        topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.LINE_AXIS));
        cameraLoading.setEnabled(false);
        cameraLoading.addActionListener((ActionEvent e) -> {
            cameraLoading.setEnabled(false);
            addrListForCamera.setEnabled(false);
            topLevelPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            // refuse allow other type of data except camera data
            try {
                serialOut.write(DATA_REFUSE, 0, DATA_REFUSE.length);
                // then you should send the camera data request
                // get the address and send the request
                sendCameraDataRequst();
            } catch (IOException ex) {
                Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        topHalf.add(cameraLoading);
        topHalf.add(Box.createRigidArea(new Dimension(100, 0)));
        addrListForCamera = new JComboBox();
        addrListForCamera.addItem("NULL");
        addrListForCamera.setEnabled(false);
        topHalf.add(addrListForCamera);
        topHalf.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        // bottom half pane
        JPanel bottomHalf = new JPanel(new BorderLayout());
        bottomHalf.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.PAGE_START);
        cameraWaiting.setMaximum(0);
        // you should init this every time you start to receive a new image
        cameraWaiting.setMaximum(153600);
        cameraWaiting.setValue(0);
        cameraWaiting.setStringPainted(true);
        bottomHalf.add(cameraWaiting, BorderLayout.PAGE_END);
        bottomHalf.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        // add 
        containPane.add(topHalf);
        containPane.add(bottomHalf);
        containPane.add(Box.createRigidArea(new Dimension(0, 40)));
        
        // set border
        containPane.setBorder(
            BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Camera Control"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        // return 
        return containPane;
    }
    
    private JPanel createLcdGraphicsPane(){
        // create pane
        // this is the top level pane
        JPanel graphicPane = new JPanel();
        graphicPane.setLayout(new BoxLayout(graphicPane, BoxLayout.PAGE_AXIS));
        graphicPane.setPreferredSize(new Dimension(300, 180));
        
        // create x , y sliders bar
        xAxis = new JSlider(JSlider.HORIZONTAL, 0, 220, 0);
        xAxis.setMajorTickSpacing(60);
        xAxis.setMinorTickSpacing(10);
        xAxis.setPaintTicks(true);
        xAxis.setPaintLabels(true);
        
        yAxis = new JSlider(JSlider.HORIZONTAL, 20, 176, 20);
        yAxis.setMajorTickSpacing(50);
        yAxis.setMinorTickSpacing(10);
        yAxis.setPaintTicks(true);
        yAxis.setPaintLabels(true);
        
        // struct it
        JPanel axis = new JPanel();
        axis.setLayout(new BoxLayout(axis, BoxLayout.LINE_AXIS));
        axis.add(xAxis);
        axis.add(yAxis);
        //axis.setBackground(Color.green);
        axis.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        // create set button
        set1.addActionListener((ActionEvent e) -> {
            // read slider bar 1
            p1.x = xAxis.getValue();
            p1.y = yAxis.getValue();
            String str = "P1: ("+p1.x+","+p1.y+")";
            L1.setText(str);
        });
        set2.addActionListener((ActionEvent e) -> {
            // read slider bar 1
            p2.x = xAxis.getValue();
            p2.y = yAxis.getValue();
            String str = "P2: ("+p2.x+","+p2.y+")";
            L2.setText(str);
        });
        setR.addActionListener((ActionEvent e) -> {
            // read slider bar 1
            R = xAxis.getValue();
            String str = "R: "+ R + "";
            LR.setText(str);
        });
        // contain point a label and set button
        JPanel setPane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        setPane.setLayout(gridbag);
        // create a constrants for each one
        GridBagConstraints c = new GridBagConstraints();
        // set globals
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        
        // set row one
        c.gridx = 0;               
        c.gridy = 0;
        c.insets = new Insets(0,0,0,10);
        setPane.add(set1, c);
        c.gridx = 1;               
        c.gridy = 0;
        setPane.add(L1, c);
        c.gridx = 2;               
        c.gridy = 0;
        setPane.add(set2, c);
        c.gridx = 3;               
        c.gridy = 0;
        // set row two
        setPane.add(L2, c);
        c.gridx = 0;               
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        setPane.add(setR, c);
        c.gridx = 1;               
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        setPane.add(LR, c);
        
        // create control panel
        JCheckBox clearCheck = new JCheckBox("Auto-clear");
        clearCheck.setSelected(true);
        clearCheck.addItemListener((ItemEvent e) -> {
            clearState = (e.getStateChange() == ItemEvent.SELECTED); 
        });
        
        c.gridx = 3;               
        c.gridy = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        setPane.add(clearCheck, c);
        
        // add a border to this
        setPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
           
        // create radio buttons
        // create a action Listener
        RadioButtonSelect check = new RadioButtonSelect(); 
        // create buttons
        JRadioButton pointButton = new JRadioButton(objects[0]);
        pointButton.setActionCommand("point");
        pointButton.addActionListener(check);
        pointButton.setSelected(true);

        JRadioButton lineButton = new JRadioButton(objects[1]);
        lineButton.setActionCommand("line");
        lineButton.addActionListener(check);

        JRadioButton recButton = new JRadioButton(objects[2]);
        recButton.setActionCommand("rectangle");
        recButton.addActionListener(check);

        JRadioButton circleButton = new JRadioButton(objects[3]);
        circleButton.setActionCommand("circle");
        circleButton.addActionListener(check);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(pointButton);
        group.add(lineButton);
        group.add(recButton);
        group.add(circleButton);
        
        JPanel radioPanel = new JPanel(new GridLayout(1, 4));
        radioPanel.add(pointButton);
        radioPanel.add(lineButton);
        radioPanel.add(recButton);
        radioPanel.add(circleButton);
        
        // add a border
        setPane.setBorder( BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        // create a border for it
        graphicPane.setBorder(
            BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Graphics"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        // add to the graphicPane
        graphicPane.add(axis);
        graphicPane.add(setPane);
        graphicPane.add(radioPanel);
  
        return graphicPane;
    }

    private JPanel createPicturePane() throws IOException, URISyntaxException{
        // create a contaner
        image = new ImagePane();
        // size the panel
        Dimension area = new Dimension(350, 270);
        image.setPreferredSize(area);
        // custom the background color
        image.setBackground(new Color(223, 239, 239));
        // add file listener here
//        wf = new FileWatcher(
//            Paths.get("./src/wirelesslcdsystem/resource/"));
        // where to start the thread?
        
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
        message = new JTextArea(3, 25);
        message.setEditable(false);
        message.setBackground(new Color(223, 231, 189));
        message.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
               message.setCaretPosition(message.getDocument().getLength()); 
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
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
        statusBar.setPreferredSize(
                new Dimension(250, 180));

        // return
        return statusBar;
    }

    private JPanel createTextPane(){
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
                        BorderFactory.createTitledBorder("Text"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        return textControlsPane;
    }
    
    private JPanel createUserControlPane(){
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
        buttonPane.add(addrListForLcd);
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
        
        // return 
        return buttonPane;
    }
    
    private JPanel createSerialControlPane() {
        // Create RS232 comport and BaudRate
        comList = new JComboBox(comListStr);
        comList.setSelectedIndex(0);
        comList.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        baudrateList = new JComboBox(baudrateListStr);
        baudrateList.setSelectedIndex(0);
        baudrateList.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // Create address list 
        addrListForLcd = new JComboBox();
        // default addr
        addrListForLcd.addItem("None");
        addrListForLcd.setSelectedIndex(0);
        addrListForLcd.setEnabled(false);
        addrListForLcd.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // Create Control Pane
        JPanel RS232ControlPane = new JPanel(new GridLayout(1, 2));
        RS232ControlPane.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("RS232 Controls"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        RS232ControlPane.add(comList);
        RS232ControlPane.add(baudrateList);

        // return it
        return RS232ControlPane;
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
        StringToHexBytes(addr, addrBytes);
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
                
                // serialPort.addEventListener(this);
                
                // use state machine here
                // so do not use the interrupt now
                // first time must open this notify
                
                //serialPort.notifyOnDataAvailable(true); 
                
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
                if(null == serialReadThread){
                    serialReadThread = new Thread(new SerialReader(serialIn));
                    // start the thread
                    serialReadThread.start();
                }
                
            } else {
                System.out.println(
                        "Error: Only serial ports are handled by this example.");
            }
        }
    }

    private void stopRS232Com() throws InterruptedException, IOException {
        
        // interrupt the thread first
        serialReadThread.interrupt();
        serialReadThread = null;
        
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
        
        // clear all the data
        serialPort = null;
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
    
    private void StringToHexBytes(String str, byte[] addr){
        // we all ready know the size of the str is 4 bits
        String str1 = str.substring(0, 2);
        String str2 = str.substring(2, 4);
        addr[0] = (byte)(Integer.parseInt(str1, 16)&0xFF);
        addr[1] = (byte)(Integer.parseInt(str2, 16)&0xFF);
    }

    private void sendCameraDataRequst() throws IOException {
        // get the address
        buffer = new ByteArrayOutputStream(7);
        
        // write start value
        buffer.write(SOF_VALUE);
        
        // write data len 0
        buffer.write(0);
        
        // wite command
        buffer.write(UINT32_LOW(IMAGE_REQ_CMD));
        buffer.write(UINT32_HIGH(IMAGE_REQ_CMD));

        // fill the address
        String addr = CameraAddressList.get(
                addrListForCamera.getSelectedIndex()).
                substring(2);
        // then get the bytes
        byte[] addrBytes = new byte[2];
        StringToHexBytes(addr, addrBytes);
        buffer.write(addrBytes, 1, 1);
        buffer.write(addrBytes, 0, 1);

        // Fill the data
        // no data for camera request
        // see before the len is zero

        // Fill FCS
        buffer.write(calcuFCS(buffer.toByteArray(), 5));

        // use the output stream to send the buffer
        serialOut.write(buffer.toByteArray());
        
        // updata the txCounter
        txCounter += 7; 

        //System.out.println(Arrays.toString(buffer.toByteArray()));
        message.append("[ " + getHexString(buffer.toByteArray()) + "]");
        message.append("\n");

        // show use tx
        //System.out.println("You hava send: " + txCounter);
        message.append("You hava send: " + txCounter);
        message.append("\n");   
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
                while (true) {
                    int ch = 0;
                    // read and dispath the event
                    while (serialReader.available() > 0) {
                    // here we use state machine to read the data
                        // first to read a data
                        ch = serialReader.read();
                        // check state
                        switch (state) {
                            case SOF_STATE:
                                if (SOF_VALUE == ch) {
                                    // step forward
                                    state = LEN_STATE;
                                }
                                break;
                            // check the length
                            case LEN_STATE:
                                // get the data
                                incomingFrameBuffer = new byte[ch + 5];
                                // record data, this used for the data check and save
                                length = ch;
                                // reset the receive data length counter
                                recvLen = 0;
                            // this is safe to convert, cause ch is between 0 to 255
                                // here the ch is much less than 128, if acquired correctly
                                incomingFrameBuffer[0] = (byte) ch;
                                // step to next
                                state = CMD1_STATE;
                                break;
                            case CMD1_STATE:
                            // save data and to next state
                                // safe to convert
                                incomingFrameBuffer[1] = (byte) ch;
                                state = CMD2_STATE;
                                break;
                            case CMD2_STATE:
                            // safe to convert 
                                // the ch here must be 0x00
                                incomingFrameBuffer[2] = (byte) ch;
                                state = DST1_STATE;
                                break;
                            case DST1_STATE:
                                incomingFrameBuffer[3] = (byte) ch;
                                state = DST2_STATE;
                                break;
                            case DST2_STATE:
                                incomingFrameBuffer[4] = (byte) ch;
                                if (0 == length) {
                                // just go fcs state
                                    // cause there is no data needed to receive
                                    state = FCS_STATE;
                                } else {
                                    state = DATA_STATE;
                                }
                                break;
                            case DATA_STATE:
                                // record the current one
                                incomingFrameBuffer[5 + recvLen++] = (byte) ch;
                                // read more if enough
                                int available = serialReader.available();
                                // read part or read all data
                                if ((length - recvLen) <= available) {
                                    // read all data
                                    serialReader.read(incomingFrameBuffer, 5 + recvLen, length - recvLen);
                                    // step to the fcs state
                                    state = FCS_STATE;
                                } else {
                                // no enough data to read all
                                    // just read part and wait another turn to read
                                    serialReader.read(incomingFrameBuffer, 5 + recvLen, available);
                                    // set the current data counter
                                    recvLen += available;
                                }
                                break;
                            case FCS_STATE:
                                fcs = ch;
                                // calculate fcs and go forther
                                if (fcs == (calculateFCS(incomingFrameBuffer, length + 5))) {
                                    // check ok but with different cmd
                                    pendingIncomingFrame(incomingFrameBuffer, true);
                                } else {
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
                    // serialPort.notifyOnDataAvailable(true);
                    Thread.sleep(1);
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
                // just return
            }
            
        }// end run
        
        // calculate fcd
        private int calculateFCS(byte[] val, int len) {
            int xorResult = 0x00;
            int cur = 0;
            // ignore the sof bit
            for(int i=0; i<len; i++){
                cur = (val[i] >= 0) ? val[i] : ((val[i]&0x7F)|(1<<7));
                xorResult ^= cur;
            }
            return xorResult;
        }
        
        // get the hex string to write to the file
         private String toHexString(byte[] pre, int offset, int len) {
            String result = "";
            for (int i = 0; i < len; i++) {
                result += (Integer.toString((pre[i+offset] & 0xff) + 0x100, 16)).toUpperCase().substring(1)
                        + " ";
            }
            return result;
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
                        ProcessIncomingMessage(data);

                        // update message
                        message.append("new node add to the network\n");
                    }else{
                        // nothing to do with demaged data
                    }
                break;
                case CAMERA_START_CMD:
                    if(flag){
                        // open the file and delete all data
//                        fileWriter = Files.newOutputStream(Paths.get("./src/wirelesslcdsystem/resource/"+newImage),
//                                    StandardOpenOption.CREATE,
//                                    StandardOpenOption.TRUNCATE_EXISTING);
//                        fileWriter = new FileOutputStream(
//                                new File("./src/wirelesslcdsystem/resource/"+newImage));
                        jpgBuffer = new ByteArrayOutputStream();    // this can auto increase
                        // JPEG Specific FF D8 start with a times of 8 bytes 
                        // data
//                        fileWriter.write(0xFF);
//                        fileWriter.write(0xD8);
                        
                        // set the colortotalbits
                        /*
                           | len  |  cmd1  | cmd2  | addr1 | addr2 | data...|
                        */
                        colorTotalBytes = BUILD_UINT32(data[5], data[6]);
                        // set the progress bar the maximum value
                        cameraWaiting.setMaximum(colorTotalBytes);
                        // update message
                        message.append("the new image has "+colorTotalBytes+" bytes.\n");
                        message.append("start to receive new image...\n");
                    }else{
                        // do nothing
                        updateCameraControlPane();
                        
                        // update message
                        message.append("camera ack fails...\n");
                    }
                break;
                case CAMERA_DATA_CMD:
                    if(flag){
                       // append to the file 
                       // check if we have received 153600 bytes 
                       // if not, just receive bytes and write to colorHex file
                        
//                       if(receivedImageBytes < colorTotalBytes){
                        
                           // the content start at pointer 5
                           // the length of the content saved in data[0]
                           // the first data of the data content is sequence 
                           // should be ignore
                           // now with new way to get camera data 
                           // so just feel free to read the data from offset = 5
                        
                           // write content
//                           fileWriter.write(data, 5, data[0]);
                           jpgBuffer.write(data, 5, data[0]);
                           
                           //fileWriter.write(data, 6, data[0]-1);
                           // update the counter
                           // data[0] must be a number between 0 ~ 255
                           receivedImageBytes += data[0];
                           
                           // update the program bar
                           cameraWaiting.setValue(receivedImageBytes);
//                       }
//                       else{
                           if(receivedImageBytes >= colorTotalBytes){
                                                          
                                // JPEG Specific FF D9 end with a times of 8 bytes
                               // data
//                               fileWriter.write(0xFF);
//                               fileWriter.write(0xD9);
                               
                               // close the hex file writer
//                               fileWriter.flush();
//                               fileWriter.close();
//                               fileWriter = null;
                               // add to the label with the new image    
                               image.setImageSource(jpgBuffer.toByteArray());
                               // repain to show instantly
                               image.repaint();
                               // reset
                               jpgBuffer.close();
                               jpgBuffer = null;
                               
                               message.append("Test: received image bytes is "+receivedImageBytes);
                               message.append("\n");
                               
                               // counter reset
                               receivedImageBytes = 0;
                               // color total bit reset
                               colorTotalBytes = 0;
                               // update the program bar
                               cameraWaiting.setValue(receivedImageBytes);
                               // use watch service to watch the file change
                               
                               // update UI
                               updateCameraControlPane();
                               // message
                               message.append("receive new image successfully\n");
                       }
                   }else{
                       // destory the file, wait another start signal
//                       fileWriter.close();
                       // stop the process bar 
                       updateCameraControlPane();
                       
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
                        // notify the pane to updata 
                        curve.invalidate();
                        // repaint
                        curve.repaint();
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
                        cameraLoading.setEnabled(true);

                        // list enable
                        addrListForLcd.setEnabled(true);
                        addrListForCamera.setEnabled(true);
                        comList.setEditable(false);
                        baudrateList.setEditable(false);
                        
                        // enable cards
                        shift.setEnabled(true);
                        
                        // update message
                        message.append("Coordinator ACK\n");
                    }
                break;
                default:
                    System.out.println("error package...");
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
             image.invalidate();
             // repaint
             image.repaint();
        }// end methods

        private void updateCameraControlPane() throws IOException {
            // stop receive data
            // and notify the receive error
            // here enable receive data
            serialOut.write(DATA_ALLOW, 0, DATA_ALLOW.length);

            // set the prograss value
            cameraWaiting.setValue(0);
            
            // set cursor null
            topLevelPane.setCursor(null);
            
            // enable the camera address choose
            cameraLoading.setEnabled(true);
            
            // enable the address list choose
            addrListForCamera.setEnabled(true);
        }
        
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
        }// end if
    }

    // process incoming message
    private void ProcessIncomingMessage(byte[] pData) {

        // deal with the cluster
        String srcAddr = "";
        String parentAddr = "";
        String device = "";
        int deviceCMD = 0;

        // build source address
        srcAddr += (Integer.toString((pData[+4] & 0xFF) + 0x100, 16)).toUpperCase().substring(1);
        srcAddr += (Integer.toString((pData[+3] & 0xFF) + 0x100, 16)).toUpperCase().substring(1);
        srcAddr = "0x" + srcAddr;

        // build device 
        deviceCMD = BUILD_UINT32(pData[+5], pData[+6]);

        // huild parent addr
        parentAddr += (Integer.toString((pData[+8] & 0xFF) + 0x100, 16)).toUpperCase().substring(1);
        parentAddr += (Integer.toString((pData[+7] & 0xFF) + 0x100, 16)).toUpperCase().substring(1);
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
            case VIBRATE:
                device = "Vibrate";
                break;
            case CAMERA:
                device = "Camera";
                break;
            default:
                device = "Unknow";
                break;
        }

        // add the node to the topology
        // this method will use much time, so here you need to use a swing 
        // worker to do this
        //topology.addNode(parentAddr, srcAddr, device);
        (new UpdateTopology(parentAddr, srcAddr, device)).execute();

        // update the address list
        switch (device) {
            case "LCD":
                if(false == LCDAddressList.contains(srcAddr)){
                    LCDAddressList.add(srcAddr);
                    // show message to the user
                    message.append(srcAddr + " join the network" + "\n");
                }   addrListForLcd.removeAllItems();
                // if the address already in the list then exit
                LCDAddressList.stream().forEach((i) -> {
                    addrListForLcd.addItem(i);
            }); break;
            case "Camera":
                if(false == CameraAddressList.contains(srcAddr)){
                    CameraAddressList.add(srcAddr);
                    // show message to the user
                    message.append(srcAddr + " join the network" + "\n");
                }   addrListForCamera.removeAllItems();
                // if the address already in the list then exit
                CameraAddressList.stream().forEach((i) -> {
                    addrListForCamera.addItem(i);
            }); break;
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
                } catch (NoSuchPortException 
                        | PortInUseException 
                        | UnsupportedCommOperationException 
                        | IOException 
                        | TooManyListenersException ex) {
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
                cameraLoading.setEnabled(false);
                
                // progress bar set zero
                cameraWaiting.setValue(0);

                // enable or disable the combobox
                addrListForLcd.setEnabled(false);
                addrListForCamera.setEnabled(false);
                comList.setEnabled(true);
                baudrateList.setEnabled(true);
                
                // disable card select
                shift.setEnabled(false);
                topLevelPane.setCursor(null);
                
                // clear the topology information
                LCDAddressList.removeAll(LCDAddressList);
                // remove elements in topology
                topology.deviceVector.removeAllElements();
                topology.linkVector.removeAllElements();   
                // repaint it this should make there no elements in the topo pane
                canvas.repaint();               
                // reset the topology root
                topology.root = null;
                // reset the visual pane
                visualTool.clearAllData();
                // clear buffer
                Arrays.fill(ADXL345, (byte)0);
                curve.repaint();
                // clear message and reset the counter
                message.removeAll();
                rxCounter = 0;
                txCounter = 0;
                // set the color counter to zero
                colorTotalBytes = 0;
                break;
        }
    }

    // draw the image 
    class ImagePane extends JPanel{
        
        private BufferedImage img = null;
        private final String defaultImage = "wtu.png";
        
        public ImagePane() throws IOException{
            // update the resource first
           java.net.URL imageUrl = new java.net.URL(WirelessLCDSystem.class.getResource("./resource/"+defaultImage).toString());
           img = ImageIO.read(imageUrl);
        }
        
        public void setImageSource(byte[] array) throws IOException{
            // read image
            img = ImageIO.read(new ByteArrayInputStream(array));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            // inherited from JPanel
            super.paintComponent(g);
            // draw the image
            g.drawImage(img, 15, 15, null);
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
            visualTool.drawVisualWave(x/1000%60, y/1000%60, z/1000%60);
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
                        // every time you draw the element 
                        // you should use the max negtive x, and y offset
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
    
    class RadioButtonSelect implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            objectTypeSelected = e.getActionCommand();
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
            //canvas.revalidate();

            // repaint
            canvas.repaint();

            // add notice here
            // the awt will also use a thread to update the pane
            // so here we do not need to care about that, so just let
            // the awt to deal with it automatically
        }
    }

    class FileWatcher implements Runnable {

        private Path file = null;
        private WatchService ws = null;

        public FileWatcher(Path path) throws IOException {
            file = path;
            // set the new watch service
            ws = FileSystems.getDefault().newWatchService();
            // add listener
            file.register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        // here use thread to poll the event
        @Override
        public void run() {
            while (true) {
                // wait for key to be signaled
                WatchKey key;

                try {
                    key = ws.take();
                } catch (InterruptedException x) {
                    return;
                }

                key.pollEvents().stream().forEach((WatchEvent<?> event) -> {
                    WatchEvent.Kind<?> kind = event.kind();                   
                    // This key is registered only
                    // for ENTRY_CREATE events,
                    // but an OVERFLOW event can
                    // occur regardless if events
                    // are lost or discarded.
                    if (!(kind == StandardWatchEventKinds.OVERFLOW)) {
//                        try {
//                            // deal with the file change and notice the pane to
//                            // redraw
//                            image.setImageSource(newImage);
//                            // repain to show instantly
//                            image.repaint();
//                        } catch (IOException ex) {
//                            Logger.getLogger(WirelessLCDSystem.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                    }
                });

                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                boolean valid = key.reset();

                if (!valid) {
                    break;
                }
            }
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
            // save data string
            String data = "";

            
            if (GRAPH.equals(itemSelected)) {
                // get new every time
                Infor t = new Infor();
                // if need to clear the screen
                if (clearState) {
                    // do not erase the title
                    data += "BOXF(0,20,220,176,0);";
                }
                // check action
                switch (objectTypeSelected) {
                    case "point":
                        data += "PS(" + p1.x + "," + p1.y + ",15);";
                        break;
                    case "line":
                        data += "PL(" + p1.x + "," + p1.y + "," + p2.x + "," + p2.y + ",15);";
                        break;
                    case "rectangle":
                        data += "BOX(" + p1.x + "," + p1.y + "," + p2.x + "," + p2.y + ",15);";
                        break;
                    case "circle":
                        data += "CIR(" + p1.x + "," + p1.y + "," + R + ",15);";
                        break;
                }

                // render mark
                data += "\r\n";
                
                // set the cmd
                t.cmd = GRAPHICS_CMD;
                        
                // get the byte array of the message
                // get addr infor from the addrListForLcd
                t.addr = LCDAddressList.get(addrListForLcd.getSelectedIndex());
                t.pMsg = data.getBytes("GB2312");
                t.len = t.pMsg.length;
                // just send the message out 
                publish(t);
                
            } else {

                for (int i = 0; i <= 5; i++) {
                    // get new every time
                    Infor t = new Infor();
                    // check each text field for message
                    switch (i) {
                        // clear first
                        case 0:
                            t.cmd = GRAPHICS_CMD;
                            data = "BOXF(0,20,220,176,0);\r\n";
                            break;
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
                            // here you need to clear the screen first
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
                    }// end switch
                    
                    // get addr infor from the addrListForLcd
                    t.addr = LCDAddressList.get(addrListForLcd.getSelectedIndex());
                    t.pMsg = data.getBytes("GB2312");
                    t.len = t.pMsg.length;
                    // just send the message out 
                    publish(t);
                }// end while
            }// end else
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
    
    private static void createAndShowLoading(){
        // create loading welcome
        JWindow loading = new JWindow();
        loading.setAlwaysOnTop(true);
        
        // set backgroud
        loading.setBackground(new Color(0, 0, 0, 0));
        
        // set transparent content
        loading.setContentPane(new JPanel(){        
            @Override
            public void setOpaque(boolean isOpaque) {
                super.setOpaque(false); 
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // get g2
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setComposite(AlphaComposite.SrcOver.derive(0.0f));
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
            }          
        });
        
        JLabel mainLabel = null;
        // add the icon
        java.net.URL imgURL = WirelessLCDSystem.class.getResource("resource/welcome.png");
        if(null != imgURL){
            mainLabel = new JLabel(new ImageIcon(imgURL));
            loading.add(mainLabel);
        }
        
        // set the property location
        loading.setLocationRelativeTo(null);
        int x = loading.getLocation().x;
        int y = loading.getLocation().y;
        loading.setLocation(x-400, y-250);
        
        // pack and show
        loading.pack();
        // show 
        loading.setVisible(true);
        // use a thread to update the logo
        (new WelcomeLogo(loading, mainLabel)).execute();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowLoading();
            }
        });
    }

}

// use to update the logo
class WelcomeLogo extends SwingWorker<Void, BufferedImage>{

    // main frame
    private JWindow currentWindow = null;
    private JLabel label = null;
    private JFrame appFrame = null;
    
    //private static int test = 0;
    
    public WelcomeLogo(JWindow w, JLabel l){
        currentWindow = w;
        if(l == null)
            label = new JLabel("Loading...");
        else
            label = l;
    }
    
    @Override
    protected void done() {
        // transparent it
        // close the current window
        currentWindow.setVisible(false);
        currentWindow.dispose();
        // show main window
        appFrame.setVisible(true);
    }

    @Override
    protected void process(List<BufferedImage> chunks) {
        chunks.stream().forEach((i) -> {
            //create new pane
            label.setIcon(new ImageIcon(i));   
            //test
            //System.out.println(test);
        });
        
    }

    @Override
    protected Void doInBackground() throws Exception {
        // first draw the window and do not show it
        createAndShowMain();
        
        // here will draw new image label
        float counter = 0.0f;
        Random generator = new Random();
        java.net.URL imgURL = WirelessLCDSystem.class.getResource("resource/welcome.png");
        Image img = ImageIO.read(imgURL);
        // slepp a random time and update the label
        while(true){
            if(counter >= 100){
                break;
            }
            // get random number less than 20 every time
            counter += generator.nextFloat()*20;
            // every time create a new image
            BufferedImage img_ = new BufferedImage(800, 500, BufferedImage.TYPE_INT_ARGB);
            Graphics2D handler = img_.createGraphics();
            // according to the counter
            int process = Math.round((counter/100)*800);
            // draw to the image
            handler.drawImage(img, 0, 0, null);
            handler.setColor(Color.yellow);
            handler.fillRect(0, 460, process, 2);
            // process
            //test++;
            publish(img_);
            // sleep a while
            Thread.sleep(500);
        }
        return null;
    }
    
    private void createAndShowMain() throws IOException, URISyntaxException {
        
        // Decorate the windows
        JFrame.setDefaultLookAndFeelDecorated(true);
        
        appFrame = new JFrame("Wireless LCD System");
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        WirelessLCDSystem system = new WirelessLCDSystem();
        appFrame.setContentPane(system.createPane());
        
        // set the Icon
        java.net.URL imgURL = WirelessLCDSystem.class.getResource("resource/logo.jpg");
        if(null != imgURL)
            appFrame.setIconImage(new ImageIcon(imgURL).getImage());
        // set the location relative to null
        
        appFrame.setLocationRelativeTo(null);
        // get the location
        int x = appFrame.getLocation().x;
        int y = appFrame.getLocation().y;
        // set again
        appFrame.setLocation(x-600, y-300);
        
        // show
        appFrame.pack();
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
    
    // x, y prefix when needed
    private int xPreOffset = 0;
    private int yPreOffset = 0;

    public TopologyElement(String addr, String name, Point center) {
        text1 = addr;
        text2 = name;
        location = center;
    }
    
    public void setPreOffset(int x, int y){
        xPreOffset = x;
        yPreOffset = y;
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
