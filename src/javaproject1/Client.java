package javaproject1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

//import SimpleDraw.LineDrawer;
//import SimpleDraw.PositionRecorder;


public class Client extends Thread implements ActionListener {

	String history = "";
	static List<ChatMessage> chatHistory = new ArrayList<ChatMessage>();
	ChatMessage myObject;
	boolean sendingdone = false, receivingdone = false;
	Scanner scan;
	Socket socketToServer;
	ObjectOutputStream myOutputStream;
	ObjectInputStream myInputStream;
	Frame f;
	Frame f1;
	TextArea ta;
	BufferedWriter fileWrite;
	Frame drawFrame;
	int mode = 0;
	public volatile int mynumber;

	private JPanel northPanel = new JPanel();
	private JPanel DrawingPanel = new JPanel();
	private JButton connectBtn = new JButton();
	private JButton userlistBtn = new JButton();
	private JButton Draw = new JButton();
	private JButton Privatebtn = new JButton();
	private JButton messageHistoryBtn = new JButton("Message History");
	private JTextField nameField = new JTextField();
	private JTextField tf = new JTextField();
	//private JTextField tf1 = new JTextField();
	private JTextField tfServer = new JTextField();
	private JTextField tfPort = new JTextField();
	public Client() {

		// ############## FRAME SETTINGS ##############

		f = new Frame();
		f.setSize(600, 550);
		f.setLayout( new BorderLayout() );
		northPanel.setLayout( new GridLayout( 1, 4 ) );
		f.add( northPanel, BorderLayout.NORTH );
		f.setTitle("Chat Client");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		// ############## FRAME SETTINGS ##############
		/*
		 * f1 = new Frame ();
		 *f1.setSize(300,400);
		 *f1.setLayout(new BorderLayout());
		 *f1.setTitle("White Board");
		 *f1.setVisible(false);
		 *f1.addWindowListener(new WindowAdapter() {
		 *	public void windowClosing(WindowEvent we) {
		 *		System.exit(0);
		 *	}
		});*/
		// ############## CONNECT BUTTON ##############
		
		

		connectBtn = new JButton("Connect");
		northPanel.add(connectBtn);
		connectBtn.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						connectionBtnFunction();
					}
				}
				);
		
		Privatebtn = new JButton("Private Chat");
		northPanel.add(Privatebtn);
		Privatebtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				mode = 1;
				privatechat();
			}
		});

		// ################ USER LIST BUTTON #############

		userlistBtn = new JButton("Userlist");
		northPanel.add(userlistBtn);
		userlistBtn.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//ta.append("Users currently In chat : \n");
						mode = 0;
						getUserList();
					}
				}
				);

		// ############# MESSAGE HISTORY BUTTON   ############	

		messageHistoryBtn = new JButton("History");
		northPanel.add(messageHistoryBtn);
		messageHistoryBtn.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							writeToFile();
							//File dir = new File(".");
							//File fin = new File(dir.getCanonicalPath() + File.separator + "in.txt");
							//OpenHistory(fin);
						}
						catch(IOException eee){
						}
					}
				}
				);

		// ############# Drawing Button  #############

		Draw = new JButton("Draw");
		northPanel.add(Draw);
		Draw.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Drawing();
			}
		});
		userlistBtn.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Drawing();
					}
				}
				);

		// ######## TEXT FIELD FOR USER NAME  ########

		nameField = new JTextField("");
		northPanel.add(nameField);
		nameField.requestFocus();

		// ######### USER CHAT MESSAGE INPUT #########

		tf = new JTextField();
		tf.addActionListener(this);
		f.add(tf, BorderLayout.SOUTH);
		
		//tf1 = new JTextField();
		//tf1.addActionListener(this);
		//f.add(tf1, BorderLayout.SOUTH);

		// ############# CHAT TEXT AREA ##############

		ta = new TextArea();
		f.add(ta, BorderLayout.CENTER);
		
		f.setVisible(true);
	}

	// ########### TO HANDLE TEXT INPUT ###########

	//for sending the chat message to server
	public void actionPerformed(ActionEvent ae) {
		myObject = new ChatMessage();
		//sets name for ChatMessage
		myObject.setName(nameField.getText());
		//gets the text from the chat text area and puts it in the ChatMessage 
		myObject.setMessage(tf.getText());
		//Resets the column for typing message again
		tf.setText("");
		try {
			myOutputStream.reset();
			//writes the object to output stream
			myOutputStream.writeObject(myObject);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}

	}
	/**
	 * This method is used to create a file on the server
	 */
	private void saveHistory(ChatMessage chatMessage) {
		try {
			File file = new File("javaio-appendfile.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();

			}
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.append(chatMessage.getName() + ":"
					+ chatMessage.getMessage() + "\n");
			bufferedWriter.close();
			fileWriter.close();
			// true = append file
			System.out.println("Done");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	// ############ RUN METHOD ###################

	public void run()  {
		
		System.out.println("Listening for messages from server . . . ");

		try {
			//keeps checking for input from server 
			while (!receivingdone) {
				myObject = (ChatMessage) myInputStream.readObject();
				//to display users online
				if(!myObject.getMessage().contains("Mode_Private") && !myObject.getMessage().contains("Mode_Public"))
				{
					if(myObject.getName().equals("Users Currently in Chatroom : ")){
					ta.append(mode+" "+myObject.getName() + " \n" + myObject.getMessage() + "\n");
				}
				else{
					//to display any other message
					if(myObject.getMessage().contains("private_msg"))
					{
					if(myObject.getMessage().endsWith("handler"+mynumber))
					 {
					ta.append(myObject.getName() + " : " + myObject.getMessage().replace("private_msg", "").replace("handler"+mynumber, "")+"\n");
					storeMessage(myObject.getName(), myObject.getMessage());
					 }
					}else{
						ta.append(myObject.getName() + " : " + myObject.getMessage()+"\n");
						storeMessage(myObject.getName(), myObject.getMessage());
						
					}
				}
			}
				//userlist.add(myObject.getName());

			}
		} catch (IOException ioe) {
			System.out.println("IOE: " + ioe.getMessage());
		} catch (ClassNotFoundException cnf) {
			System.out.println(cnf.getMessage());
		}
	}

	void toConnect() throws Exception {
		//to establish socket connection
		nameField.setVisible(false);
		f.setTitle(nameField.getText());
		socketToServer = new Socket("localhost", 5070);
		myOutputStream = new ObjectOutputStream(socketToServer.getOutputStream());
		myInputStream = new ObjectInputStream(socketToServer.getInputStream()); 
		connected();
		start();
	}

	// ############### CONNECT / DISCONNECT BUTTON ##############
	void connectionBtnFunction() {
		try {
			
			File file1 = new File("count.txt");
			if(file1.exists())
			{
			BufferedReader br = new BufferedReader(new FileReader("count.txt"));
			String str = br.readLine();
			System.out.println(str);
			br.close();
			mynumber = Integer.parseInt(str);
			BufferedWriter bw = new BufferedWriter(new FileWriter("count.txt"));
			int mnum = mynumber+1;
			bw.write(Integer.toString(mnum));
			bw.flush();
			}else
			{
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter("count.txt"));
				bw.write("1");
				bw.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			}
			if (connectBtn.getLabel() == "Connect") {

				toConnect();
				connectBtn.setLabel("Disconnect");

			} else if (connectBtn.getLabel() == "Disconnect") {

				disconnectSignal();
				connectBtn.setLabel("Connect");
				socketToServer.close();
				System.exit(0);

			}
		} catch (Exception ee) {
		}
	}
	// ############### TO DISPLAY CONNECTED ###################

	void connected(){
		myObject = new ChatMessage();
		myObject.setName(nameField.getText());
		myObject.setMessage("-Has Joined-");

		try {
			myOutputStream.reset();
			myOutputStream.writeObject(myObject);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	// ################# TO GET USERLIST ####################

	void getUserList(){
		myObject = new ChatMessage();
		myObject.setName(nameField.getText());
		myObject.setMessage("Send_List"+" Mode_Public");

		try {
			myOutputStream.reset();
			myOutputStream.writeObject(myObject);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
	
	
	void privatechat()
	{
		myObject = new ChatMessage();
		myObject.setName(nameField.getText());
		myObject.setMessage("SELECT HANDLER"+" Mode_Private");

		try {
			myOutputStream.reset();
			myOutputStream.writeObject(myObject);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	// ################# TO DISCONNECT ######################

	void disconnectSignal(){
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("count.txt"));
			String str = br.readLine();
			System.out.println(str);
			br.close();
			int nnum = Integer.parseInt(str);
			BufferedWriter bw = new BufferedWriter(new FileWriter("count.txt"));
			--nnum;
			--nnum;
			bw.write(Integer.toString(nnum));
			bw.flush();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		myObject = new ChatMessage();
		myObject.setName(nameField.getText());
		myObject.setMessage("Disconnect_me");

		try {
			myOutputStream.reset();
			myOutputStream.writeObject(myObject);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}

	}

	// ############# TO WRITE HISTORY IN TEXT FILE ##############

	void writeToFile() throws IOException{
		//String fileName = f.getTitle() + ".txt";
		//fileWrite = new BufferedWriter( new FileWriter(fileName));
		//fileWrite.write(history);
		//System.out.println(fileName + " File Writing Successful!!");
		//System.out.println("Data in file :\n" + history);
		//fileWrite.flush();
		//File file = new File(fileName);
		//java.awt.Desktop.getDesktop().open(file);

		File file = new File("javaio-appendfile.txt");
		System.out.println(file.getAbsolutePath() + " Realteive "+ file.getPath());
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();

		}
		String line = null;
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while ((line = bufferedReader.readLine()) != null) {
			System.out.println(line);
			String[] data = line.split(":");
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setMessage(data[1]);
			chatMessage.setName(data[0]);
			myOutputStream.writeObject(chatMessage);
		}
		// receivingdone = true;
	}

	// ################# TO STORE HISTORY ####################

	void storeMessage(String name, String message){
		history = history + name + " : " + message + "\n";

	}


	// ################# DRAWING #######################

	void Drawing(){
		// Drawing.
	}

	// #################### MAIN METHOD  ##################

	public static void main(String[] arg) {

		Client c = new Client();

	}
	public void newFrame(){
		drawFrame = new Frame();
		drawFrame .setTitle("Drawing");
		drawFrame .setSize(100, 100);
		drawFrame .setBackground(Color.white);
		drawFrame .setVisible(true);
		//addMouseListener(new PositionRecorder());
		// addMouseMotionListener(new LineDrawer());
	}

}