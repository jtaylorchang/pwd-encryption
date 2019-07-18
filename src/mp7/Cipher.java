package mp7;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Scanner;

/**
 * 
 * @author Jeff Taylor-Chang and Chinmaya Sharma.
 * 
 *         ==================== GOAL ==================== The goal was to create
 *         an encryption which was unique to the password and thus could only be
 *         broken by someone who knew the password, rather than knew the
 *         encryption method.
 * 
 *         ==================== TIME ==================== Since every encrypted
 *         message can be different based on the message or the password, the
 *         brute force method of testing various inputs is nearly impossible
 *         since the attacker would have to not only test every possible message
 *         but also test every possible password. The number of possibilities
 *         that would have to be tested would scale at a rate of c27^(m+p) with
 *         m being the number of letters in the message and p being the number
 *         of letters in the password and c being the time it takes to encrypt
 *         one message. So with a ten letter password and assuming c = 1 second
 *         |____ |_______ |_______________ | | m | p | time | |____ |_______
 *         |_______________ | | 1 | 10 | 5.6 * 10^15 | | 2 | 10 | 1.5 * 10^17 |
 *         | 3 | 10 | 4.1 * 10^18 | | 4 | 10 | 1.1 * 10^20 | | 5 | 10 | 3.0 *
 *         10^21 | | 6 | 10 | 8.0 * 10^22 | | 7 | 10 | 2.2 * 10^24 | | 8 | 10 |
 *         5.8 * 10^25 | | 9 | 10 | 1.6 * 10^27 | | 10 | 10 | 4.2 * 10^28 | -->
 *         100 billion * the age of the universe |____ |_______ |_______________
 *         |
 * 
 *         and that is only with 10 characters for a password, let alone
 *         something like 20...
 * 
 *         ==================== METHOD ==================== Step 1: Generate a
 *         sequence number that is unique to the password. ie: "password" -->
 *         071724150123020426111409211200181310200603051625082219
 * 
 *         Step 2: Generate key-value pairs using a proprietary algorithm which
 *         creates the connections based on the sequence number.
 * 
 *         Step 3: Apply a 1:1 swap of the keys and values which is a
 *         substitution cipher.
 * 
 *         Step 4: Analyze the distribution of characters in the encrypted
 *         message.
 * 
 *         Step 5: Automatically determine the appropriate way to neutralize the
 *         message ie: add filler characters that disrupt the message and
 *         prevent character analysis aka "the scrabble method" (coined by yours
 *         truly) aka Statistical Frequency Analysis which is commonly used to
 *         break substitution ciphers like the one we applied in step 3
 * 
 *         Step 6: Calculate the unique neutralizing numbers and insert into the
 *         message using a similarly encrypted tag.
 * 
 *         Step 7: (Optional) Apply the encryption on itself to further
 *         randomize as many times as you'd like since the encryption can
 *         self-apply recursively. In order to keep the scaling low, this only
 *         applies it once but overriding the public encrypt and decrypt methods
 *         allows as many times as necessary.
 * 
 *         Decryption is accomplished by undoing each step in the reverse order.
 * 
 *         ==================== CREDIT ==================== ~ Written and
 *         devised by Jeff Taylor-Chang (https://jefftc.com) ~ implemented by
 *         Jeff Taylor-Chang and Chinmaya Sharma
 * 
 *         © Copyright 2017
 * 
 *         Created on December 14th, 2017
 * 
 */
public class Cipher {

	/** Use a sequence unique to each password to generate the key value pairs. **/
	private String sequence = "";
	/** The array of all the keys. **/
	private String[] keys = { " ",
			// "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
			// "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
			"V", "W", "X", "Y", "Z",
			// "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
	};
	/** The array of all the values. Must be populated. **/
	private String[] values = new String[keys.length];

	/**
	 * Generate a unique sequence using the password and then generate the key value
	 * pairs.
	 * 
	 * @param password the user's password.
	 */
	public Cipher(final String password) {
		this.sequence = this.generateSequence(password);
		System.out.println("\t > Sequence: " + sequence);
		this.generateKV(this.sequence);
	}

