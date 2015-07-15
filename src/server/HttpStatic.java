package server;

import dns.server.ServerDNS;
import pools.esgi.com.Pool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * File created by duane
 * 2015-05-18 | 10:30 AM
 */

public class HttpStatic {
    public static int PORT = 8181;

    final static String CRLF = "\r\n";
    public static boolean withSession = false;
    public static boolean  dnsClient = false;
    public static boolean  dnsClientServer = false;
    public static boolean  dnsRegistrar = false;
    public static String  dnsRequestType = "";
    public static String  domainToLookUp = "";


    public static void main(String[] args) throws Exception {
        int port = PORT;
        String hostName = null;

        if (2 == args.length) {
            port = Integer.valueOf(args[0]);
            withSession = args[1].equals("--with-session")? true: false;
            System.out.println("******* with session****** "+withSession+ " value: "+args[1]);
        }
        else if(1 == args.length){
            port = Integer.valueOf(args[0]);
        }


        Pool threadPool  = new Pool(7);
        Map<Integer, Domain> domains = new HashMap<Integer, Domain>();
        String filename = "";

        Properties props = new Properties();
        InputStream input = new FileInputStream("config.ini");
        props.load(input);
        for (String key : props.stringPropertyNames()) {
            String[] keys = key.split("\\.");

            if (!domains.containsKey(Integer.parseInt(keys[1]))) {
                domains.put(Integer.parseInt(keys[1]), new Domain());
            }

            if (keys[2].equals("name")) {
                domains.get(Integer.parseInt(keys[1])).name = props.getProperty(key);
            } else if (keys[2].equals("documentRoot")) {
                domains.get(Integer.parseInt(keys[1])).documentRoot = props.getProperty(key);
            }
        }

        System.out.println("HttpStatic server started on port " + port + ".");
        System.out.println(domains.size() + " domains definitions loaded.");

        ServerSocket ss = new ServerSocket(port);

        // init dns server
        ServerDNS localDnsServer = new ServerDNS(8000, 9000);
        // new thread for registrations
        threadPool.addJob(localDnsServer);
        try {

            while (true) {
                Socket s = ss.accept();
                System.out.println(" -> New connection accepted [" + s.getInetAddress() + ":" + s.getPort() + "]");
                try {
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String domainName = "";
                    String sessionInfo = "";
                    String line = null;
                    String dnsClientLookup = "";
                    String dnsLookup = "";
                    String registrarLookup = "";

                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        System.out.println("header: " + line);
                        if (line.startsWith("GET ")) {
                            filename = line.replace("GET ", "").split(" ")[0];
                            dnsClientLookup = line.split("dns/client/").length > 1? line.split("dns/client/")[1]: line;
                            System.out.print("client"+ dnsClientLookup);
                            dnsLookup = line.split("dns/server/").length > 1? line.split("dns/server/")[1]: line;
                            registrarLookup = (line.split("registrar/")).length > 1? line.split("registrar/")[1] :line;
                            // launch dns or client
                            if(!dnsClientLookup.equals(line)){
                                dnsClient = true;
                                dnsRequestType = "client";
                                domainToLookUp = dnsClientLookup.split(" ")[0];
                            }

                            if(!dnsLookup.equals(line)){
                                dnsRequestType = "localdns";
                                dnsClientServer = true;
                                domainToLookUp = dnsLookup.split(" ")[0];
                            }
                            if(!registrarLookup.equals(line)){
                                dnsRequestType = "registrar";
                                dnsRegistrar = true;
                                domainToLookUp = registrarLookup.split(" ")[0];
                            }
                        }
                        if (line.startsWith("Host: ")) {
                            domainName = line.replace("Host: ", "").split(":")[0];
                            hostName = line.replace("Host: ", "");

                        }
                        if(withSession && line.startsWith("Set-Cookie:")){
                            sessionInfo = line.replace("Set-Cookie:", "");
                        }
                    }

                    for (Map.Entry<Integer, Domain> entry : domains.entrySet()) {
                        Domain dom = entry.getValue();
                        if (dom.name.equals(domainName)) {
                            if(withSession){
                                HttpRequestWithSession request = new HttpRequestWithSession(s, dom.documentRoot, filename, hostName, sessionInfo);
                                threadPool.addJob(request);

                            }else if(!dnsRequestType.equals("")){
                                HttpDnsClientHandler request = new HttpDnsClientHandler(s, domainToLookUp,dnsRequestType);
                                threadPool.addJob(request);
                            }
                            else {
                                HttpRequestHandler request = new HttpRequestHandler(s, dom.documentRoot, filename, hostName);
                                threadPool.addJob(request);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e);
                }

            }
        } finally {
            if(ss != null)
                ss.close();
        }
    }
}
