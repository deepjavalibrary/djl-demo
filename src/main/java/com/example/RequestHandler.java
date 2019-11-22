/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.example;

import ai.djl.MalformedModelException;
import ai.djl.modality.Classifications;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader proxyToClientReader;
    private BufferedWriter proxyToClientWriter;
    private final MaliciousURLModel maliciousURLModel;
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    /**
     * Creates a RequestHandler object capable of servicing HTTP/HTTPS GET requests
     *
     * @param clientSocket socket connected to the client
     */
    RequestHandler(Socket clientSocket, MaliciousURLModel model) {
        this.clientSocket = clientSocket;
        this.maliciousURLModel = model;
        try {
            this.clientSocket.setSoTimeout(6000);
            proxyToClientReader =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            proxyToClientWriter =
                    new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines if URL is malicious loading the model, then handles content based on HTTP vs HTTPS
     */
    @Override
    public void run() {

        // Get Request from client
        String requestString;
        try {
            requestString = proxyToClientReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error reading request from client");
            return;
        }

        logger.info("Request Received " + requestString);

        // Get the Request type
        String request = requestString.substring(0, requestString.indexOf(' '));
        // remove request type and space
        String urlString = requestString.substring(requestString.indexOf(' ') + 1);

        // Remove everything past next space
        urlString = urlString.substring(0, urlString.indexOf(' '));

        if (!urlString.substring(0, 4).equals("http")) {
            String temp = "http://";
            urlString = temp + urlString;
        }
        // malicious url detector
        try {
            Classifications output;
            synchronized (maliciousURLModel) {
                maliciousURLModel.defineModel();
                maliciousURLModel.loadModel();
                output = maliciousURLModel.inference(urlString);
            }
            if (output.get("malicious").getProbability() >= 0.50) {
                logger.info("Blocked site : " + urlString);
                blockedMaliciousSiteRequested();
                return;
            }
        } catch (IOException | MalformedModelException e) {
            throw new RuntimeException(e);
        }
        if (request.equals("CONNECT")) {
            logger.info("HTTPS Request for : " + urlString + "\n");
            handleSecureRequest(urlString);
        } else {
            handleRequest(urlString);
        }
    }

    /**
     * Sends the contents of the file specified by the urlString to the client
     *
     * @param urlString URL ofthe file requested
     */
    private void handleRequest(String urlString) {
        try {
            // Define some reponse objects
            HttpResponse responseOk =
                    new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
            HttpResponse responseNotFound =
                    new BasicHttpResponse(
                            HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "NOT FOUND");
            String responseHeader = "\nProxy-agent: FilterProxyServer/1.0\n\r\n";
            // Get file extension to handle content type
            int fileExtensionIndex = urlString.lastIndexOf(".");
            String fileExtension;
            fileExtension = urlString.substring(fileExtensionIndex);

            // Get the initial file name
            String fileName = urlString.substring(0, fileExtensionIndex);

            // Trim off http://www. as no need for it in file name
            fileName = fileName.substring(fileName.indexOf('.') + 1);

            // Remove any illegal characters from file name
            fileName = fileName.replace("/", "__");
            fileName = fileName.replace('.', '_');

            // Trailing / result in index.html of that directory being fetched
            if (fileExtension.contains("/")) {
                fileExtension = fileExtension.replace("/", "__");
                fileExtension = fileExtension.replace('.', '_');
                fileExtension += ".html";
            }

            fileName = fileName + fileExtension;
            // Check if file is an image
            if ((fileExtension.contains(".png"))
                    || fileExtension.contains(".jpg")
                    || fileExtension.contains(".jpeg")
                    || fileExtension.contains(".gif")) {
                // Create the URL
                URL remoteURL = new URL(urlString);
                BufferedImage image = ImageIO.read(remoteURL);

                if (image != null) {
                    // Send response code to client
                    proxyToClientWriter.write(
                            responseOk.getStatusLine().toString() + responseHeader);
                    proxyToClientWriter.flush();

                    // Send them the image data
                    ImageIO.write(
                            image, fileExtension.substring(1), clientSocket.getOutputStream());

                    // No image received from remote server
                } else {
                    logger.error(
                            "Sending 404 to client as image wasn't received from server"
                                    + fileName);
                    proxyToClientWriter.write(responseNotFound + responseHeader);
                    proxyToClientWriter.flush();
                    return;
                }
            }

            // File is a text file
            else {
                // Create the URL
                URL remoteURL = new URL(urlString);
                // Create a connection to remote server
                HttpURLConnection proxyToServerCon = (HttpURLConnection) remoteURL.openConnection();
                proxyToServerCon.setRequestProperty(
                        "Content-Type", "application/x-www-form-urlencoded");
                proxyToServerCon.setRequestProperty("Content-Language", "en-US");
                proxyToServerCon.setUseCaches(false);
                proxyToServerCon.setDoOutput(true);
                // Create Buffered Reader from remote Server
                InputStream is = proxyToServerCon.getInputStream();
                BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(is));
                // Send success code to client
                proxyToClientWriter.write(responseOk + responseHeader);
                String line;
                // Read from input stream between proxy and remote server
                while ((line = proxyToServerBR.readLine()) != null) {
                    // Send on data to client
                    proxyToClientWriter.write(line);
                }

                // Ensure all data is sent by this point
                proxyToClientWriter.flush();

                // Close Down Resources
                proxyToServerBR.close();
            }
            if (proxyToClientWriter != null) {
                proxyToClientWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Handles URLs predicted as malicious. */
    private void blockedMaliciousSiteRequested() {
        try {
            HttpResponse response =
                    new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "MAL");
            HttpEntity httpEntity = new FileEntity(new File("index.html"), ContentType.WILDCARD);
            BufferedWriter bufferedWriter =
                    new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            bufferedWriter.write(response.getStatusLine().toString());
            String headers =
                    "Proxy-agent: FilterProxy/1.0\r\n"
                            + httpEntity.getContentType().toString()
                            + "\r\n"
                            + "Content-Length: "
                            + httpEntity.getContentLength()
                            + "\r\n\r\n";
            bufferedWriter.write(headers);
            // Pass index.html content
            bufferedWriter.write(EntityUtils.toString(httpEntity));
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            logger.error("Error writing to client when requested a blocked site");
            e.printStackTrace();
        }
    }

    /**
     * Handles HTTPS requests between client and remote server
     *
     * @param urlString desired file to be transmitted over https
     */
    private void handleSecureRequest(String urlString) {
        // Extract the URL and port of remote
        String url = urlString.substring(7);
        String[] pieces = url.split(":");
        url = pieces[0];
        int port = Integer.parseInt(pieces[1]);
        HttpResponse responseOk =
                new BasicHttpResponse(
                        HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "Connection Established");
        HttpResponse responseTimeout =
                new BasicHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpStatus.SC_GATEWAY_TIMEOUT,
                        "Timeout Occured after 10s");

        String responseHeader = "\nProxy-agent: FilterProxyServer/1.0\n\r\n";
        try {
            // Only first line of HTTPS request has been read at this point (CONNECT *)
            // Read (and throw away) the rest of the initial data on the stream
            for (int i = 0; i < 5; i++) {
                proxyToClientReader.readLine();
            }

            // Get actual IP associated with this URL through DNS
            InetAddress address = InetAddress.getByName(url);

            // Open a socket to the remote server
            Socket proxyToServerSocket = new Socket(address, port);
            proxyToServerSocket.setSoTimeout(7000);

            // Send Connection established to the client
            proxyToClientWriter.write(responseOk.getStatusLine().toString() + responseHeader);
            proxyToClientWriter.flush();

            // Client and Remote will both start sending data to proxy at this point
            // Proxy needs to asynchronously read data from each party and send it to the other
            // party

            // Create a Buffered Writer betwen proxy and remote
            BufferedWriter proxyToServerBW =
                    new BufferedWriter(
                            new OutputStreamWriter(proxyToServerSocket.getOutputStream()));

            // Create Buffered Reader from proxy and remote
            BufferedReader proxyToServerBR =
                    new BufferedReader(new InputStreamReader(proxyToServerSocket.getInputStream()));

            // Create a new thread to listen to client and transmit to server
            ClientToServerHttpsTransmit clientToServerHttps =
                    new ClientToServerHttpsTransmit(
                            clientSocket.getInputStream(), proxyToServerSocket.getOutputStream());

            Thread httpsClientToServer = new Thread(clientToServerHttps);
            httpsClientToServer.start();

            // Listen to remote server and relay to client
            try {
                byte[] buffer = new byte[4096];
                int read;
                do {
                    read = proxyToServerSocket.getInputStream().read(buffer);
                    if (read > 0) {
                        clientSocket.getOutputStream().write(buffer, 0, read);
                        if (proxyToServerSocket.getInputStream().available() < 1) {
                            clientSocket.getOutputStream().flush();
                        }
                    }
                } while (read >= 0);
            } catch (SocketTimeoutException e) {
                logger.error("Socket timed out");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Close Down Resources
            proxyToServerSocket.close();
            proxyToServerBR.close();
            proxyToServerBW.close();
            if (proxyToClientWriter != null) {
                proxyToClientWriter.close();
            }
        } catch (SocketTimeoutException e) {
            try {
                proxyToClientWriter.write(
                        responseTimeout.getStatusLine().toString() + responseHeader);
                proxyToClientWriter.flush();
            } catch (IOException ioe) {
                logger.error("There was a Socket Exception, handle it results in IOException");
                ioe.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Error on HTTPS : " + urlString);
            e.printStackTrace();
        }
    }

    /**
     * Listen to data from client and transmits it to server. This is done on a separate thread as
     * must be done asynchronously to reading data from server and transmitting that data to the
     * client.
     */
    static class ClientToServerHttpsTransmit implements Runnable {

        InputStream proxyToClientIS;
        OutputStream proxyToServerOS;

        /**
         * Creates Object to Listen to Client and Transmit that data to the server
         *
         * @param proxyToClientIS Stream that proxy uses to receive data from client
         * @param proxyToServerOS Stream that proxy uses to transmit data to remote server
         */
        ClientToServerHttpsTransmit(InputStream proxyToClientIS, OutputStream proxyToServerOS) {
            this.proxyToClientIS = proxyToClientIS;
            this.proxyToServerOS = proxyToServerOS;
        }

        @Override
        public void run() {
            try {
                // Read byte by byte from client and send directly to server
                byte[] buffer = new byte[4096];
                int read;
                do {
                    read = proxyToClientIS.read(buffer);
                    if (read > 0) {
                        proxyToServerOS.write(buffer, 0, read);
                        if (proxyToClientIS.available() < 1) {
                            proxyToServerOS.flush();
                        }
                    }
                } while (read >= 0);
            } catch (SocketTimeoutException ste) {
                logger.error("Socket timed out");
            } catch (IOException e) {
                logger.error("Proxy to client HTTPS read timed out");
                e.printStackTrace();
            }
        }
    }
}
