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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FilterProxy class receives requests on a server socket, and handles requests on indvidual threads
 * looking for malicious url patterns.
 */
public class FilterProxy implements Runnable {

    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "all");
        FilterProxy myFilterProxy = new FilterProxy(PORT_NUMBER);
        myFilterProxy.listen();
    }

    // Port number can be configured here
    private static final int PORT_NUMBER = 8080;
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private MaliciousURLModel model = MaliciousURLModel.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(FilterProxy.class);
    private static ArrayList<Thread> requestHandlerThreads;

    /**
     * Create the FilterProxy Server
     *
     * @param port Port to forward proxy server requests to.
     */
    public FilterProxy(int port) {

        requestHandlerThreads = new ArrayList<>();

        // Start dynamic manager on a separate thread.
        Runtime.getRuntime().addShutdownHook(new Thread(this)); // On a kill -15 close gracefully

        try {
            serverSocket = new ServerSocket(port);
            logger.info("Waiting for request(s) on port " + serverSocket.getLocalPort());
            running = true;
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Listens to port and accepts new socket connections. Creates a new thread to handle the
     * request and passes it the socket connection and continues listening.
     */
    public void listen() {

        while (running) {
            try {
                Socket socket = serverSocket.accept();

                // Create new Thread and pass it Runnable RequestHandler, and the singleton model
                Thread thread = new Thread(new RequestHandler(socket, model));
                requestHandlerThreads.add(thread);
                thread.start();
            } catch (IOException e) {
                logger.info(e.getMessage());
            }
        }
    }

    /** Close request handler threads, once they are done */
    private void closeServer() {
        logger.info("Closing Server\n");
        running = false;
        try {
            logger.debug("Terminating Connection");
            serverSocket.close();
            for (Thread t : requestHandlerThreads) {
                t.join();
            }
        } catch (Exception e) {
            logger.error("Exception closing proxy's server socket");
            e.printStackTrace();
        }
    }

    /** Close server when receiving message from hook */
    @Override
    public void run() {
        while (running) {
            closeServer();
        }
    }
}
