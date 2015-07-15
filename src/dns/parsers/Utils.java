package dns.parsers;

import java.nio.ByteBuffer;

public class Utils {

	public static final int LOW_MASK = 0xFF;
	public static final int HIGH_MASK = 0xFF00;
	public static final int LOW_BYTE = 0;
	public static final int HIGH_BYTE = 1;

	public static String getName(ByteBuffer bb, int lentgh) {
		int number = 0;
		int size = 0;
		String name = "";
		int truePosition = 0;
		while (size < lentgh) {
			byte b = bb.get();
			size++;
			if ((b & LOW_MASK) == 192) {
				int pointer = LOW_MASK & ((b ^ 192) << 8) | bb.get() & LOW_MASK;
				if (truePosition == 0)
					truePosition = bb.position();
				bb.position(pointer);
				b = bb.get();
                //System.out.println("p ****>  " +pointer);
			}
			if (b == 0) {
                //System.out.println("b ****> .... " +b);
				name += ".";
				break;
			} else if (number == 0) {
                //System.out.println("num ****> .... " +number);
				number = b & LOW_MASK;
				name += ".";
			} else {
				name += (char) b;
				number--;
			}
		}
		if (truePosition != 0) {
			bb.position(truePosition);
		}
		return name;
	}

	public static String getIP(ByteBuffer bb, int lentgh) {
		int size = 0;
		String name = "";
		while (size < lentgh) {
			byte b = bb.get();
			size++;

			name += b & LOW_MASK;
			if (size % 4 != 0) {
				name += ".";
			}
		}
		return name;
	}
	
	public static void putIP(ByteBuffer bb,String ip) {
		String[] dot = ip.split("\\.");
		for (int i = 0; i < dot.length; i++) {
			bb.put(Integer.valueOf(dot[i]).byteValue());
		}
	}

	public static int getSize(String name) {
		String[] dot = name.split("\\.");

		int size = 0;
		for (String s : dot) {
			size += s.getBytes().length;
		}
		return size + dot.length;
	}

	public static void putName(ByteBuffer bb, String name) {
		String[] dot = name.split("\\.");
		for (int i = 1; i < dot.length; i++) {
			byte b = 0;
			b |= dot[i].getBytes().length & LOW_MASK;
			bb.put(b);
			bb.put(dot[i].getBytes());
		}
	}

}
