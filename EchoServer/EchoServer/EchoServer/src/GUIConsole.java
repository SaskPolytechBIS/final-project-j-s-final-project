/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.io.*;

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

    private JLabel userIdLB = new JLabel("User Id: ", JLabel.RIGHT);
    private JLabel hostLB = new JLabel("Host: ", JLabel.RIGHT);
    private JLabel portLB = new JLabel("Port: ", JLabel.RIGHT);
    private JLabel messageLB = new JLabel("Message: ", JLabel.RIGHT);

    private JTextField userIdTxF = new JTextField("");
    private JTextField messageTxF = new JTextField("");
    private JTextArea hostTxF = new JTextArea("localhost");
    //private JTextArea hostTxF = new JTextArea("127.0.0.1");
    private JTextArea portTxF = new JTextArea("5555");
    
    private JTextArea messageList = new JTextArea();

    //Instance variables **********************************************
    /**
     * The instance of the client that created this ConsoleChat.
     */
    ChatClient client;

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

        setVisible(true);
        JPanel bottom = new JPanel();
        add("Center", messageList);
        add("South", bottom);

        //make the bottom part of the window a grid with
        //6 rows, 2 columns and 5 pixels of vertical and horizontal space
        bottom.setLayout(new GridLayout(6, 2, 5, 5));
        bottom.add(userIdLB);
        bottom.add(userIdTxF);
        bottom.add(hostLB);
        bottom.add(hostTxF);

        bottom.add(portLB);
        bottom.add(portTxF);

        bottom.add(messageLB);
        bottom.add(messageTxF);

        bottom.add(loginB);
        bottom.add(sendB);

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
                send("#setHost "+hostTxF.getText());
                
                //update port
                send("#setPort "+portTxF.getText());
                //update the user Id
                String userId = userIdTxF.getText();
                send("#login "+userId);
            }
        });
        logoffB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String userId = userIdTxF.getText();
                send("#logoff");
            }
        });
        try {
            client = new ChatClient(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't setup connection!!!!"
                    + " Terminating client.");
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
