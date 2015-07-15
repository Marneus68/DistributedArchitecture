package dns.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import dns.opcode.Answer;
import dns.opcode.Question;
import dns.rcodes.RR;
import dns.parsers.ResourceRecordsManager;

public class ServerDNS  implements Runnable{
	int dnsServerPort = 8000;
	int dnsRegistryPort = 9000;

	public ServerDNS(int dnsServerPortIn,int dnsRegistryPortIn) {
		super();
		dnsServerPort = dnsServerPortIn;
		dnsRegistryPort = dnsRegistryPortIn;

        new Thread(new Runnable() {
			@Override
			public void run() {
				start();
			}
		}).start();
	}

	public void start() {

		try {
			byte[] data = new byte[512];
			DatagramSocket socket = new DatagramSocket(dnsServerPort);
			while (true) {
				DatagramPacket packet = new DatagramPacket(data, data.length);

				socket.receive(packet);
				System.out.println("Receive DNS REQUEST");
				Answer a = new Answer(ByteBuffer.wrap(data));
				for (Question q : a.getQuestions()) {
					a.addResourceRecords(ResourceRecordsManager
							.getResourceRecords(q.getQname()));
				}

				System.out.println("Send Answer");
				byte[] resp = a.toByteBuffer().array();
				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				packet = new DatagramPacket(resp, resp.length, address, port);
				socket.send(packet);

			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	
	public void run() {
		try {
			byte[] data = new byte[512];
			DatagramSocket socket = new DatagramSocket(dnsRegistryPort);
			while (true) {
				DatagramPacket packet = new DatagramPacket(data, data.length);
				socket.receive(packet);
				System.out.println("Receive REGISTRATION");
				String name = new String(data);
				name=name.substring(0, name.indexOf('\0'));
				InetAddress address = packet.getAddress();
				RR rr = new RR(name, address.getHostAddress());
				System.out.println(rr);
				List<RR> rrs = ResourceRecordsManager.getResourceRecords(name);
				if(rrs==null){
					rrs=new ArrayList<RR>();
				}
				rrs.add(rr);
				ResourceRecordsManager.addResourceRecords(name, rrs);
                System.out.println("registration successfully done");
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return;
	}
}
