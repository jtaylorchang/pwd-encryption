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
		this.sequence = this.generateSequence(password);
		System.out.println("\t > Sequence: " + sequence);
		this.generateKV(this.sequence);
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
		for (int i = 0; i < sequence.length() / 2; i++) {
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

	public String simpleReplace(final String message, final String[] keys, final String[] values) {
		String output = message;
		for (int i = 0; i < message.length(); i++) {
			for (int j = 0; j < keys.length; j++) {
				if (output.charAt(i) == (keys[j].charAt(0))) {
					output = output.substring(0, i) + values[j] + output.substring(i + 1);
					j = keys.length;
				}
			}
		}
		return output;
	}
	
	public String blur(final String message) {
		return "TODO";
	}
	
	public String sharpen(final String message) {
		return "TODO";
	}

	public String addSplicer(final String message, final String splicer) {
		return message + splicer;
	}

	public String stripSplicer(final String message) {
		return message.substring(0, message.lastIndexOf("S"));
	}
	
	public String stripOff(final String message) {
		return message.substring(0, message.lastIndexOf("Z"));
	}

	public String stripPasses(final String message) {
		return message.substring(0, message.lastIndexOf("PP"));
	}
	
	public int[] getFillerDistribution(final int[] dist, final int max) {
		int[] filler = new int[dist.length];
		for(int i = 0; i < dist.length; i++) {
			if(dist[i] == -1) {
				filler[i] = -1;
			} else {
				filler[i] = max - dist[i];
			}
		}
		return filler;
	}
	
	public int getNonZeroIndex(int[] array) {
		for (int i = 0; i < array.length; i++) {
			if(array[i] > 0) {
				return i;
			}
		}
		return -1;
	}
	
	public int getLastNonZeroIndex(int[] array) {
		for (int i = array.length - 1; i >= 0; i--) {
			if(array[i] > 0) {
				return i;
			}
		}
		return -1;
	}
	
	
	
	/**
	 * Encrypt
	 * 
	 * @param message
	 * @return
	 */
	public String encrypt(final String message) {
		String input = message.toUpperCase();
		String output = this.simpleReplace(input, this.keys, this.values);
		System.out.println("\t > Finished applying simple replace [" + output + "]");
		String blurred = this.blur(output);
		output = blurred;
		return output;
	}

	/**
	 * Undo encrypt
	 * 
	 * @param message
	 * @return
	 */
	public String decrypt(final String message) {
		String input = message.toUpperCase();
		String output = this.simpleReplace(input, this.values, this.keys);
		System.out.println("\t > Finished applying simple replace [" + output + "]");
		String sharpened = this.sharpen(output);
		output = sharpened;
		return output;
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
