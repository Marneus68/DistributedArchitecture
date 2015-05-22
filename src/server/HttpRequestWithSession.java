package server;


import java.io.*;
import java.net.Socket;

/**
 * Created by madalien on 22/05/15.
 */
public class HttpRequestWithSession implements Runnable {

    final static String CRLF = "\r\n";

    String contentPath;
    Socket socket;
    InputStream input;
    OutputStream output;
    BufferedReader br;
    Socket sessionSocket;
    String filename;
    String domainName;
    String sessionInfo;
    InputStream sessionIStream = null;
    OutputStream sessionOStream = null;

    public HttpRequestWithSession(Socket socket, String contentPath, String filenameIn, String domainNameIn, String sessionInfoIn) throws Exception {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.contentPath = contentPath;
        filename = filenameIn;
        domainName = domainNameIn;
        sessionInfo = sessionInfoIn;
        //TODO get from config
        sessionSocket = new Socket("127.0.0.1", 8989);
        sessionIStream = sessionSocket.getInputStream();
        sessionOStream = sessionSocket.getOutputStream();
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private String getSessionID(){

        String askId = "SNEW:12345" +CRLF;
        byte[] reply = new byte[1024];
        String sessionId = "";
        int bytesRead = 0;
                try {

                    sessionOStream.write(askId.getBytes(), 0, askId.getBytes().length);

                    while ((bytesRead = sessionIStream.read(reply)) != 0) {


                        System.out.println(" bytes reed"+ bytesRead);
                        if(bytesRead > 0){
                            System.out.println(" bytes string"+ new String(reply));
                            sessionId = new String(reply);
                            break;
                        }

                    }
                    sessionIStream.close();
                    sessionOStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(" server in stream");

                }
        return sessionId;
    }

    private void processRequest() throws Exception {

        System.out.println(" =======> sessionID:"+ sessionInfo);
        String sessionID = sessionInfo.equals("") ? getSessionID():sessionInfo;
        while (true) {

            System.out.println(" =======> sessionID:"+ sessionID);
            String removeStartPath = "";

            if (contentPath.startsWith("~" + File.separator)) {
                contentPath = System.getProperty("user.home") + contentPath.substring(1);
                removeStartPath = contentPath;
            } else {
                removeStartPath = contentPath;
            }

            if (!filename.isEmpty() && !filename.equals("/favicon.ico")) {
                contentPath += filename;
            }

            File repo = new File(contentPath);
            String serverLine = "Server: Simple Java Http Server"+CRLF;
            String statusLine = null;
            String contentTypeLine = null;
            String entityBody = null;
            String contentLengthLine = "";
            String sessionContent = "Set-Cookie: session="+sessionID+"; path=/"+ CRLF;
            FileInputStream fis = null;
            if (repo.isFile()) {

                boolean fileExists = true;
                try {
                    fis = new FileInputStream(contentPath);
                } catch (FileNotFoundException e) {
                    fileExists = false;
                    System.out.println("input file error"+ e.getMessage());
                }
                statusLine = "HTTP/1.1 200 OK" + CRLF;
                contentTypeLine = "Content-type: " + contentType(contentPath) + CRLF;
                contentLengthLine = "Content-Length: " + (new Integer(fis.available())).toString() + CRLF;

            }
            else if (repo.isDirectory()) {

                File[] fileList = repo.listFiles();
                String htmlList = "";
                for (int i = 0; i < fileList.length; i++) {
                    String href = String.valueOf(fileList[i]);
                    href = href.replace(removeStartPath, "");
                    htmlList += "<a href=\"http://" + domainName + href + "\">" + fileList[i] + "</a><br>";
                }
                statusLine = "HTTP/1.1 200 OK" + CRLF;
                contentTypeLine = "Content-type: " + "text/html" + CRLF;
                entityBody = "<HTML>"
                        + "<HEAD><TITLE>repository</TITLE></HEAD>"
                        + "<BODY>repository"
                        + "<p>" + htmlList
                        + "</p>"
                        + "<br>usage:http://yourHostName:port/"
                        + "fileName.html</BODY></HTML>";

            } else {
                statusLine = "HTTP/1.1 404 Not Found" + CRLF;
                contentTypeLine = "Content-type: " + "text/html" + CRLF;
                entityBody = "<HTML>"
                        + "<HEAD><TITLE>404 Not Found</TITLE></HEAD>"
                        + "<BODY>404 Not Found"
                        + "</BODY></HTML>";
            }

            // Send headers
            output.write(statusLine.getBytes());
            output.write(serverLine.getBytes());
            output.write(contentTypeLine.getBytes());
            output.write(sessionContent.getBytes());
            output.write(contentLengthLine.getBytes());
            output.write(CRLF.getBytes());
            // Send the entity body.
            if (repo.isFile()) {
                sendBytes(fis, output);
                fis.close();
            } else {
                output.write(entityBody.getBytes());
            }

            try {
                if(output != null)
                output.close();

                if(br != null)
                br.close();

                if(socket != null)
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error while sending back to client"+ e.getMessage());
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
