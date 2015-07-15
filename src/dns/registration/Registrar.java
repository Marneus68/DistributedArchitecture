package dns.registration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
/**
 * Created by madalien on 14/07/15.
 */
public class Registrar {

    String dnsServerAdress ="localhost";
	int dnsServerPort = 9000;

    public String register(String name){
        InetAddress registrarIp = null;
		try {
			registrarIp = InetAddress.getByName(dnsServerAdress);
			byte[] domainByte = (name+'\0').getBytes();
			DatagramPacket packet = new DatagramPacket(domainByte,
					domainByte.length, registrarIp, dnsServerPort);
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
            System.out.println("Registration packet ====> " + packet);
			System.out.println("Registration Sent for " + name + dnsServerAdress+ registrarIp.getHostAddress());
			socket.close();

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		return registrarIp.getHostAddress();
	}
}
