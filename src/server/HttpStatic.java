package server;

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

    public static void main(String[] args) throws Exception {
        int port = PORT;
        String hostName = null;

        if (args.length > 0) {
            port = Integer.valueOf(args[0]);
        }

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
        try {

            while (true) {
                Socket s = ss.accept();
                System.out.println(" -> New connection accepted [" + s.getInetAddress() + ":" + s.getPort() + "]");
                try {
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String domainName = "";
                    String line = null;

                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        System.out.println("header: " + line);
                        if (line.startsWith("GET ")) {
                            filename = line.replace("GET ", "").split(" ")[0];
                        }
                        if (line.startsWith("Host: ")) {
                            domainName = line.replace("Host: ", "").split(":")[0];
                            hostName = line.replace("Host: ", "");
                        }
                    }

                    for (Map.Entry<Integer, Domain> entry : domains.entrySet()) {
                        Domain dom = entry.getValue();
                        if (dom.name.equals(domainName)) {
                            HttpRequestHandler request = new HttpRequestHandler(s, dom.documentRoot, filename, hostName);
                            Thread thread = new Thread(request);
                            thread.start();
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e);
                }

            }
        } finally {
            ss.close();
        }
    }
}
