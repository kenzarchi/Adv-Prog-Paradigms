package ma.aui.sse.paradigms.communication.fx.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class FxServer {

	public static void main(String[] args) throws Exception {

		try (ServerSocket ss = new ServerSocket(81)) {
			while (true) {
				System.out.println("Server waiting...");
				Socket connectionFromClient = ss.accept();
				System.out.println(
						"Server got a connection from a client whose port is: " + connectionFromClient.getPort());

				try {
					InputStream in = connectionFromClient.getInputStream();
					OutputStream out = connectionFromClient.getOutputStream();

					String errorMessage = "NOT FOUND\n";

					BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
					BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));

					DataInputStream dataIn = new DataInputStream(in);
					DataOutputStream dataOut = new DataOutputStream(out);

					String header = headerReader.readLine();
					StringTokenizer strk = new StringTokenizer(header, " ");

					String command = strk.nextToken();

					String fileName = strk.hasMoreTokens() ? strk.nextToken() : "";

					if (command.equals("download")) {
						try {
							FileInputStream fileIn = new FileInputStream("ServerShare/" + fileName);
							int fileSize = fileIn.available();
							header = "OK " + fileSize + "\n";

							headerWriter.write(header, 0, header.length());
							headerWriter.flush();

							byte[] bytes = new byte[fileSize];
							fileIn.read(bytes);

							fileIn.close();

							dataOut.write(bytes, 0, fileSize);

						} catch (Exception ex) {
							headerWriter.write(errorMessage, 0, errorMessage.length());
							headerWriter.flush();

						} finally {
							connectionFromClient.close();
						}
					} else if (command.equals("upload")) {

						String fileSize = strk.nextToken();

						int size = Integer.parseInt(fileSize);

						byte[] space = new byte[size];

						dataIn.readFully(space);

						try (FileOutputStream fileOut = new FileOutputStream("ServerShare/" + fileName)) {
							fileOut.write(space, 0, size);
						}

					} else if (command.equals("list")) {

						File folder = new File("ServerShare/");
						File[] listOfFiles = folder.listFiles();
						String listFileNames = "";
						for (int i = 0; i < listOfFiles.length; i++) {
							if (listOfFiles[i].isFile() && !listOfFiles[i].getName().startsWith(".")) {
								listFileNames += listOfFiles[i].getName() + " ";
							}
						}

						header = "OK " + listFileNames + "END \n";

						headerWriter.write(header, 0, header.length());
						headerWriter.flush();

					} else {

						System.out.println("Connection got from an incompatible client");

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
