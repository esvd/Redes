package edu.esvd;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

public class UDPClient {
	private DatagramSocket socket = null;
	private String filePath = "C:\\Users\\escvd\\images1.jpg";
	private String hostname;
	private int port;

	public static void main(String[] args) {
		try {
			UDPClient client = new UDPClient();
			client.startClient();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public UDPClient() throws SocketException {
		this.hostname = "localhost";
		this.port = 9500;
		this.socket = new DatagramSocket();
	}

	private void startClient() throws InterruptedException {
		System.out.println("Client started...");
		try {
			InetAddress address = InetAddress.getByName(hostname);
			socket = new DatagramSocket();
			byte[] recvHash = null;

			boolean isHashRecv = false;
			
			DatagramPacket request = new DatagramPacket(new byte[1], 1, address, port);
			socket.send(request);
			
			while (true) {
				byte[] buffer = new byte[8192];
				DatagramPacket response = new DatagramPacket(buffer, buffer.length);
				socket.receive(response);

				buffer = response.getData();

				final int responseLength = response.getLength();
				if (!isHashRecv) {
					recvHash = new byte[responseLength];
					for (int i = 0; i < responseLength; ++i) {
						recvHash[i] = buffer[i];
					}
					isHashRecv = true;
				} else {
					byte[] image = new byte[responseLength];

					for (int i = 0; i < responseLength; ++i) {
						image[i] = buffer[i];
					}

					InputStream is = new ByteArrayInputStream(image);
					BufferedImage recvImage = ImageIO.read(is);
					ImageIO.write(recvImage, "jpg", new File(filePath));
					recvImage.flush();

					if (recvHash != null) {
						BufferedImage bufImg = ImageIO.read(new File(this.filePath));
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ImageIO.write(bufImg, "jpg", baos);
						bufImg.flush();
						baos.close();

						/* with Base64 */
//						byte[] imageHash1 = Base64.getEncoder().encode(image);
//						String decodedHash = new String(imageHash1);
//						String recvHashBytesAsString = new String(recvHash);
//						
//						if (decodedHash.equals(recvHashBytesAsString)) {
//							System.out.println("loaded image equals to received");
//						}

						/* with MessageDigest */
						byte[] fromFileImageHash = this.hashBytes(baos.toByteArray());
						String fromFileImageHashString = this.hashToString(fromFileImageHash);

						byte[] recvImageHash = this.hashBytes(image);
						String recvImageHashString = this.hashToString(recvImageHash);

						String recvHashString = this.hashToString(recvHash);

						if (recvHashString.equals(recvImageHashString)
								&& fromFileImageHashString.equals(recvImageHashString)) {
							System.out.println("loaded file image: " + fromFileImageHashString);
							System.out.println("received image bytes: " + recvImageHashString);
							System.out.println("received hash: " + recvHashString);
						}

					}
					isHashRecv = false;
				}
			}
		} catch (SocketTimeoutException ex) {
			System.out.println("Timeout error: " + ex.getMessage());
			ex.printStackTrace();
		} catch (IOException ex) {
			System.out.println("Client error: " + ex.getMessage());
			ex.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("Image is null");
			e.printStackTrace();
		} finally {
			this.socket.close();
		}
	}

	private byte[] hashBytes(byte[] bytes) {
		byte[] hash = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			hash = md.digest(bytes);
			md.reset();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash;
	}

	public String hashToString(byte[] hashInBytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : hashInBytes) {
			sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}
}
