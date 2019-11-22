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
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
    private static final int PORT_NUMBER = 8085;
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private MaliciousURLModel model = MaliciousURLModel.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(FilterProxy.class);
    /**
     * ArrayList of threads that are currently running and servicing requests. This list is required
     * in order to join all threads on closing of server
     */
    private static ArrayList<Thread> servicingThreads;

    /**
     * Create the FilterProxy Server
     *
     * @param port Port number to run proxy server from.
     */
    public FilterProxy(int port) {

        // Create array list to hold servicing threads
        servicingThreads = new ArrayList<>();

        // Start dynamic manager on a separate thread.
        Runtime.getRuntime().addShutdownHook(new Thread(this)); // On a kill -15 close gracefully

        try {
            // Create the Server Socket for the FilterProxy
            serverSocket = new ServerSocket(port);
            logger.info("Waiting for client on port " + serverSocket.getLocalPort() + "..");
            running = true;
        }

        // Catch exceptions associated with opening socket
        catch (SocketException se) {
            logger.error("Socket Exception when connecting to client");
            se.printStackTrace();
        } catch (SocketTimeoutException ste) {
            logger.error("Timeout occured while connecting to client");
        } catch (IOException io) {
            logger.error("IO exception when connecting to client");
        }
    }

    /**
     * Listens to port and accepts new socket connections. Creates a new thread to handle the
     * request and passes it the socket connection and continues listening.
     */
    public void listen() {

        while (running) {
            try {
                // serverSocket.accpet() Blocks until a connection is made
                Socket socket = serverSocket.accept();

                // Create new Thread and pass it Runnable RequestHandler, and the singleton model
                // object
                Thread thread = new Thread(new RequestHandler(socket, model));

                // Key a reference to each thread so they can be joined later if necessary
                servicingThreads.add(thread);

                thread.start();
            } catch (SocketException e) {
                logger.info("Server closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the blocked and cached sites to a file so they can be re loaded at a later time. Also
     * joins all of the RequestHandler threads currently servicing requests.
     */
    private void closeServer() {
        logger.info("\nClosing Server..");
        running = false;

        // Close Server Socket
        try {
            logger.debug("Terminating Connection");
            serverSocket.close();
            for (Thread t : servicingThreads) {
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
