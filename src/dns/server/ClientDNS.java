package dns.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import dns.opcode.Answer;
import dns.opcode.Query;
import dns.rcodes.RR;

public class ClientDNS {
	String serverDnsAdress = "8.8.8.8";
	int serverDnsPort = 53;

	public ClientDNS(String serverDnsAdress, int serverDnsPort) {
		super();
		this.serverDnsAdress = serverDnsAdress;
		this.serverDnsPort = serverDnsPort;
	}

	public List<String> getIpfromDomain(boolean recursionDesired, String... names) {
		List<String> ips = null;
		try {
			InetAddress adr = InetAddress.getByName(serverDnsAdress);

			System.out.println("DNS Request sent");
			Query query = new Query();
			query.getHeader().setRd((recursionDesired == true) ? 1 : 0);
			for (String name : names) {
				query.addQuestion(name, 1, 1);
			}

			ByteBuffer bb = ByteBuffer.allocate(512);
			bb.put(query.toByteBuffer());

			byte[] array = bb.array();
			DatagramPacket packet = new DatagramPacket(array, array.length,
					adr, serverDnsPort);
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
			byte[] data = new byte[512];
			packet.setData(data);
			packet.setLength(512);
			socket.setSoTimeout(10000);
			socket.receive(packet);
			data = packet.getData();
			socket.close();

			ByteBuffer anwser = ByteBuffer.allocate(512);
			anwser.put(data);

			System.out.println("Answer Receive");
			Answer a = new Answer(anwser);

			ips = new ArrayList<String>();
			for (RR rr : a.getResourceRecords()) {
				if (rr.getType() == 1)
					ips.add(rr.getRdata());
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ips;
	}
}
