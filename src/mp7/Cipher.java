package mp7;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
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
		MessageDigest messageDigest;
		String sequence = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			String preSequence = new String(byteArrayToHexString(messageDigest.digest()));
			for(int i = 0; i < preSequence.length(); i++) {
				sequence += (int) preSequence.charAt(i);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		StringBuilder finalSequence = new StringBuilder();
		int[] pieces2 = this.breakBySize(sequence, 2);
		int[] pieces3 = this.breakBySize(sequence, 3);
		int[] pieces4 = this.breakBySize(sequence, 4);
		ArrayList<Integer> combined = this.combine(pieces2, this.breakDown(pieces3), this.breakDown(pieces4));
		ArrayList<Integer> chopped = new ArrayList<Integer>(new LinkedHashSet<Integer>(combined));
		ArrayList<Integer> seq26 = this.convertTo26(chopped);
		for(int i = 0; i < seq26.size(); i++) {
			if(seq26.get(i) < 10) {
				finalSequence.append("0");
			}
			finalSequence.append(seq26.get(i));
		}
		return finalSequence.toString();
	}
	
	public ArrayList<Integer> convertTo26(ArrayList<Integer> pieces) {
		ArrayList<Integer> sorted = new ArrayList<Integer>();
		for(int i = 0; i < pieces.size(); i++) {
			sorted.add(pieces.get(i));
		}
		ArrayList<Integer> sorted26 = new ArrayList<Integer>();
		for(int i = 0; i < sorted.size() && i < 27; i++) {
			sorted26.add(sorted.get(i));
		}
		Collections.sort(sorted26);
		ArrayList<Integer> p26 = new ArrayList<Integer>();
		for(int j = 0; j < sorted26.size(); j++) {
			for(int i = 0; i < pieces.size() && i < 27; i++) {
				if(pieces.get(i) == sorted26.get(j)) {
					p26.add(i);
				}
			}
		}
		return p26;
	}
	
	public int[] breakBySize(String sequence, int size) {
		ArrayList<Integer> pieces = new ArrayList<Integer>();
		for (int i = 0; i < sequence.length() / size; i++) {
			String sPiece = "";
			for (int j = 0; j < size; j++) {
				try {
					sPiece += sequence.charAt(i * size + j);
				} catch(Exception e) {
					
				}
			}
			try {
				int piece = Integer.parseInt(sPiece);
				pieces.add(piece);
			} catch(Exception e) {
				
			}
		}
		int[] array = new int[pieces.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = pieces.get(i);
		}
		return array;
	}
	
	public int[] breakDown(int[] original) {
		int[] small = new int[original.length];
		for (int i = 0; i < original.length; i++) {
			int s = (original[i] / 7);
			while(s > 99) {
				s /= 7;
			}
			small[i] = s;
		}
		return small;
	}
	
	public ArrayList<Integer> combine(int[] ... arrays) {
		int size = 0;
		for (int i = 0; i < arrays.length; i++) {
			size += arrays[i].length;
		}
		int[] c = new int[size];
		ArrayList<Integer> combined = new ArrayList<Integer>();
		int index = 0;
		for (int i = 0; i < arrays.length; i++) {
			for (int j = 0; j < arrays[i].length; j++) {
				c[index] = arrays[i][j];
				index++;
				combined.add(arrays[i][j]);
			}
		}
		return combined;
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
	
	/**
	 * Get the character occurrences for each key value.
	 * 
	 * @param message
	 * @return
	 */
	public int[][] getCharacterDistribution(final String message) {
		int[] dist = new int[this.keys.length];
		int max = 0;
		System.out.print("\t > Neutrality pattern: ");
		for(int i = 0; i < this.keys.length; i++) {
			int occ = message.length() - message.replace(keys[i], "").length();
			if(occ == 0) {
				occ = -1;
			}
			if(occ > max) {
				max = occ;
			}
			dist[i] = occ;
			if(occ > 0) System.out.print(occ);
			//System.out.println("\t > Occurrences of " + this.keys[i] + " = " + dist[i]);
		}
		System.out.println();
		return new int[][] { dist, new int[] { max }};
	}
	
	public String convertNtoL(final int num) {
		String conv = String.valueOf(num);
		for(int i = 0; i < this.keys.length; i++) {
			conv = conv.replaceAll(String.valueOf(i), this.keys[i]);
		}
		return conv;
	}
	
	public int convertLtoN(final String s) {
		String conv = s;
		for(int i = 0; i < this.keys.length; i++) {
			conv = conv.replaceAll(this.keys[i], String.valueOf(i));
		}
		return Integer.parseInt(conv);
	}
	
	public String determineSplicerValue(final String message) {
		return "S" + this.convertNtoL(this.getRequiredFillerMax(message));
	}

	public String addSplicer(final String message, final String splicer) {
		return message + splicer;
	}
	
	public int getSplicer(String message) {
		String s = message.substring(message.lastIndexOf("S") + 1, message.length());
		return this.convertLtoN(s);
	}

	public String stripSplicer(final String message) {
		return message.substring(0, message.lastIndexOf("S"));
	}
	
	public int getOff(String message) {
		String s = message.substring(message.lastIndexOf("Z") + 1, message.length());
		return this.convertLtoN(s);
	}
	
	public String stripOff(final String message) {
		return message.substring(0, message.lastIndexOf("Z"));
	}
	
	public int[] getPasses(String message) {
		String s = message.substring(message.lastIndexOf("PP") + 2, message.length());
		String[] splits = s.split("P");
		int[] passes = new int[splits.length];
		for(int i = splits.length - 1; i >= 0; i--) {
			passes[i] = this.convertLtoN(splits[i]);
		}
		return passes;
	}

	public String stripPasses(final String message) {
		return message.substring(0, message.lastIndexOf("PP"));
	}
	
	/**
	 * Get the number of characters required to neutralize
	 * @param dist
	 * @param max
	 * @return
	 */
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
	
	public int getRequiredFillerMax(final String message) {
		int[][] distCombo = this.getCharacterDistribution(message);
		int[] dist = distCombo[0];
		int max = distCombo[1][0];
		int[] filler = this.getFillerDistribution(dist, max);
		int required = 0;
		for(int i = 0; i < filler.length; i++) {
			if(filler[i] > 0) {
				required += filler[i];
			}
		}
		return required;
	}
	
	/**
	 * Apply the character neutralizer
	 * @param message
	 * @param filler
	 * @param splice
	 * @return
	 */
	public String applyFiller(final String message, final int[] filler, final String splice) {
		String output = message;
		int[] rem = new int[filler.length];
		int required = 0;
		for(int i = 0; i < filler.length; i++) {
			rem[i] = filler[i];
			if(filler[i] > 0) {
				required += filler[i];
			}
		}
		int index = 0;
		ArrayList<Integer> perPasses = new ArrayList<Integer>();
		int passes = 0;
		int off = (output.length() + required) / required;
		boolean forward = false;
		for(int i = 0; i < required; i++) {
			forward = !forward;
			int nzIndex = 0;
			if(forward) {
				nzIndex = this.getNonZeroIndex(rem);
			} else {
				nzIndex = this.getLastNonZeroIndex(rem);
			}
			output = output.substring(0, index) + keys[nzIndex] + output.substring(index);
			index += off + 1;
			passes++;
			if(index >= output.length()) {
				index = 0;
				perPasses.add(passes);
				passes = 0;
			}
			rem[nzIndex] -= 1;
		}
		if(passes > 0) {
			perPasses.add(passes);
		}
		System.out.println("\t > Required " + required + " passes");
		return output + this.createPasses(perPasses) + this.simpleReplace("Z" + this.convertNtoL(off), this.keys, this.values);
	}
	
	public String createPasses(ArrayList<Integer> perPasses) {
		String pass = "PP";
		for(int i = 0; i < perPasses.size(); i++) {
			pass += this.convertNtoL(perPasses.get(i));
			if(i < perPasses.size() - 1) {
				pass += "P";
			}
		}
		return this.simpleReplace(pass, this.keys, this.values);
	}
	
	/**
	 * Remove all the filler.
	 * 
	 * @param message
	 * @param required
	 * @param offset
	 * @return
	 */
	public String removeFiller(final String message, final int required, final int offset, final int[] passes) {
		String output = message;
		int index = 0;
		int currentCycle = 0;
		int cycleNumber = 0;
		for(int i = 0; i < required; i++) {
			if(cycleNumber < passes.length && currentCycle >= passes[passes.length - cycleNumber - 1]) {
				currentCycle = 0;
				cycleNumber++;
				index = 0;
			}
			output = output.substring(0, index) + output.substring(index + 1);
			currentCycle++;
			index += offset;
		}
		return output;
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
		System.out.println("What is your password to encrypt?");
		String password = scanner.nextLine();
		System.out.println("What is your message?");
		String message = scanner.nextLine();
		System.out.println("Original Message:");
		System.err.print(message);
		System.out.println("\n");
		System.out.println("Encrypting:");
		Cipher enc = new Cipher(password);
		String encryptedText = "";
		try {
			encryptedText = enc.encrypt(message);
			System.err.print(encryptedText);
			System.out.println("\n");
		} catch(Exception e) {
			System.out.println("There was an error encrypting");
		}
		boolean done = false;
		while(!done) {
			System.out.println("What is your password to decrypt?");
			password = scanner.nextLine();
			System.out.println("Decrypting:");
			Cipher dec = new Cipher(password);
			String decryptedText = "";
			try {
				decryptedText = dec.decrypt(encryptedText);
				System.err.print(decryptedText);
				System.out.println("");
				System.out.println("\n");
				done = true;
			} catch(Exception e) {
				System.out.println("ERROR! There was an error decrypting");
				System.out.println();
				System.out.println("Try a different password:");
			}
		}
		scanner.close();
	}

}