	/**
	 * Generate a sequence unique to the password. First encrypt the password using
	 * SHA-256 and then convert to a string. Take that number and break into small
	 * chunks that can be used as array locations. Convert them to the bounds
	 * necessary for the number of keys. Put them in order and then compare to find
	 * out the random locations of each one which is unique to the password, thus
	 * giving unique connections between keys and values.
	 * 
	 * @param password to generate with.
	 * @return the sequence generated.
	 */
	private String generateSequence(final String password) {
		MessageDigest messageDigest;
		String sequence = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			String preSequence = new String(byteArrayToHexString(messageDigest.digest()));
			for (int i = 0; i < preSequence.length(); i++) {
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
		for (int i = 0; i < seq26.size(); i++) {
			if (seq26.get(i) < 10) {
				finalSequence.append("0");
			}
			finalSequence.append(seq26.get(i));
		}
		return finalSequence.toString();
	}

	/**
	 * Take many arrays of numbers of various sizes and compile them to create one
	 * array with a set of unique numbers.
	 * 
	 * @param pieces the arrays of chunks.
	 * @return the array in the correct size.
	 */
	private ArrayList<Integer> convertTo26(ArrayList<Integer> pieces) {
		ArrayList<Integer> sorted = new ArrayList<Integer>();
		for (int i = 0; i < pieces.size(); i++) {
			sorted.add(pieces.get(i));
		}
		ArrayList<Integer> sorted26 = new ArrayList<Integer>();
		for (int i = 0; i < sorted.size() && i < this.keys.length; i++) {
			sorted26.add(sorted.get(i));
		}
		Collections.sort(sorted26);
		ArrayList<Integer> p26 = new ArrayList<Integer>();
		for (int j = 0; j < sorted26.size(); j++) {
			for (int i = 0; i < pieces.size() && i < this.keys.length; i++) {
				if (pieces.get(i) == sorted26.get(j)) {
					p26.add(i);
				}
			}
		}
		return p26;
	}

	/**
	 * Break a sequence into small chunks.
	 * 
	 * @param sequence the sequence to select from.
	 * @param size     the size of the chunks.
	 * @return an array of chunks.
	 */
	private int[] breakBySize(String sequence, int size) {
		ArrayList<Integer> pieces = new ArrayList<Integer>();
		for (int i = 0; i < sequence.length() / size; i++) {
			String sPiece = "";
			for (int j = 0; j < size; j++) {
				try {
					sPiece += sequence.charAt(i * size + j);
				} catch (Exception e) {

				}
			}
			try {
				int piece = Integer.parseInt(sPiece);
				pieces.add(piece);
			} catch (Exception e) {

			}
		}
		int[] array = new int[pieces.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = pieces.get(i);
		}
		return array;
	}

	/**
	 * Shrink numbers down to an appropriate scope.
	 * 
	 * @param original the numbers in their original sizes.
	 * @return the array of smaller numbers.
	 */
	private int[] breakDown(int[] original) {
		int[] small = new int[original.length];
		for (int i = 0; i < original.length; i++) {
			int s = (original[i] / 7);
			while (s > 99) {
				s /= 7;
			}
			small[i] = s;
		}
		return small;
	}

	/**
	 * Combines all the arrays into one single array.
	 * 
	 * @param arrays the arrays to combine.
	 * @return the combined array.
	 */
	private ArrayList<Integer> combine(int[]... arrays) {
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
	 * Generate the key value pairs.
	 * 
	 * @param sequence the unique sequence.
	 */
	private void generateKV(final String sequence) {
		this.values = new String[this.keys.length];
		for (int i = 0; i < sequence.length() / 2; i++) {
			String sV = sequence.substring(i * 2, i * 2 + 2);
			int v = Integer.parseInt(sV);
			this.values[i] = this.keys[v];
		}
	}

	/**
	 * Convert bytes to readable string.
	 * 
	 * @param b the byte array.
	 * @return the string version.
	 */
	private static String byteArrayToHexString(byte[] bytes) {
		String result = "";
		for (int i = 0; i < bytes.length; i++) {
			result += Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	/**
	 * Applies a simple 1-1 swap of the key value pairs. If given the keys and
	 * values one way and then the other, it will encrypt then undo the encryption.
	 * 
	 * @param message the message to swap.
	 * @param keys    the key array.
	 * @param values  the value array.
	 * @return the swapped message.
	 */
	private String simpleReplace(final String message, final String[] keys, final String[] values) {
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

	/**
	 * Insert characters to neutralize the message character distribution. This
	 * allows the encrypted message to beat attempts to analyze the number of each
	 * character.
	 * 
	 * @param message the message to encrypt.
	 * @return the blurred message with near neutral distribution.
	 */
	private String blur(final String message) {
		System.out.println("\t > Applying character blur");
		String output = message;
		String splicer = this.simpleReplace(this.determineSplicerValue(output), this.keys, this.values);
		System.out.println("\t > Finished calculating splicer " + splicer);
		int[][] distCombo = this.getCharacterDistribution(output);
		int[] dist = distCombo[0];
		int max = distCombo[1][0];
		System.out.println("\t > Finished analysing character occurrences");
		int[] filler = this.getFillerDistribution(dist, max);
		System.out.println("\t > Finished determining necessary filler");
		output = this.applyFiller(output, filler, splicer);
		output = addSplicer(output, splicer);
		System.out.println("\t > Finished applying splicer");
		// may need to filter once more?
		System.out.println("\t > Finished neutralizing character distribution");
		return output;
	}

	/**
	 * Remove the filler characters, thus restoring the message to its original
	 * state before the 1-1 swap.
	 * 
	 * @param message the blurred message.
	 * @return the swapped message.
	 */
	private String sharpen(final String message) {
		System.out.println("\t > Applying character sharpen");
		String output = message;
		int required = this.getSplicer(output);
		System.out.println("\t > Detecting character splicer");
		output = this.stripSplicer(output);
		System.out.println("\t > Extracting character splicer = " + required);
		int off = this.getOff(output);
		System.out.println("\t > Detecting offset value = " + off);
		output = this.stripOff(output);
		System.out.println("\t > Extracting offset value");
		int[] passes = this.getPasses(output);
		String pass = "";
		for (int i = 0; i < passes.length; i++) {
			pass += passes[i];
		}
		System.out.println("\t > Detecting pass counter");
		output = this.stripPasses(output);
		System.out.println("\t > Extracting pass counter = " + pass);
		output = this.removeFiller(output, required, off, passes);
		System.out.println("\t > Removed neutralizing characters");
		return output;
	}

	/**
	 * Get the character occurrences for each key value.
	 * 
	 * @param message the message to count.
	 * @return an array of how many occurrences of each character.
	 */
	private int[][] getCharacterDistribution(final String message) {
		int[] dist = new int[this.keys.length];
		int max = 0;
		System.out.print("\t > Neutrality pattern: ");
		for (int i = 0; i < this.keys.length; i++) {
			int occ = message.length() - message.replace(keys[i], "").length();
			if (occ == 0) {
				occ = -1;
			}
			if (occ > max) {
				max = occ;
			}
			dist[i] = occ;
			if (occ > 0)
				System.out.print(occ);
			// System.out.println("\t > Occurrences of " + this.keys[i] + " = " + dist[i]);
		}
		System.out.println();
		return new int[][] { dist, new int[] { max } };
	}

	/**
	 * Convert numbers to letters, ie 1 --> A, 2 --> B etc. This allows numbers to
	 * be hidden inside the message for tagging purposes.
	 * 
	 * @param num the number to convert.
	 * @return the letter value.
	 */
	private String convertNtoL(final int num) {
		String conv = String.valueOf(num);
		for (int i = 0; i < this.keys.length; i++) {
			conv = conv.replaceAll(String.valueOf(i), this.keys[i]);
		}
		return conv;
	}

	/**
	 * Undoes the conversion.
	 * 
	 * @param s the string to convert.
	 * @return the number value.
	 */
	private int convertLtoN(final String s) {
		String conv = s;
		for (int i = 0; i < this.keys.length; i++) {
			conv = conv.replaceAll(this.keys[i], String.valueOf(i));
		}
		return Integer.parseInt(conv);
	}

	/**
	 * Calculates what the splicer tag should be.
	 * 
	 * @param message the encrypted message.
	 * @return the splicer value.
	 */
	private String determineSplicerValue(final String message) {
		return "S" + this.convertNtoL(this.getRequiredFillerMax(message));
	}

	/**
	 * Adds a splicer tag to a message.
	 * 
	 * @param message the message without a splicer tag.
	 * @param splicer the splicer value.
	 * @return the message with a splicer tag.
	 */
	private String addSplicer(final String message, final String splicer) {
		return message + splicer;
	}

	/**
	 * Reads a splicer tag from a message.
	 * 
	 * @param message the encrypted message.
	 * @return the splicer value.
	 */
	private int getSplicer(String message) {
		String s = message.substring(message.lastIndexOf("S") + 1, message.length());
		return this.convertLtoN(s);
	}

	/**
	 * Removes the splicer tag from a message.
	 * 
	 * @param message the message with a splicer tag.
	 * @return the message without a splicer tag.
	 */
	private String stripSplicer(final String message) {
		return message.substring(0, message.lastIndexOf("S"));
	}

	/**
	 * Reads the offset tag from a message.
	 * 
	 * @param message the message with the offset tag.
	 * @return the offset value.
	 */
	private int getOff(String message) {
		String s = message.substring(message.lastIndexOf("Z") + 1, message.length());
		return this.convertLtoN(s);
	}

	/**
	 * Removes the offset tag from a message.
	 * 
	 * @param message the message with the offset tag.
	 * @return the message without the offset tag.
	 */
	private String stripOff(final String message) {
		return message.substring(0, message.lastIndexOf("Z"));
	}

	/**
	 * Reads all of the pass tags from a message.
	 * 
	 * @param message the message with the pass tags.
	 * @return the array of pass values.
	 */
	private int[] getPasses(String message) {
		String s = message.substring(message.lastIndexOf("PP") + 2, message.length());
		String[] splits = s.split("P");
		int[] passes = new int[splits.length];
		for (int i = splits.length - 1; i >= 0; i--) {
			passes[i] = this.convertLtoN(splits[i]);
		}
		return passes;
	}

	/**
	 * Removes all of the pass tags from a message.
	 * 
	 * @param message the message with the pass tags.
	 * @return the message without the pass tags.
	 */
	private String stripPasses(final String message) {
		return message.substring(0, message.lastIndexOf("PP"));
	}

	/**
	 * Get the number of characters required to neutralize.
	 * 
	 * @param dist the distribution array.
	 * @param max  the maximum necessary for any one character.
	 * @return the array of polarized characters.
	 */
	private int[] getFillerDistribution(final int[] dist, final int max) {
		int[] filler = new int[dist.length];
		for (int i = 0; i < dist.length; i++) {
			if (dist[i] == -1) {
				filler[i] = -1;
			} else {
				filler[i] = max - dist[i];
			}
		}
		return filler;
	}

	/**
	 * Get the maximum amount of neutralizing characters needed for any one
	 * character.
	 * 
	 * @param message the message to encrypt.
	 * @return the maximum polarized character.
	 */
	private int getRequiredFillerMax(final String message) {
		int[][] distCombo = this.getCharacterDistribution(message);
		int[] dist = distCombo[0];
		int max = distCombo[1][0];
		int[] filler = this.getFillerDistribution(dist, max);
		int required = 0;
		for (int i = 0; i < filler.length; i++) {
			if (filler[i] > 0) {
				required += filler[i];
			}
		}
		return required;
	}

	/**
	 * Apply the character neutralizer.
	 * 
	 * @param message the message to apply the filter to.
	 * @param filler  the filler value array.
	 * @param splice  the number to fill with.
	 * @return the neutralized message.
	 */
	private String applyFiller(final String message, final int[] filler, final String splice) {
		String output = message;
		int[] rem = new int[filler.length];
		int required = 0;
		for (int i = 0; i < filler.length; i++) {
			rem[i] = filler[i];
			if (filler[i] > 0) {
				required += filler[i];
			}
		}
		int index = 0;
		ArrayList<Integer> perPasses = new ArrayList<Integer>();
		int passes = 0;
		int off = (output.length() + required) / required;
		boolean forward = false;
		for (int i = 0; i < required; i++) {
			forward = !forward;
			int nzIndex = 0;
			if (forward) {
				nzIndex = this.getNonZeroIndex(rem);
			} else {
				nzIndex = this.getLastNonZeroIndex(rem);
			}
			output = output.substring(0, index) + keys[nzIndex] + output.substring(index);
			index += off + 1;
			passes++;
			if (index >= output.length()) {
				index = 0;
				perPasses.add(passes);
				passes = 0;
			}
			rem[nzIndex] -= 1;
		}
		if (passes > 0) {
			perPasses.add(passes);
		}
		System.out.println("\t > Required " + required + " passes");
		return output + this.createPasses(perPasses)
				+ this.simpleReplace("Z" + this.convertNtoL(off), this.keys, this.values);
	}

	/**
	 * Create the passes tag.
	 * 
	 * @param perPasses the number of characters per pass.
	 * @return the message with the pass tag.
	 */
	private String createPasses(ArrayList<Integer> perPasses) {
		String pass = "PP";
		for (int i = 0; i < perPasses.size(); i++) {
			pass += this.convertNtoL(perPasses.get(i));
			if (i < perPasses.size() - 1) {
				pass += "P";
			}
		}
		return this.simpleReplace(pass, this.keys, this.values);
	}

	/**
	 * Remove all the filler characters in a string, negating the neutralizing
	 * agents.
	 * 
	 * @param message  the message with filler.
	 * @param required the number of neutralizing agents.
	 * @param offset   the offset used in distribution.
	 * @return the cleared message.
	 */
	private String removeFiller(final String message, final int required, final int offset, final int[] passes) {
		String output = message;
		int index = 0;
		int currentCycle = 0;
		int cycleNumber = 0;
		for (int i = 0; i < required; i++) {
			if (cycleNumber < passes.length && currentCycle >= passes[passes.length - cycleNumber - 1]) {
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

	/**
	 * Get the first nonzero index in an array.
	 * 
	 * @param array the array.
	 * @return the first nonzero index.
	 */
	private int getNonZeroIndex(int[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] > 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the last nonzero index in an array.
	 * 
	 * @param array the array.
	 * @return the last nonzero index.
	 */
	private int getLastNonZeroIndex(int[] array) {
		for (int i = array.length - 1; i >= 0; i--) {
			if (array[i] > 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * A single phase of the encryption. Separated from the public method to allow
	 * for recursively encrypting over and over.
	 * 
	 * @param message the original message.
	 * @return the encrypted message.
	 */
	private String encryptPhase(final String message) {
		String input = message.toUpperCase();
		String output = this.simpleReplace(input, this.keys, this.values);
		System.out.println("\t > Finished applying simple replace [" + output + "]");
		String blurred = this.blur(output);
		output = blurred;
		return output;
	}

	/**
	 * Encrypt the message using the proprietary algorithm.
	 * 
	 * @param message the original message.
	 * @return the encrypted message.
	 */
	public String encrypt(final String message) {
		return this.encryptPhase(message);
	}

	/**
	 * A single phase of the decryption. Separated from the public method to allow
	 * for recursively decrypting over and over.
	 * 
	 * @param message the encrypted message.
	 * @return the original message.
	 */
	private String decryptPhase(final String message) {
		String input = message.toUpperCase();
		String output = this.simpleReplace(input, this.values, this.keys);
		System.out.println("\t > Finished applying simple replace [" + output + "]");
		String sharpened = this.sharpen(output);
		output = sharpened;
		return output;
	}

	/**
	 * Undo the encryption using the proprietary algorithm.
	 * 
	 * @param message the encrypted message.
	 * @return the original message.
	 */
	public String decrypt(final String message) {
		return this.decryptPhase(message);
	}

	/**
	 * The main function to let users encrypt and undo encrypt.
	 * 
	 * @param args unused
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("What is your password to encrypt?");
		String password = scanner.nextLine();
		System.out.println("What is your message?");
		String message = scanner.nextLine();
		System.out.println("Original Message:");
		System.out.print(message);
		System.out.println("\n");
		System.out.println("Encrypting:");
		Cipher enc = new Cipher(password);
		String encryptedText = "";
		try {
			encryptedText = enc.encrypt(message);
			System.out.print(encryptedText);
			System.out.println("\n");
		} catch (Exception e) {
			System.err.println("There was an error encrypting");
		}
		boolean done = false;
		while (!done) {
			System.out.println("What is your password to decrypt?");
			password = scanner.nextLine();
			System.out.println("Decrypting:");
			Cipher dec = new Cipher(password);
			String decryptedText = "";
			try {
				decryptedText = dec.decrypt(encryptedText);
				System.out.print(decryptedText);
				System.out.println("");
				System.out.println("\n");
				done = true;
			} catch (Exception e) {
				System.err.println("ERROR! There was an error decrypting");
				System.out.println();
				System.err.println("Try a different password:");
			}
		}
		scanner.close();
	}

}
