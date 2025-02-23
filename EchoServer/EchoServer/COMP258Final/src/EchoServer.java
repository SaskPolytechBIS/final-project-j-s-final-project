import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class EchoServer extends AbstractServer {
    //Class variables *************************************************

    /**
     * The default port to listen on.
     */
    final public static int DEFAULT_PORT = 5555;

    //Constructors ****************************************************
    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port) {

        super(port);

        try {
            this.listen(); //Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }

    }

    //Instance methods ************************************************
    /**
     * This method handles any messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        //check that message is envelope
        if (msg instanceof Envelope) {
            handleClientCommand((Envelope) msg, client);

            //handle normal message
        } else {
            System.out.println("Message received: " + msg + " from " + client);

            String messageWithId = client.getInfo("userId") + ": " + msg;
            this.sendToAllClientsInRoom(messageWithId, client.getInfo("room").toString());
        }

    }

    public void handleClientCommand(Envelope env, ConnectionToClient client) {
        //#login <userId>
        if (env.getName().equals("login")) {
            //save the userId from env.msg into client hashmap as "userId"
            client.setInfo("userId", env.getMsg());
            //by default place all clients in the room commons
            client.setInfo("room", "commons");
            System.out.println(client.getInfo("userId") + " has logged in. "
                    + "they have been place in room " + client.getInfo("room"));
            

        }
        //#join <room>
        if (env.getName().equals("join")) {
            //set the new room for client
            client.setInfo("room", env.getMsg());
            //print out a message to say  what room they joined
            System.out.println(client.getInfo("userId") + " has joined room "
                    + client.getInfo("room"));
        }

        //#pm <User> <message>
        if (env.getName().equals("pm")) {
            //get user annd message
            String user = env.getArg();
            String pmMessage = env.getMsg().toString();

            sendToClientByUserId(pmMessage, user);
        }
        //#who
        if (env.getName().equals("who")) {
            //make who envelope
            //set name to who
            Envelope returnEnv = new Envelope("who", null, null);

            //get an arraylist<string> with all the user in the same room of client
            ArrayList<String> clientsInRoom = getAllClientInRoom(client.getInfo("room").toString());

            returnEnv.setMsg(clientsInRoom);

            //use client "send to client" to send envelope back to client
            try {
                client.sendToClient(returnEnv);
            } catch (Exception e) {
                System.out.println("Encounter an exception while handling #who command");
            }
        }

        //#ison 
        if (env.getName().equals("ison")) {
            //get user
            String user = env.getArg().toString();
            //get Room
            Envelope returnEnv = new Envelope("ison", user, null);
            String room = getRoomFromUserId(user);
            String message;
            if (user != "" && room != "") {
                message = user + " is on in room " + room;

            } else {

                message = user + " is not logged in";
            }
            //use client "send to client" to send envelope back to client
            try {
                client.sendToClient(message);
            } catch (Exception e) {
                System.out.println("Encounter an exception while handling #ison command");
            }
        }

        //#userstatus  
        if (env.getName().equals("userstatus")) {
            //make userstatus envelope
            //set name to userstatus
            Envelope returnEnv = new Envelope("userstatus", null, null);

            //get an arraylist<string> with all the user 
            ArrayList<String> userStatus = ListUserStatus();

            returnEnv.setMsg(userStatus);

            //use client "send to client" to send envelope back to client
            try {
                client.sendToClient(returnEnv);
            } catch (Exception e) {
                System.out.println("Encounter an exception while handling #userstatus command");
            }
        }
        
        // #ftpUpload
        // Save a file to the uploads folder
        if(env.getName().equals("ftpUpload")) 
        {
            // Get file name from the argument section.
            String fileName = env.getArg();                                     
            
            // Get file data as an array of bytes from content of an envelope.
            byte[] saveBytes = (byte[])env.getMsg();                        
            
            try
            {
                // Set path and file name to be saved.
                Path savePath = Paths.get("uploads/" + fileName);               

                // Write a file. (Note: with same name, the file will be replace).
                Files.write(savePath, saveBytes);                               
                
                client.sendToClient("The file has been saved.");
            }
            catch (IOException eio)
            {
                System.out.println(eio);
            }
        }
        
        // #ftplistSend 
        // all uploaded file names to requested client.
        if(env.getName().equals("ftplist")) 
        {
            // Create an array to hold file names.
            ArrayList<String> fileNames = new ArrayList<String>();              

            // Specify the path to the uploads folder.
            File savePath = new File("uploads");                                

            // List all files in the uploads folder.
            File[] files = savePath.listFiles();                                
            
            // Check if files is not null to avoid NullPointerException
            if (files != null)                                                  
            {
                for(File file : files)
                {           
                    //create array of file name
                    fileNames.add(file.getName());                              
                }
            }
            
            // Make an envelope
            Envelope envToSend = new Envelope("ftplist", "", fileNames);

            // Send envelope back.
            try {
                client.sendToClient(envToSend);
            } catch(Exception e) {
                System.out.println("Error when trying to send an envelope to the user.");
                System.out.println(e.toString());
            }
        }
        
        // Send a file to a requested client.
        if(env.getName().equals("ftpget")) 
        {
            // Get the file name.
            String fileName = env.getArg();
            
            try
            {
                // Get path of the file to be downloaded.
                Path filePath = Paths.get("uploads/" + fileName);               

                // Read file data as bytes from the path.
                byte[] bytes = Files.readAllBytes(filePath);                    
                
                // Create an envelope for client.
                Envelope envToSend = new Envelope("ftpget", fileName, bytes);   
                
                // Send envelope back.
                try {
                    client.sendToClient(envToSend);
                } catch(Exception e) {
                    System.out.println("Error when trying to send an envelope to the user.");
                    System.out.println(e.toString());
                }
            }
            catch (IOException eio)
            {
                System.out.println(eio);
            }
        }
    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * starts listening for connections.
     */
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * stops listening for connections.
     */
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
    }

    synchronized protected void cliendException(ConnectionToClient client, Throwable exception) {
        System.out.println("<Cliend disconnected>");
    }

    //Class methods ***************************************************
    /**
     * This method is responsible for the creation of the server instance (there
     * is no UI in this phase).
     *
     * @param args[0] The port number to listen on. Defaults to 5555 if no
     * argument is entered.
     */
    public static void main(String[] args) {
        int port = 0; //Port to listen on

        try {
            //read port from command line argruments
            port = Integer.parseInt(args[0]);// //Set port to 5555
            //port = DEFAULT_PORT; //Set port to 5555
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            //default the port number to 5555
            port = DEFAULT_PORT;

        }

        EchoServer sv = new EchoServer(port);

        try {
            sv.listen(); //Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }

    }

    protected void clientConnected(ConnectionToClient client) {

        System.out.println("<Client Connected:" + client + ">");

    }

    public void sendToAllClientsInRoom(Object msg, String roomName) {
        //get all connection to client and store them in a thread array
        Thread[] clientThreadList = getClientConnections();
        //loop through all clients
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient currentClient = (ConnectionToClient) clientThreadList[i];
            if (roomName.equals(currentClient.getInfo("room"))) {
                //try to sending message to client
                try {
                    currentClient.sendToClient(msg);
                } catch (Exception ex) {
                    System.out.println("ERROR - Could not send message to all clients!");
                }
            }
        }
    }

    public void sendToClientByUserId(Object msg, String userId) {
        //get all connection to client and store them in a thread array
        Thread[] clientThreadList = getClientConnections();
        //loop through all clients
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient currentClient = (ConnectionToClient) clientThreadList[i];
            //only send to client if their userId matchs the provided userId
            //try to sending message to client
            try {
                if (userId.equals(currentClient.getInfo("userId"))) {
                    currentClient.sendToClient(msg);
                }

            } catch (Exception ex) {
                System.out.println("ERROR - Could not send msg to clients!");
            }

        }
    }

    public ArrayList<String> getAllClientInRoom(String roomName) {
        //get all connection to client and store them in a thread array
        Thread[] clientThreadList = getClientConnections();
        ArrayList<String> userInRoom = new ArrayList<String>();
        //loop through all clients
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient currentClient = (ConnectionToClient) clientThreadList[i];
            //if In the same room
            if (roomName.equals(currentClient.getInfo("room"))) {
                //get all userId into arrayList
                userInRoom.add(currentClient.getInfo("userId").toString());

            }
        }

        return userInRoom;
    }

    public ArrayList<String> ListUserStatus() {
        //get all connection to client and store them in a thread array
        Thread[] clientThreadList = getClientConnections();
        ArrayList<String> userData = new ArrayList<String>();
        //loop through all clients
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient currentClient = (ConnectionToClient) clientThreadList[i];
            //get userId and roomName for each client
            try {
                //create message <user> - <room>
                String user = currentClient.getInfo("userId").toString();
                String roomName = currentClient.getInfo("room").toString();
                String returnMsg = user + " - " + roomName;
                //add into array
                userData.add(returnMsg);
            } catch (Exception ex) {
                System.out.println("ERROR - Could not list allClient data");
            }
        }
        //return array
        return userData;
    }

    public String getRoomFromUserId(String userId) {
        //get all connection to client and store them in a thread array
        Thread[] clientThreadList = getClientConnections();
        //loop through all clients
        String room = "";
        for (int i = 0; i < clientThreadList.length; i++) {
            ConnectionToClient currentClient = (ConnectionToClient) clientThreadList[i];
            //only the room that have userId
            //try to get room

            try {
                if (userId.equals(currentClient.getInfo("userId"))) {
                    room = currentClient.getInfo("room").toString();
                    break;
                }

            } catch (Exception ex) {
                System.out.println("ERROR - Could not get room from userId");
            }

        }
        return room;
    }
}
//End of EchoServer class
