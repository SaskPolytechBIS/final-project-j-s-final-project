
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.*;

/**
 * This class overrides some of the methods defined in the abstract superclass
 * in order to give more functionality to the client.
 */
public class ChatClient extends AbstractClient {
    //Instance variables **********************************************

    /**
     * The interface type variable. It allows the implementation of the display
     * method in the client.
     */
    ChatIF clientUI;
    // A file to save on the server as an array of bytes. 
    private byte[] fileBytes = null;
    //Constructors ****************************************************
    /**
     * Constructs an instance of the chat client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     * @param clientUI The interface type variable.
     */
    public ChatClient(String host, int port, ChatIF clientUI)
            throws IOException {
        super(host, port); //Call the superclass constructor >> abstract 
        this.clientUI = clientUI;
        //openConnection();
    }

    //Instance methods ************************************************
    /**
     * This method handles all data that comes in from the server.
     *
     * @param msg The message from the server.
     */
    public void handleMessageFromServer(Object msg) {
        //check if message is an envelope
        if (msg instanceof Envelope) {
            handleServerCommand((Envelope) msg);
        } else {

            //else handle normal
            clientUI.display(msg.toString());
        }
    }

    public void handleServerCommand(Envelope env) {
        if (env.getName().equals("who")) {
            //get out the arrayList of clients in room
            ArrayList<String> clientsInRoom = (ArrayList<String>) env.getMsg();

            clientUI.display("--User In Room--");
            //loop through arrayList
            for (int i = 0; i < clientsInRoom.size(); i++) {

                //print out userId
                //get(i) is to arrayList as [i] is to arrays
                clientUI.display(clientsInRoom.get(i));
            }
        } else if (env.getName().equals("userstatus")) {
            //get out the arrayList of clients in room
            ArrayList<String> userStatus = (ArrayList<String>) env.getMsg();

            clientUI.display("--User Status--");
            //loop through arrayList
            for (int i = 0; i < userStatus.size(); i++) {

                //print out userId
                //get(i) is to arrayList as [i] is to arrays
                clientUI.display(userStatus.get(i));
            }
        // ftplist : List all uploaded files.
        } else if(env.getName().equals("ftplist")) {
            
            clientUI.display("<Uploaded File(s)>: ");
            
            for (String name : (ArrayList<String>)env.getMsg()) {
                //System.out.println(name);
                clientUI.display(name);
            }
        }
        
        // ftplist : List all uploaded files.
        else if(env.getName().equals("ftpget"))
        {
            String fileName = env.getArg();                           // Get the file name.
            
            byte[] downloadBytes = (byte[])env.getMsg();          // Get file data as an array of bytes from content of an envelope.
            
            try
            {
                Path savePath = Paths.get("downloads/" + fileName);             // Set path and file name to be saved.

                Files.write(savePath, downloadBytes);                  // Write a file. Note: with same name, the file will be replace.
                
                clientUI.display("The file has been downloaded.");
            }
            catch (IOException eio)
            {
                System.out.println(eio);
            }
        }
    }

    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(String message) {

        if (message.charAt(0) == '#') {

            handleClientCommand(message);

        } else {
            try {
                sendToServer(message);
            } catch (IOException e) {
                clientUI.display("Could not send message to server.  Terminating client.......");
                quit();
            }
        }
    }

