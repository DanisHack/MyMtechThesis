package net.tinyos.PushWeb2;

import java.net.*;
import java.io.*;
import net.tinyos.util.PrintStreamMessenger;
import net.tinyos.message.Message;
import net.tinyos.message.MoteIF;
import net.tinyos.message.MessageListener;

public class WebServer {    
	private int WINDOW_SIZE=5;
	private String[] window;
	private int startReading;
	private int currentReading;
        MoteIF mote;
 

   public WebServer(int port, int group_id){	
	window = new String[WINDOW_SIZE];
	for(int i=0;i<WINDOW_SIZE;i++) 
			window[i] = "";	
	currentReading=0;

   	try {
   	    ServerSocket server = new ServerSocket(port);
   	    System.out.println("Accepting connections on port "
   			       + server.getLocalPort());
	    MoteIF mote = new MoteIF();//MoteIF represents a Java interface for sending and receiving messages to and from motes.
  		mote.registerListener(new SenseToRadioMsg(), new MessageReceive());
		
   	    while (true) {
   			  Socket connection = server.accept();
   			  new Thread(new RequestHandler(connection, this)).start();
   
   	    }
		
   	} 
   	catch (IOException ex) {
   	    System.err.println("Could not start server. Port Occupied");
   	}

  } 
		
public String getMessage() {
   StringBuffer buffer = new StringBuffer(); 
   for(int i=0; i<WINDOW_SIZE; i++) {				   
      	buffer.append(window[(currentReading+i)%WINDOW_SIZE]);
	}		   	 
  	return buffer.toString(); 
}
	
class MessageReceive implements MessageListener
 {
  public void messageReceived(int dest_addr, Message msg) { 
  		SenseToRadioMsg multiMsg;  
                 
  		if (msg instanceof SenseToRadioMsg) {     
 		   multiMsg = (SenseToRadioMsg) msg;
                   //System.out.println(multiMsg); 
                    //System.out.println("Working Directory = " +
                    //System.getProperty("user.dir"));
                   
		   synchronized (this) {
		   		window[currentReading] = multiMsg.toString();
			}
		   currentReading = (currentReading+1)%WINDOW_SIZE;		   	   	  				
 		} else {      
 		  throw new RuntimeException("Unknown type: " + msg);    
 		}  
 	}
 }



public static void main(String[] args) throws IOException {
		 int port = 9000;
		 int group_id = 136;
		BufferedReader console = new BufferedReader
			(new InputStreamReader(System.in));
			
		System.out.print("Enter Web Server port number [9000]: ");
		String portString = console.readLine();
		System.out.print("Enter group ID [136] : ");
		String groupIDString = console.readLine();

		try{			
			if (portString.length() != 0)			  
			   port = Integer.parseInt(portString);
			if (groupIDString.length() != 0)
			   group_id = Integer.parseInt(groupIDString);
			new WebServer(port, group_id);
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
    }
}

class RequestHandler implements Runnable {

    private Socket connection;
	WebServer server;
	OutputStream out;
	String CRLF = "\r\n";
        String path = "support/sdk/java/net/tinyos/PushWeb2/";
	boolean pending;

    public RequestHandler(Socket connection, WebServer server)
	{
		   this.connection = connection;
		   this.server = server;		   		   
    }



	private short parseValue(String request)
	{
	 int q = request.indexOf("?");		  			   
	 String entries = request.substring(q+1);
	 int r = Integer.parseInt(entries.substring(2,3));
	 int g = Integer.parseInt(entries.substring(6,7));
	 int y = Integer.parseInt(entries.substring(10));
	 return (short) (r+g+y);
	}
	
	private String success(int value)
	{
			   
	 String page = "<html><head>" +
	 		"<title>Cmd Server: Sensor Networks</title>" +
			"<link rel=stylesheet href=\"style.css\">" +
			"</head>" +
			"<body>" +
			"<h1>Congratulations!</h1>" +
			"The request is sent to all the sensor nodes." +
			"<ul><table bgcolor=gray>\n" +
			"<tr><td><font color=red>RED</font></td><td>" + ( ((value&4)!=0) ? "ON" : "OFF" ) + "</td></tr>" +
			"<tr><td><font color=green>GREEN</font></td><td>" + ( ((value&2)!=0) ? "ON" : "OFF" ) + "</td></tr>" +
			"<tr><td><font color=yellow>YELLOW</font></td><td>" + ( ((value&1)!=0) ? "ON" : "OFF" ) + "</td></tr>" +
			"</table></ul>\n" + 
			"Click <a href=\"index.html\">here</a> to send another value." +
			"</body>" +
			"</html>";
		return page;
	}

    public void run() {
	try {
	    out = new BufferedOutputStream
			(connection.getOutputStream());
	    InputStream in = new BufferedInputStream
			(connection.getInputStream());
	
	    StringBuffer buffer = new StringBuffer(80);
	    while (true) {
			int c = in.read();
			if (c == '\r' || c == '\n' || c == -1) break;
			buffer.append((char) c);
	    }
		
	    String request = buffer.toString();
            String[] fields = request.split(" ");
	    System.out.println("Method = " + fields[0]);
	    System.out.println("File = " + fields[1]);
	    System.out.println("Protocol = " + fields[2]);
	    System.out.println("request = " + request);

	   
	    if (request.toString().indexOf("/send") != -1) {
		    String statusLine = "HTTP/1.0 200 OK" + CRLF;
		    String serverName = "Sensor Server: " + CRLF;
		    String contentType = "Content-type: text/plain" + CRLF;
		    String headers = statusLine + serverName + contentType + CRLF;	
		    System.out.println("headers = " + headers);		
			out.write(headers.getBytes("ASCII"));
			out.write("<html><body><h1>Sensed Temperature Data Messages</h1><pre>".getBytes("ASCII"));
			out.flush();
			out.write(server.getMessage().getBytes("ASCII"));
			out.flush();			
	    }	
            if (request.toString().indexOf("/index.html") != -1) {
		    String fileName = fields[1].substring(1);
		   if (fileName.length() == 0)
		   	  fileName = "index.html";
		   String contents;	 
		   if (fileName.indexOf("?") != -1) {		   						
				CmdMsg cmd = new CmdMsg();
				short value = parseValue(fields[1]);
				cmd.set_value( value );
				server.mote.send(MoteIF.TOS_BCAST_ADDR, cmd);				
				contents = success(value);
		   } else { // read file
		   	  fileName = path + fileName;		
			  BufferedReader file =
			  	new BufferedReader(new FileReader(fileName));
			  String line = file.readLine();
			  StringBuffer contentsBuffer = new StringBuffer();
			  while(line != null) {
					   contentsBuffer.append(line + "\n");
					   line = file.readLine();
			   }
			   contents = contentsBuffer.toString();
		   }
			   
		    String statusLine = "HTTP/1.0 200 OK" + CRLF;
		    String serverName = "Sensor Server: " + CRLF;
		    String contentType = "Content-type: text/html" + CRLF;
		    String headers = statusLine + serverName + contentType + CRLF;	
		    System.out.println("headers = " + headers);		
			out.write(headers.getBytes("ASCII"));			
			out.write(contents.getBytes("ASCII"));
			out.flush();




						
	    }	      
	    out.flush();
	}  
	catch(IOException e){ e.printStackTrace(); }	
	
	finally {
	    if (connection != null) {
		   try{
		       connection.close();
			} catch(IOException e){e.printStackTrace(); }
	    } 
		
	} 

    } 
	
} 
