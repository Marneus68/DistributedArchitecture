package dns.opcode;

import dns.parsers.Utils;

import java.nio.ByteBuffer;


public class Question {
	
	
	String qname ;
	int qtype;
	int qclass;
	
	public ByteBuffer toByteBuffer(){
		
		
		ByteBuffer bb =  ByteBuffer.allocate(Utils.getSize(qname)+4);
		
		byte[] typeb = intToByte(qtype);
		byte[] classsb = intToByte(qclass);
		byte zero = 0;
		
		Utils.putName(bb, qname);
		
		bb.put(zero);
		bb.put(typeb[Utils.HIGH_BYTE]);
		bb.put(typeb[Utils.LOW_BYTE]);
		bb.put(classsb[Utils.HIGH_BYTE]);
		bb.put(classsb[Utils.LOW_BYTE]);
				
		bb.rewind();
		return bb;
	}
	
	
	public Question() {}
	
	public Question(ByteBuffer bb){
		qname=Utils.getName(bb, Integer.MAX_VALUE);
		qtype = bytetoInt(bb.get(), bb.get());
		qclass = bytetoInt(bb.get(), bb.get());
		
		
		
	}
	
	private byte[] intToByte(int id2) {
		byte[] bb = new byte[2];
        // cleanation du byte array
		bb[Utils.LOW_BYTE] |= id2 & Utils.LOW_MASK;
		bb[Utils.HIGH_BYTE] |= (id2 & Utils.HIGH_MASK) >> 8;
        //System.out.println("bb{"+Utils.LOW_BYTE+"} ===> "+ bb[Utils.LOW_BYTE]);
        //System.out.println("bb{"+Utils.HIGH_BYTE+"} ===> "+ bb[Utils.HIGH_BYTE] +"id2"+ id2);
		return bb;
	}

	private int bytetoInt(byte high, byte low) {
		int bb = ((high & Utils.LOW_MASK)<<8)+ (low & Utils.LOW_MASK);
		return bb;
	}


	public String getQname() {

		String qname = this.qname;
		if(qname!=null && qname.startsWith("."))
			qname=qname.substring(1);
		if(qname!=null && qname.endsWith("."))
			qname=qname.substring(0, qname.length()-1);
		return qname;
	}


	public void setQname(String qname) {
		if(qname!=null && !qname.startsWith("."))
			qname="."+qname;
		if(qname!=null && !qname.endsWith("."))
			qname+=".";
		this.qname = qname;
	}


	public int getQtype() {
		return qtype;
	}


	public void setQtype(int qtype) {
		this.qtype = qtype;
	}


	public int getQclass() {
		return qclass;
	}


	public void setQclass(int qclass) {
		this.qclass = qclass;
	}


	@Override
	public String toString() {
		return "Question [qname=" + getQname() + ", qtype=" + qtype + ", qclass="
				+ qclass + "]";
	}
	
	
	
	
}