    /**
     * This method terminates the client.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {
        }
        System.exit(0);
    }

    public void connectionClosed() {

        System.out.println("Connection closed");

    }

    protected void connectionException(Exception exception) {

        System.out.println("Server has shut down");

    }

    public void handleClientCommand(String message) {

        if (message.equals("#quit")) {
            clientUI.display("Shutting Down Client");
            quit();

        }

        if (message.equals("#logoff")) {
            clientUI.display("Disconnecting from server");
            try {
                closeConnection();
            } catch (IOException e) {
            };

        }
        //
        if (message.indexOf("#setHost") > 0) {

            if (isConnected()) {
                clientUI.display("Cannot change host while connected");
            } else {
                setHost(message.substring(8, message.length()));
            }

        }

        //#SETpORT
        //if Cliend already connect >> do nothing
        //if cliend
        if (message.indexOf("#setPort") >= 0) {

            if (isConnected()) {
                clientUI.display("Cannot change port while connected");
            } else {

                /*
                //getport number from message
                String s = message.substring(8, message.length()).trim();
                //trim
                s.trim();//or change to 9 in line above
                
                //parse the interger from string
                int portNum = Integer.parseInt(s);
                //set the port number
                setPort(portNum);
                 */
                setPort(Integer.parseInt(message.substring(8, message.length()).trim()));
            }

        }
        //if user is already connected >> do nothing
        //otherwise opens the connection to server
        //create an envelope and sends in the username to the server
        //#login <userId>
        if (message.indexOf("#login") >= 0) {

            if (isConnected()) {
                clientUI.display("already connected");
            } else {

                try {
                    //openConnection is reasponsibke for connection
                    //to the server
                    openConnection();

                } catch (IOException e) {
                    clientUI.display("failed to connect to server.");
                }
                //get the username

                //#login test
                String userId = message.substring(7, message.length());

                //create envelope
                Envelope env = new Envelope("login", null, userId);

                //send the envelope to the server
                try {
                    sendToServer(env);
                    clientUI.display("User:"+userId+" has log in.");
                } catch (IOException e) {
                    clientUI.display("Error when try to send envelope to server");
                }
            }
        }
        //#join <room>
        if (message.indexOf("#join") >= 0) {
            //check if we are connected
            if (isConnected()) {
                //get the name of the room
                String roomName = message.substring(6, message.length()).trim();
                //create envelope
                Envelope env = new Envelope("join", null, roomName);
                //send the envelope to the server
                try {
                    sendToServer(env);
                } catch (IOException e) {
                    clientUI.display("Error when try to send envelope to server");
                }
            } else {
                clientUI.display("You must connected to server to use");
            }

        }
        //#pm <User> <message>
        if (message.indexOf("#pm") >= 0) {
            //check if we are connected
            if (isConnected()) {
                //#pm Mike Hi mike

                //pass out the user and the message 
                String userAndMsg = message.substring(4, message.length()).trim();

                //Mike Hi mike
                String user;
                String pmMessage;
                //seperate user and message into seperate variable and trim them!
                user = userAndMsg.substring(0, userAndMsg.indexOf(" ")).trim();
                pmMessage = userAndMsg.substring(userAndMsg.indexOf(" ") + 1, userAndMsg.length()).trim();
                //create an envelope
                Envelope env = new Envelope("pm", user, pmMessage);
                //send the envelope
                try {
                    sendToServer(env);
                } catch (IOException e) {
                    clientUI.display("Error when try to send envelope to server");
                }
            } else {
                clientUI.display("You must connected to server to use");
            }
        }

        //#who 
        if (message.indexOf("#who") >= 0) {
            //check if we are connected
            if (isConnected()) {
                //create an envelope
                Envelope env = new Envelope("who", null, null);
                //send the envelope to the server
                try {
                    sendToServer(env);
                } catch (IOException e) {
                    clientUI.display("Error when try to send envelope to server");
                }
            } else {
                clientUI.display("You must connected to server to use");
            }

        }

        //#ison <user> - prints a message saying the user is on and what room they are in
        if (message.indexOf("#ison") >= 0) {
            //check if we are connected
            if (isConnected()) {
                //#ison Mike 

                //pass out the user and the message 
                String user = message.substring(5, message.length()).trim();

                if (user != "") {
                    //create an envelope
                    Envelope env = new Envelope("ison", user, null);
                    //send the envelope to the server
                    try {
                        sendToServer(env);
                    } catch (IOException e) {
                        clientUI.display("Error when try to send envelope to server");
                    }
                } else {
                    clientUI.display("Error! user must have value");
                }

            } else {
                clientUI.display("You must connected to server to use");
            }

        }

        //userStatus – prints a list off all connected users and what room they are in
        if (message.indexOf("#userstatus") >= 0) {
            //check if we are connected
            if (isConnected()) {
                //create an envelope
                Envelope env = new Envelope("userstatus", null, null);
                //send the envelope to the server
                try {
                    sendToServer(env);
                } catch (IOException e) {
                    clientUI.display("Error when try to send envelope to server");
                }
            } else {
                clientUI.display("You must connected to server to use");
            }

        }
        
        // Send a file to save to the server.
        if(message.indexOf("#ftpUpload") >= 0) {
            
            if(isConnected()) {
                // Parse file name as argument.
                String fileName = message.substring(10).trim();
                
                // Create an envelope.
                Envelope env = new Envelope("ftpUpload", fileName, fileBytes);

                // Send an envelope.
                sendEnvelope(env);
            }
            else {
                clientUI.display("Must be connected to upload a file.");
            }
        }
        
        // Display all the files that have been uploaded to the server
        if (message.equals("#ftplist")) {
            
            if(isConnected()) {
                // Create an envelope.
                Envelope env = new Envelope("ftplist", "", "");

                // Send an envelope.
                sendEnvelope(env);
            }
            else {
                clientUI.display("Must be connected to request information.");
            }
        }
        
        // Download the file specified onto the client in a folder called downloads
        if(message.indexOf("#ftpget") >= 0) {
            
            if(isConnected()) {
                // Parse file name as argument.
                String fileName = message.substring(7).trim();
                
                // Create an envelope.
                Envelope env = new Envelope("ftpget", fileName, "");

                // Send an envelope.
                sendEnvelope(env);
            }
            else {
                clientUI.display("Must be connected to download a file.");
            }
        }
    }
 /**
     * This function sends an Envelope (command) to the server.
     * @param env : envelope (command).
     */
    public void sendEnvelope(Envelope env) {
        try {
            sendToServer(env);
        } catch (IOException e) {
            clientUI.display("Fail to send Envelope.");
        }
    }

    /**
     * This function set fileBytes for sending in an Envelope.
     * @param fileBytes : file data in bytes.
     */
    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }
}
//End of ChatClient class
