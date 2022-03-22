package ma.aui.sse.paradigms.communication.fx.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FxClient {

	private static String host = "localhost";
	private static int port = 81;
	private static int numberOfSeconds = 30;

	public static void main(String[] args) throws Exception {

		String command = args[0];
		String fileName = args.length >= 2 ? args[1] : "";

		/*
		 * if (args.length == 2) fileName = args[1]; else fileName = "";
		 */

		if (command.equals("d")) {

			download(fileName);

		} else if (command.equals("u")) {

			upload(fileName);

		} else if (command.equals("ll")) {

			localList();

		}
		else if (command.equals("rl")) {

			remoteList();

		}else if (command.equals("b")) {

			backup();

		} else if (command.equals("s")) {

			ScheduledExecutorService executorService;
			executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleAtFixedRate(() -> {
				try {
					synch();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}, 0, numberOfSeconds, TimeUnit.SECONDS);

		} else {
			// To do
		}
	}

	private static void localList() {

		try {
			File folder = new File("ClientShare/");
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					System.out.println("Local file " + listOfFiles[i].getName());
				} else if (listOfFiles[i].isDirectory()) {
					System.out.println("Directory " + listOfFiles[i].getName());
				}
			}

		} catch (Exception ex) {

			System.out.println("Error happend on local listenning");

		}
	}

	private static void backup() {

		try {
			File folder = new File("ClientShare/");
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					String currentFileName = listOfFiles[i].getName();
					System.out.println("Uploading " + currentFileName + " ...");
					upload(currentFileName);
					System.out.println("End of uploading " + currentFileName);
				} else if (listOfFiles[i].isDirectory()) {
					System.out.println("Directory " + listOfFiles[i].getName());
				}
			}

		} catch (Exception ex) {

			System.out.println("Error happend on backupping");

		}
	}

	private static void upload(String fileName) throws UnknownHostException, IOException {

		try (Socket connectionToServer = new Socket(host, port)) {

			OutputStream out = connectionToServer.getOutputStream();

			BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));
			DataOutputStream dataOut = new DataOutputStream(out);

			try {
				FileInputStream fileIn = new FileInputStream("ClientShare/" + fileName);
				int fileSize = fileIn.available();

				String header = "upload " + fileName + " " + fileSize + "\n";
				headerWriter.write(header, 0, header.length());
				headerWriter.flush();

				byte[] bytes = new byte[fileSize];
				fileIn.read(bytes);

				fileIn.close();

				dataOut.write(bytes, 0, fileSize);

			} catch (Exception ex) {

				System.out.println("Error happend on uploading");

			} finally {
				connectionToServer.close();
			}
		}
	}

	private static void download(String fileName) throws UnknownHostException, IOException {
		try (Socket connectionToServer = new Socket(host, port)) {

			try {
				InputStream in = connectionToServer.getInputStream();
				OutputStream out = connectionToServer.getOutputStream();
				BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
				BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));
				DataInputStream dataIn = new DataInputStream(in);
				String header = "download " + fileName + "\n";
				headerWriter.write(header, 0, header.length());
				headerWriter.flush();

				header = headerReader.readLine();

				if (header.equals("NOT FOUND")) {
					System.out.println("We're extremely sorry, the file you specified is not available!");
				} else {
					StringTokenizer strk = new StringTokenizer(header, " ");

					String status = strk.nextToken();

					if (status.equals("OK")) {

						String temp = strk.nextToken();

						int size = Integer.parseInt(temp);

						byte[] space = new byte[size];

						dataIn.readFully(space);

						try (FileOutputStream fileOut = new FileOutputStream("ClientShare/" + fileName)) {
							fileOut.write(space, 0, size);
						}

					} else {
						System.out.println("You're not connected to the right Server!");
					}

				}
			} catch (Exception ex) {

				System.out.println("Error happend on downloading");

			} finally {
				connectionToServer.close();
			}
		}

	}

	private static void remoteList() throws UnknownHostException, IOException {
		try (Socket connectionToServer = new Socket(host, port)) {

			OutputStream out = connectionToServer.getOutputStream();

			BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));

			try {
				InputStream in = connectionToServer.getInputStream();
				BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
				String header = "list \n";
				headerWriter.write(header, 0, header.length());
				headerWriter.flush();

				header = headerReader.readLine();
				StringTokenizer strk = new StringTokenizer(header, " ");

				String status = strk.nextToken();

				connectionToServer.close();

				if (status.equals("OK")) {
					String fileName = strk.nextToken();

					while (!fileName.equals("END")) {

						System.out.println("Remote file " + fileName);

						fileName = strk.nextToken();

					}

				} else {
					System.out.println("You're not connected to the right Server!");
				}

			} catch (Exception ex) {

				System.out.println("Error happend on remote list");

			}
		}

	}
	
	private static void synch() throws UnknownHostException, IOException {
		try (Socket connectionToServer = new Socket(host, port)) {

			OutputStream out = connectionToServer.getOutputStream();

			BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));

			try {
				InputStream in = connectionToServer.getInputStream();
				BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
				String header = "list \n";
				headerWriter.write(header, 0, header.length());
				headerWriter.flush();

				header = headerReader.readLine();
				StringTokenizer strk = new StringTokenizer(header, " ");

				String status = strk.nextToken();

				connectionToServer.close();

				if (status.equals("OK")) {
					String fileName = strk.nextToken();

					while (!fileName.equals("END")) {
						System.out.println("Downloading " + fileName + " ...");

						download(fileName);

						System.out.println("Finish downloading " + fileName);

						fileName = strk.nextToken();

					}

				} else {
					System.out.println("You're not connected to the right Server!");
				}

			} catch (Exception ex) {

				System.out.println("Error happend on synch");

			}
		}

	}

}
