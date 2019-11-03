package edu.esvd;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

public class UDPServer {
	private DatagramSocket socket;

	public UDPServer(int port) throws SocketException {
		socket = new DatagramSocket(port);
	}

	public static void main(String[] args) {
		String filePath = "C:\\Users\\escvd\\images.jpg";
		int port = 9500;

		try {
			UDPServer server = new UDPServer(port);
			server.service(filePath);
		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	private void service(String filePath) throws IOException {
		System.out.println("Server started...");
		while (true) {
			DatagramPacket request = new DatagramPacket(new byte[1], 1);
			socket.receive(request);

			BufferedImage image = ImageIO.read(new File(filePath));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			baos.flush();

			byte[] imageBytes = baos.toByteArray();
			
			/* with MessageDigest */
			byte[] hash = hashBytes(imageBytes);
			
			/* with Base64 */
//			byte[] hash = Base64.getEncoder().encode(imageBytes);

			InetAddress clientAddress = request.getAddress();
			final int clientPort = request.getPort();

			DatagramPacket hashPacket = new DatagramPacket(hash, hash.length, clientAddress, clientPort);
			socket.send(hashPacket);
			
			DatagramPacket dataPacket = new DatagramPacket(imageBytes, imageBytes.length, clientAddress, clientPort);
			System.out.println("image length: " + imageBytes.length);
			socket.send(dataPacket);
		}
	}

	private byte[] hashBytes(byte[] file) {
		byte[] hash = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			hash = md.digest(file);
			md.reset();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash;
	}
}
