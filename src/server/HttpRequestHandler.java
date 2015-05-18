package server;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * File created by duane
 * 2015-05-18 | 12:19 PM
 */
public class HttpRequestHandler implements Runnable {
    final static String CRLF = "\r\n";

    String contentPath;
    Socket socket;
    InputStream input;
    OutputStream output;
    BufferedReader br;
    String filename;
    String domainName;

    public HttpRequestHandler(Socket socket, String contentPath, String filenameIn, String domainNameIn) throws Exception {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.contentPath = contentPath;
        filename = filenameIn;
        domainName = domainNameIn;

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
            String removeStartPath = "";
            if (contentPath.startsWith("~" + File.separator)) {
                contentPath = System.getProperty("user.home") + contentPath.substring(1);
                removeStartPath =  contentPath;
            }
            else {
                removeStartPath = contentPath;
            }



            if(!filename.isEmpty() && !filename.equals("/favicon.ico")){
                System.out.println("folder filename"+ filename);
                contentPath += filename;
            }

            File repo = new File(contentPath);

            String serverLine = "Server: Simple Java Http Server";
            String statusLine = null;
            String contentTypeLine = null;
            String entityBody = null;
            String contentLengthLine = "";
            FileInputStream fis = null;
            if (repo.isFile()) {

                boolean fileExists = true;
                try {
                    fis = new FileInputStream(contentPath);
                } catch (FileNotFoundException e) {
                    fileExists = false;
                }
                statusLine = "HTTP/1.0 200 OK" + CRLF;
                contentTypeLine = "Content-type: " + contentType(contentPath) + CRLF;
                contentLengthLine = "Content-Length: "
                        + (new Integer(fis.available())).toString() + CRLF;
            }
            else if(repo.isDirectory()) {

                File[] fileList = repo.listFiles();
                String htmlList= "";
                for(int i=0; i < fileList.length; i++){
                    String href = String.valueOf(fileList[i]);
                    href = href.replace(contentPath, "")+filename;
                    System.out.println("file href"+ href+"\n  String to trim "+removeStartPath+" content path: "+contentPath);
                    htmlList += "<a href=\"http://"+domainName+":8181/"+href+"\">"+ fileList[i]+ "</a><br>";
                }
                statusLine = "HTTP/1.0 200 OK" + CRLF;
                contentTypeLine = "Content-type: " + "text/html" + CRLF;
                entityBody = "<HTML>"
                        + "<HEAD><TITLE>repository</TITLE></HEAD>"
                        + "<BODY>repository"
                        +"<p>"+ htmlList
                        +"</p>"
                        + "<br>usage:http://yourHostName:port/"
                        + "fileName.html</BODY></HTML>";

            }
            else {
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
            if (repo.isFile()) {
                sendBytes(fis, output);
                fis.close();
            } else  {
                output.write(entityBody.getBytes());
            }

            try {
                output.close();
                br.close();
                socket.close();
            } catch (Exception e){
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
