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

import ai.djl.modality.Classifications;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class RequestHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket clientSocket;
    private BufferedReader proxyToClientReader;
    private BufferedWriter proxyToClientWriter;
    private final MaliciousURLModel maliciousURLModel;

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
            logger.error("", e);
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
            logger.error("Error reading request from client{}", e.getMessage());
            return;
        }

        logger.info("Request Received {}", requestString);
        String[] parts = requestString.split(" ", 3);
        String requestType = parts[0];
        String urlString = parts[1];
        if (!urlString.toLowerCase().startsWith("http")) {
            urlString = "http://" + urlString;
        }
        // malicious url detector
        Classifications output = maliciousURLModel.inference(urlString);
        logger.debug(output.toString());
        if (output.get("malicious").getProbability() >= 0.50) {
            logger.info("Malicious URL detected and blocked {}", urlString);
            blockedMaliciousSiteRequested();
            return;
        }

        if (requestType.equals("CONNECT")) {
            logger.info("HTTPS Request for {}\n", urlString);
            handleSecureRequest(urlString);
        } else {
            logger.info("HTTP Request for {}\n", urlString);
            handleRequest(urlString);
        }
    }

    /**
     * Handle regular HTTP requests
     *
     * @param urlString URL of the file requested
     */
    private void handleRequest(String urlString) {
        try {
            // Define some response objects
            HttpResponse responseOk = new BasicHttpResponse(HttpStatus.SC_OK, "OK");
            responseOk.setVersion(HttpVersion.HTTP_1_1);
            HttpResponse responseNotFound =
                    new BasicHttpResponse(HttpStatus.SC_NOT_FOUND, "NOT FOUND");
            responseNotFound.setVersion(HttpVersion.HTTP_1_1);
            String responseHeader = "\nProxy-agent: FilterProxyServer/1.0\n\r\n";
            String fileType = urlString.substring(urlString.lastIndexOf("."));
            // Check if request is for a image file resource
            String[] imageTypes = {".png", ".jpg", ".jpeg", ".svg", ".gif"};
            if (Arrays.stream(imageTypes).parallel().anyMatch(fileType::contains)) {
                URL remoteURL = new URL(urlString);
                BufferedImage image = ImageIO.read(remoteURL);

                if (image != null) {
                    proxyToClientWriter.write(responseOk.toString());
                    proxyToClientWriter.flush();

                    ImageIO.write(image, fileType.substring(1), clientSocket.getOutputStream());

                } else {
                    logger.error("Sending 404 to client as image wasn't received from server");
                    proxyToClientWriter.write(responseNotFound + responseHeader);
                    proxyToClientWriter.flush();
                    return;
                }
            }
            // HTML or raw text files
            else {
                URL remoteURL = new URL(urlString);
                // Create a connection to remote server
                HttpURLConnection proxyToServerCon = (HttpURLConnection) remoteURL.openConnection();
                proxyToServerCon.setRequestProperty(
                        "Content-Type", "application/x-www-form-urlencoded");
                proxyToServerCon.setRequestProperty("Content-Language", "en-US");
                proxyToServerCon.setUseCaches(false);
                proxyToServerCon.setDoOutput(true);
                InputStream is = proxyToServerCon.getInputStream();
                BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(is));
                proxyToClientWriter.write(responseOk + responseHeader);
                // Read Content line by line
                String line;
                while ((line = proxyToServerBR.readLine()) != null) {
                    proxyToClientWriter.write(line);
                }
                proxyToClientWriter.flush();
                proxyToServerBR.close();
            }
            if (proxyToClientWriter != null) {
                proxyToClientWriter.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /** Handles URLs predicted as malicious. */
    private void blockedMaliciousSiteRequested() {
        try {
            HttpResponse response = new BasicHttpResponse(HttpStatus.SC_BAD_REQUEST, "MAL");
            response.setVersion(HttpVersion.HTTP_1_1);
            HttpEntity httpEntity = new FileEntity(new File("index.html"), ContentType.WILDCARD);
            BufferedWriter bufferedWriter =
                    new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            bufferedWriter.write(response.toString());
            String headers =
                    "Proxy-agent: FilterProxy/1.0\r\n"
                            + httpEntity.getContentType()
                            + "\r\n"
                            + "Content-Length: "
                            + httpEntity.getContentLength()
                            + "\r\n\r\n";
            bufferedWriter.write(headers);
            // Pass index.html content
            bufferedWriter.write(EntityUtils.toString(httpEntity));
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException | ParseException e) {
            logger.error("Error writing to client when requested a blocked site", e);
        }
    }

    /**
     * Handles HTTPS requests between client and remote server
     *
     * @param urlString desired file to be transmitted over https
     */
    private void handleSecureRequest(String urlString) {
        HttpResponse responseOk = new BasicHttpResponse(HttpStatus.SC_OK, "Connection Established");
        responseOk.setVersion(HttpVersion.HTTP_1_1);
        HttpResponse responseTimeout =
                new BasicHttpResponse(HttpStatus.SC_GATEWAY_TIMEOUT, "Timeout Occurred");
        responseTimeout.setVersion(HttpVersion.HTTP_1_1);
        String responseHeader = "\nProxy-agent: FilterProxyServer/1.0\n\r\n";
        String url = urlString.substring(7);
        String[] parts = url.split(":");
        url = parts[0];
        int port = Integer.parseInt(parts[1]);

        try {
            InetAddress address = InetAddress.getByName(url);
            Socket proxyToServerSocket = new Socket(address, port);
            proxyToServerSocket.setSoTimeout(7000);
            // Send Connection established to the client
            proxyToClientWriter.write(responseOk + responseHeader);
            proxyToClientWriter.flush();

            BufferedWriter proxyToServerBW =
                    new BufferedWriter(
                            new OutputStreamWriter(proxyToServerSocket.getOutputStream()));
            BufferedReader proxyToServerBR =
                    new BufferedReader(new InputStreamReader(proxyToServerSocket.getInputStream()));

            InputStream proxyToClientIS = clientSocket.getInputStream();
            OutputStream proxyToServerOS = proxyToServerSocket.getOutputStream();
            new Thread(
                            () -> {
                                try {
                                    // Read byte by byte from client and send directly to server
                                    byte[] buffer = new byte[8192];
                                    int content;
                                    do {
                                        content = proxyToClientIS.read(buffer);
                                        if (content > 0) {
                                            proxyToServerOS.write(buffer, 0, content);
                                            if (proxyToClientIS.available() < 1) {
                                                proxyToServerOS.flush();
                                            }
                                        }
                                    } while (content >= 0);
                                } catch (IOException e) {
                                    logger.error(e.getMessage());
                                }
                            })
                    .start();
            // Listen to remote server and relay to client
            try {
                byte[] buffer = new byte[8192];
                int content;
                do {
                    content = proxyToServerSocket.getInputStream().read(buffer);
                    if (content > 0) {
                        clientSocket.getOutputStream().write(buffer, 0, content);
                        if (proxyToServerSocket.getInputStream().available() < 1) {
                            clientSocket.getOutputStream().flush();
                        }
                    }
                } while (content >= 0);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            proxyToServerSocket.close();
            proxyToServerBR.close();
            proxyToServerBW.close();
            proxyToClientWriter.close();
        } catch (Exception e) {
            try {
                proxyToClientWriter.write(responseTimeout + responseHeader);
                proxyToClientWriter.flush();
            } catch (IOException ioException) {
                logger.error("", e);
            }
        }
    }
}
