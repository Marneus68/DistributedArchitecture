package dns.opcode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import dns.rcodes.RR;

public class Answer extends Query {

	List<RR> rrs = new ArrayList<RR>();

	public ByteBuffer toByteBuffer() {
		ByteBuffer bb = ByteBuffer.allocate(512);
		h.setQr(1);
		if (rrs.size()==0) {
			h.setRcode(2);
			bb.put(super.toByteBuffer());
			bb.flip();
			return bb;
		}
		h.setNacount(rrs.size());
		bb.put(super.toByteBuffer());
		for (RR rr : rrs)
			bb.put(rr.toByteBuffer());
		bb.flip();
		return bb;
	}

	public Answer(ByteBuffer bb) {
		super(bb);
		for (int i = 0; i < h.getNacount(); i++) {
			RR rr = new RR(bb);
			rrs.add(rr);
			System.out.println(rr);
		}
	}

	public List<RR> getResourceRecords() {
		return rrs;
	}

	public void addResourceRecords(List<RR> rrs) {
		if (rrs != null)
			this.rrs.addAll(rrs);
	}
}
