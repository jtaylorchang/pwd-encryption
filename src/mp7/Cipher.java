package mp7;

import java.util.Scanner;

public class Cipher {

	private String sequence = "";
	private String[] keys = { " ", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
			"R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
	private String[] values = new String[keys.length];

	/**
	 * Generate a unique sequence using the password
	 * 
	 * @param password
	 */
	public Cipher(final String password) {

	}

	/**
	 * Generate a sequence unique to the password
	 * 
	 * @param password
	 */
	public String generateSequence(final String password) {
		return "TODO";
	}

	/**
	 * Generate the key value pairs
	 * 
	 * @param sequence
	 */
	public void generateKV(final String sequence) {
		this.values = new String[this.keys.length];
		for(int i = 0; i < sequence.length() / 2; i++) {
			String sV = sequence.substring(i * 2, i * 2 + 2);
			int v = Integer.parseInt(sV);
			this.values[i] = this.keys[v];
		}
	}

	public static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	
	

	/**
	 * Encrypt
	 * 
	 * @param message
	 * @return
	 */
	public String encrypt(final String message) {
		return "TODO";
	}

	/**
	 * Undo encrypt
	 * 
	 * @param message
	 * @return
	 */
	public String decrypt(final String message) {
		return "TODO";
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("What is your password?");
		String password = scanner.nextLine();
		System.out.println("What is your message?");
		String message = scanner.nextLine();
		System.out.println("Original Message:");
		System.out.println("\t > " + message);
		System.out.println("Encrypted Message:");
		Cipher enc = new Cipher(password);
		String encryptedText = enc.encrypt(message);
		System.out.println("\t > " + encryptedText);
		System.out.println("Decrypted Message:");
		Cipher dec = new Cipher(password);
		String decryptedText = dec.decrypt(encryptedText);
		System.out.println("\t > " + decryptedText);
		scanner.close();
	}

}
