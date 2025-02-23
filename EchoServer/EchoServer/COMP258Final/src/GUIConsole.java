/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;

/**
 * This class constructs the UI for a chat client. It implements the chat
 * interface in order to activate the display() method.
 *
 * @author PC
 */
public class GUIConsole extends JFrame implements ChatIF {

    //Class variables *************************************************
    /**
     * The default port to connect on.
     */
    final public static int DEFAULT_PORT = 5555;

    //GUI variable
    private JButton loginB = new JButton("Login");
    private JButton logoffB = new JButton("Logoff");
    private JButton sendB = new JButton("Send");
    private JButton quitB = new JButton("Quit");
    private JButton browseB = new JButton("Browse");
    private JButton saveB = new JButton("Save");

    private JLabel hostLB = new JLabel("Host: ", JLabel.RIGHT);
    private JLabel portLB = new JLabel("Port: ", JLabel.RIGHT);
    private JLabel userIdLB = new JLabel("User Id: ", JLabel.RIGHT);
    private JLabel messageLB = new JLabel("Message: ", JLabel.RIGHT);

    private JTextArea hostTxF = new JTextArea("localhost");
    //private JTextArea hostTxF = new JTextArea("127.0.0.1");
    private JTextArea portTxF = new JTextArea("5555");
    private JTextField userIdTxF = new JTextField("");
    private JTextField messageTxF = new JTextField("");

    private JTextArea messageList = new JTextArea();

    //Instance variables **********************************************
    /**
     * The instance of the client that created this ConsoleChat.
     */
    ChatClient client;
    // Working with files: Declare a blank file object for Browse and Save buttons.
    private File selectedFile = null;

    //main method
    //  set the host and the port
    //  from command line?
    //  create instance of class with host and port
    //  call method to listen for input?
    //constructor with host and port
    public GUIConsole(String host, int port) {
        //set anme of window
        super("Simple Chat GUI");
        //set the size
        setSize(300, 400);
        
        //set messagelist color
        messageList.setBackground(Color.BLACK);
        messageList.setForeground(Color.WHITE);
        
        setVisible(true);
        JPanel bottom = new JPanel();
        add("Center", messageList);
        add("South", bottom);

        //make the bottom part of the window a grid with
        //7 rows, 2 columns and 5 pixels of vertical and horizontal space
        bottom.setLayout(new GridLayout(7, 2, 5, 5));
        // Add a 5-pixel empty border at the top (top, left, bottom, right)
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Set background color to a pale purple (lavender-like)
        bottom.setBackground(new Color(230, 230, 250));
        
        bottom.add(hostLB);
        bottom.add(hostTxF);

        bottom.add(portLB);
        bottom.add(portTxF);

        bottom.add(userIdLB);
        bottom.add(userIdTxF);
        bottom.add(messageLB);
        bottom.add(messageTxF);

        bottom.add(loginB);
        bottom.add(sendB);

        bottom.add(browseB);
        bottom.add(saveB);
        bottom.add(logoffB);
        bottom.add(quitB);

        sendB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = messageTxF.getText();
                //display(message);
                messageTxF.setText("");
                send(message);
            }
        });
        quitB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send("#quit");
            }
        });
        loginB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //update host
                send("#setHost " + hostTxF.getText());

                //update port
                send("#setPort " + portTxF.getText());
                //update the user Id
                String userId = userIdTxF.getText();
                send("#login " + userId);
            }
        });
        logoffB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String userId = userIdTxF.getText();
                send("#logoff");
            }
        });
        // Browse button.
        browseB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (client.isConnected()) {
                    JFileChooser fileChooser = new JFileChooser();              // Declare File Chooser

                    int choosingStatus = fileChooser.showOpenDialog(bottom);    // Show Open-Dialog that has bottom Jpanel as its parent.

                    // Possible outcomes: CANCEL_OPTION, APPROVE_OPTION, and ERROR_OPTION.
                    if (choosingStatus == JFileChooser.APPROVE_OPTION) // choosingStatus == 0.
                    {
                        selectedFile = fileChooser.getSelectedFile();           // Get a file from fileChooser UI then store in a temporary file.

                        display("Selected file: " + selectedFile.getAbsolutePath());
                    } else if (choosingStatus == JFileChooser.ERROR_OPTION) // choosingStatus == -1.
                    {
                        display("Error on choosing a file.");
                    }
                } else {
                    display("Please log in first!");
                }
            }
        });

        // Save button.
        saveB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (client.isConnected()) {
                    try {
                        byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());   // Transform a file into an array of bytes.

                        client.setFileBytes(fileBytes);                         // Set file data (array of bytes) in ChatCient.

                        send("#ftpUpload " + selectedFile.getName());           // Send command
                    } catch (IOException eio) {
                        display("Error sending file to server.");
                        System.out.println(eio);
                    }
                } else {
                    display("Please log in first!");
                }
            }
        });
        try {
            client = new ChatClient(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error Can't setup connection.... Terminating client.");
            System.exit(1);
        }
        //Do all other constructor code before showing the window
        //make the window visible
        setVisible(true);
    }

    public void send(String message) {
        client.handleMessageFromClientUI(message);
    }

    //  try to make a chatClient with host port and this
    //  catch io exception
    //      Shutdown the application
    //accept method
    /**
     * Thus message take a string and display it to the GUI interface CURRENTLY
     * NOT IMPLEMENTED CORRECTLY
     *
     * @param message
     */
    public void display(String message) {
        System.out.println(message);
        messageList.append(message + "\n");
    }

    public static void main(String[] args) {
        GUIConsole test = new GUIConsole("localhost", 5555);

    }
}
