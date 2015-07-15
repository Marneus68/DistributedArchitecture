package dns.rcodes;

import dns.parsers.Utils;

import java.nio.ByteBuffer;

public class RR {

	String name = "";
	int type=1;
	int classs=1;
	int ttl=0;
	int rdLength=0;
	String rdata = "";

	public ByteBuffer toByteBuffer() {
		ByteBuffer bb = ByteBuffer.allocate(Utils.getSize(name)+Utils.getSize(rdata) + 10);

		byte[] typeb = fromIntToByte(type);
		byte[] classsb = fromIntToByte(classs);
		byte[] ttlb1 = fromIntToByte(ttl & 0xFFFF);
		byte[] ttlb2 = fromIntToByte((ttl & 0xFFFF0000) >> 16);
		byte[] rdlengthb = fromIntToByte(rdLength);

		Utils.putName(bb, name);
		bb.put((byte) 0);
		bb.put(typeb[Utils.HIGH_BYTE]);
		bb.put(typeb[Utils.LOW_BYTE]);
		bb.put(classsb[Utils.HIGH_BYTE]);
		bb.put(classsb[Utils.LOW_BYTE]);
		bb.put(ttlb1[Utils.HIGH_BYTE]);
		bb.put(ttlb1[Utils.LOW_BYTE]);
		bb.put(ttlb2[Utils.HIGH_BYTE]);
		bb.put(ttlb2[Utils.LOW_BYTE]);
		bb.put(rdlengthb[Utils.HIGH_BYTE]);
		bb.put(rdlengthb[Utils.LOW_BYTE]);
		if(type==1){
			Utils.putIP(bb, rdata);
		}else{
			Utils.putName(bb, rdata);
		}
		bb.flip();
		return bb;
	}

	public RR() {
	}
	public RR(String name, String rdata) {
		setName(name);
		this.rdata=rdata;
		rdLength=4;
	}

	public RR(ByteBuffer bb) {
		name = Utils.getName(bb, Integer.MAX_VALUE);
		type = fromBytetoInt(bb.get(), bb.get());

		classs = fromBytetoInt(bb.get(), bb.get());

		int ttl1 = fromBytetoInt(bb.get(), bb.get());

		int tttl2 = fromBytetoInt(bb.get(), bb.get());

		ttl = (ttl1 & 0xFFFF) << 16 + (tttl2 & 0xFFFF);

		rdLength = fromBytetoInt(bb.get(), bb.get());
		if (type == 1)
			rdata = Utils.getIP(bb, rdLength);
		else
			rdata = Utils.getName(bb, rdLength);

	}

	private byte[] fromIntToByte(int id2) {
		byte[] bb = new byte[2];
		bb[Utils.LOW_BYTE] |= id2 & Utils.LOW_MASK;
		bb[Utils.HIGH_BYTE] |= (id2 & Utils.HIGH_MASK) >> 8;
		return bb;
	}

	private int fromBytetoInt(byte high, byte low) {
		int bb = ((high & Utils.LOW_MASK) << 8) + (low & Utils.LOW_MASK);
		return bb;
	}

	@Override
	public String toString() {
		return "RR [name=" + name + ", type=" + type + ", classs=" + classs
				+ ", ttl=" + ttl + ", rdLength=" + rdLength + ", rdata="
				+ rdata + "]";
	}

	private void setName(String name) {
		if(name!=null && !name.startsWith("."))
			name="."+name;
		if(name!=null && !name.endsWith("."))
			name+=".";
		this.name = name;
	}
	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public int getClasss() {
		return classs;
	}

	public int getTtl() {
		return ttl;
	}

	public int getRdLength() {
		return rdLength;
	}

	public String getRdata() {
		return rdata;
	}
	
	
	

}
