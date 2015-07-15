package dns.opcode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import dns.headers.Header;
import dns.opcode.Question;

public class Query 
{

	protected Header h = new Header();
	List<Question> questions = new ArrayList<Question>();

	public Query() {
	}
	public Header getHeader() {
		return h;
	}
	public void addQuestion(String qName, int qClass, int qType) {
		Question q = new Question();
		q.setQname(qName);
		q.setQclass(qClass);
		q.setQtype(qType);
		questions.add(q);
	}

	public ByteBuffer toByteBuffer(){
		h.setQdcount(questions.size());
		ByteBuffer bb = ByteBuffer.allocate(512);
		bb.put(h.toByteBuffer());
		System.out.println(h);
		for (Question q : questions){
			bb.put(q.toByteBuffer());
			System.out.println(q);
		}
		bb.flip();
		return bb;
	}
	

	public Query(ByteBuffer bb){
		h= new Header(bb);
		System.out.println(h);
		for (int i=0;i<h.getQdcount();i++) {
			Question q = new Question(bb);
			questions.add(q);
			System.out.println(q);
		}
	}
	
	public List<Question> getQuestions(){
		return questions;
	}
}
