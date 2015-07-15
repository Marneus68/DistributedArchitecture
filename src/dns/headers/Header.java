package dns.headers;

import java.nio.ByteBuffer;

public class Header {

	private static final int LOW_MASK = 0xFF;
	private static final int HIGH_MASK = 0xFF00;
	private static final int LOW_BYTE = 0;
	private static final int HIGH_BYTE = 1;

	public final static int QR_MASK = 7;
	public final static int OPCODE_MASK = 3;
	public final static int AA_MASK = 2;
	public final static int TC_MASK = 1;
	public final static int RD_MASK = 0;
	public final static int RA_MASK = 7;
	public final static int Z_MASK = 4;
	public final static int RCODE_MASK = 0;
	
	int id;
	int qr=0;
	int opcode=0;
	int aa=0;
	int tc=0;
	int rd=0;
	int ra=0;
	int z=0;
	int rcode=0;
	int qdcount=0;
	int nacount=0;
	int nscount=0;
	int arcount=0;
	
	
	public Header() {
		id=(int) (Math.random()*65536);
	}
	
	public Header(ByteBuffer bb) {
		bb.rewind();
		byte[] idb= new byte[2];
		idb[HIGH_BYTE]=bb.get();
		idb[LOW_BYTE]=bb.get();
		byte first = bb.get();		
		byte second = bb.get();
		
		byte[] qdcountb = new byte[2];
		qdcountb[HIGH_BYTE]=bb.get();
		qdcountb[LOW_BYTE]=bb.get();
		
		byte[] nacountb = new byte[2];
		nacountb[HIGH_BYTE]=bb.get();
		nacountb[LOW_BYTE]=bb.get();
		
		byte[] nscountb = new byte[2];
		nscountb[HIGH_BYTE]=bb.get();
		nscountb[LOW_BYTE]=bb.get();
		
		byte[] arcountb = new byte[2];
		arcountb[HIGH_BYTE]=bb.get();
		arcountb[LOW_BYTE]=bb.get();

		this.id=fromBytetoInt(idb);
		this.qdcount=fromBytetoInt(qdcountb);
		this.nacount=fromBytetoInt(nacountb);
		this.nscount=fromBytetoInt(nscountb);
		this.arcount=fromBytetoInt(arcountb);

		this.qr = ((first & LOW_MASK & (0|(1<<QR_MASK)))>>QR_MASK);
		this.opcode = ((first & LOW_MASK & (0|(7<<OPCODE_MASK)))>>OPCODE_MASK);
		this.aa = ((first & LOW_MASK & (0|(1<<AA_MASK)))>>AA_MASK);
		this.tc = ((first & LOW_MASK & (0|(1<<TC_MASK)))>>TC_MASK);
		this.rd = ((first & LOW_MASK & (0|(1<<RD_MASK)))>>RD_MASK);
		this.ra = ((second & LOW_MASK & (0|(1<<RA_MASK)))>>RA_MASK);
		this.z=((second & LOW_MASK & (0|(3<<Z_MASK)))>>Z_MASK);
		this.rcode=((second & LOW_MASK & (0|(7<<RCODE_MASK)))>>RCODE_MASK);
		
	}


	public ByteBuffer toByteBuffer() {
		ByteBuffer bb = ByteBuffer.allocate(12);

		byte[] idb = fromIntToByte(id);
		byte a = firstPart();
		byte b = secondPart();
		byte[] qdcountb = fromIntToByte(qdcount);
		byte[] nacountb = fromIntToByte(nacount);
		byte[] nscountb = fromIntToByte(nscount);
		byte[] arcountb = fromIntToByte(arcount);
		bb.put(idb[HIGH_BYTE]);
		bb.put(idb[LOW_BYTE]);
		bb.put(a);
		bb.put(b);
		bb.put(qdcountb[HIGH_BYTE]);
		bb.put(qdcountb[LOW_BYTE]);
		bb.put(nacountb[HIGH_BYTE]);
		bb.put(nacountb[LOW_BYTE]);
		bb.put(nscountb[HIGH_BYTE]);
		bb.put(nscountb[LOW_BYTE]);
		bb.put(arcountb[HIGH_BYTE]);
		bb.put(arcountb[LOW_BYTE]);
		
		bb.rewind();
		
		return bb;
	}

	private byte firstPart() {
		byte b = 0;
		b |= (qr<<QR_MASK);
		b |= (opcode<<OPCODE_MASK);
		b |= (aa<<AA_MASK);
		b |= (tc<<TC_MASK);
		b |= (rd<<RD_MASK);
		return b;
	}
	private byte secondPart() {
		byte b = 0;
		b |= (ra<<RA_MASK);
		b |= (0<<Z_MASK);
		b |= (rcode<<RCODE_MASK);
		return b;
	}

	private byte[] fromIntToByte(int id2) {
		byte[] bb = new byte[2];
		bb[LOW_BYTE] |= id2 & LOW_MASK;
		bb[HIGH_BYTE] |= (id2 & HIGH_MASK) >> 8;
		return bb;
	}

	private int fromBytetoInt(byte[] idb) {
		int bb = ((idb[HIGH_BYTE]&LOW_MASK)<<8)+ (idb[LOW_BYTE]&LOW_MASK);
		return bb;
	}
	
	
	
	public int getQr() {
		return qr;
	}

	public void setQr(int qr) {
		this.qr = qr;
	}

	public int getOpcode() {
		return opcode;
	}

	public void setOpcode(int opcode) {
		this.opcode = opcode;
	}

	public int getAa() {
		return aa;
	}

	public void setAa(int aa) {
		this.aa = aa;
	}

	public int getTc() {
		return tc;
	}

	public void setTc(int tc) {
		this.tc = tc;
	}

	public int getRd() {
		return rd;
	}

	public void setRd(int rd) {
		this.rd = rd;
	}

	public int getRa() {
		return ra;
	}

	public void setRa(int ra) {
		this.ra = ra;
	}

	public int getRcode() {
		return rcode;
	}

	public void setRcode(int rcode) {
		this.rcode = rcode;
	}

	public int getQdcount() {
		return qdcount;
	}

	public void setQdcount(int qdcount) {
		this.qdcount = qdcount;
	}

	public int getNacount() {
		return nacount;
	}

	public void setNacount(int nacount) {
		this.nacount = nacount;
	}

	public int getNscount() {
		return nscount;
	}

	public void setNscount(int nscount) {
		this.nscount = nscount;
	}

	public int getArcount() {
		return arcount;
	}

	public void setArcount(int arcount) {
		this.arcount = arcount;
	}

	public int getId() {
		return id;
	}

	public int getZ() {
		return z;
	}

	@Override
	public String toString() {
		return "Header [id=" + id + ", qr=" + qr + ", opcode=" + opcode
				+ ", aa=" + aa + ", tc=" + tc + ", rd=" + rd + ", ra=" + ra
				+ ", z=" + z + ", rcode=" + rcode + ", qdcount=" + qdcount
				+ ", nacount=" + nacount + ", nscount=" + nscount
				+ ", arcount=" + arcount + "]";
	}

}
