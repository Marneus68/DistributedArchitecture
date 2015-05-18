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
    public static final int PORT = 8181;

    public static void main(String [] args) throws Exception{

        Map<Integer, Domain> domains = new HashMap<Integer, Domain>();

        Properties props = new Properties();
        InputStream input = new FileInputStream("config.ini");
        props.load(input);
        for (String key : props.stringPropertyNames())
        {
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

        System.out.println("HttpStatic server started on port " + PORT + ".");
        System.out.println(domains.size() + " domains definitions loaded.");

        ServerSocket ss = new ServerSocket(PORT);
        try {
            while(true) {
                Socket s = ss.accept();
                System.out.println(" -> New connection accepted [" + s.getInetAddress() + ":" + s.getPort() + "]");
                try {
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader( s.getInputStream()));
                    String domainName = "";
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("Host: ")) {
                            domainName = line.replace("Host: ", "").split(":")[0];
                            break;
                        }
                    }

                    for(Map.Entry<Integer, Domain> entry : domains.entrySet()) {
                        //Integer key = entry.getKey();
                        Domain dom = entry.getValue();
                        if (dom.name.equals(domainName)) {
                            HttpRequestHandler request = new HttpRequestHandler(s, dom.documentRoot);
                            Thread thread = new Thread(request);
                            thread.start();
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e);
                } finally {
                    s.close();
                }
            }
        } finally {
            ss.close();
        }
    }
}
