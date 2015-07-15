package server;

/**
 * Created by madalien on 14/07/15.
 */
import java.io.*;
import java.net.Socket;
import java.util.List;

import dns.registration.Registrar;
import dns.server.ClientDNS;
public class HttpDnsClientHandler implements Runnable {

    final static String CRLF = "\r\n";

    String contentPath;
    Socket socket;
    InputStream input;
    OutputStream output;
    BufferedReader br;
    String filename;
    String domainName;
    String domainToLookup;
    String dnsRequestType;

    public HttpDnsClientHandler(Socket socket, String domainToLookupIn, String typeIn) throws Exception {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        //this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        dnsRequestType = typeIn;
        domainToLookup = domainToLookupIn;
        //ClientDNS client = new ClientDNS("localhost", 6767);

    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void processRequest() throws Exception {


        while (true) {

             // goole public dns or use the local 192.168.1.254  cat /etc/resolv.conf
        //ClientDNS client = new ClientDNS("8.8.8.a8", 53);
            //local server dns
        //ClientDNS client = new ClientDNS("127.0.0.1", 8383);
            // local network dns server
            ClientDNS client = null;
            List<String> ips = null;
            StringBuilder htmlListIps = null;
            String htmlTitle = null;
            String htmlcontent = null;
            System.out.println(" status type ==> "+ dnsRequestType);
            if( dnsRequestType.equals("client")) {
                client = new ClientDNS("8.8.8.8", 53);
                ips = client.getIpfromDomain(false, new String[]{domainToLookup});
                if (ips == null || ips.isEmpty()) {
                    client = new ClientDNS("192.168.1.254", 53);
                    ips = client.getIpfromDomain(false, new String[]{domainToLookup});
                }

                htmlTitle = "DNS lookup from google or Fai ips for "+ domainToLookup;
            }else if(dnsRequestType.equals("localdns")){

                client = new ClientDNS("localhost", 8000);
                ips = client.getIpfromDomain(false, new String[]{domainToLookup});
                htmlTitle = "DNS lookup from local dns server "+ domainToLookup;
            }

            if(dnsRequestType.equals("registrar")){
                Registrar registrar = new Registrar();
                registrar.register(domainToLookup);
                htmlTitle = "Register domain "+ domainToLookup;
                htmlcontent = "<p> The domain "+domainToLookup+" has been successfully registered</p>";
            }else {

                htmlListIps = new StringBuilder("");
                for (String ip : ips) {
                    htmlListIps.append("<p>" + ip + "</p>");
                    System.out.println("ip: " + ip);
                }
                htmlcontent = htmlListIps.toString();
            }



            String serverLine = "Server: Simple Java Http Server";
            String statusLine = null;
            String contentTypeLine = null;
            String entityBody = null;
            String contentLengthLine = "";


            if((htmlListIps != null) || dnsRequestType.equals("registrar")){
                statusLine = "HTTP/1.0 200 OK" + CRLF;
                contentTypeLine = "Content-type: " + "text/html" + CRLF;
                entityBody = "<HTML>"
                        + "<HEAD><TITLE>"+htmlTitle+"</TITLE></HEAD>"
                        + "<BODY>repository"
                        +"<h3>DNS ips for "+ domainToLookup+"</h3>"
                        + htmlcontent
                        + "<br>usage:http://yourHostName:port/"
                        + "fileName.html</BODY></HTML>";

            } else {
                statusLine = "HTTP/1.0 404 Not Found" + CRLF;
                contentTypeLine = "Content-type: " + "text/html" + CRLF;
                entityBody = "<HTML>"
                        + "<HEAD><TITLE>404 Not Found</TITLE></HEAD>"
                        + "<BODY>404 Not Found"
                        + "</BODY></HTML>";
            }

            // Send the status line.
            output.write(statusLine.getBytes());

            // Send the server line.
            output.write(serverLine.getBytes());

            // Send the content type line.
            output.write(contentTypeLine.getBytes());

            // Send the Content-Length
            output.write(contentLengthLine.getBytes());

            // Send a blank line to indicate the end of the header lines.
            output.write(CRLF.getBytes());
            // Send the entity body.

            output.write(entityBody.getBytes());



            try {
                if(output != null)
                output.close();

                //if(br != null)
                //br.close();

                if(socket != null)
                socket.close();

                break;

            } catch (Exception e) {
            }

        }
    }

    private static void sendBytes(FileInputStream fis, OutputStream os)
            throws Exception {

        byte[] buffer = new byte[1024];
        int bytes = 0;

        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")
                || fileName.endsWith(".txt")) {
            return "text/html";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream";
        }
    }
}
