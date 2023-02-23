/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client;

import net.tbsoft.oragentclient.util.BytesUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class RequestJob implements Runnable {
    public static final int REQUEST_MAX_SIZE = 4096;
    final static Logger log = LoggerFactory.getLogger(RequestJob.class);
    private final Socket socket;
    private final String serverName;

    private final String jobName;

    final private BlockingQueue<byte[]> blockingQueue;

    public RequestJob(String jobName, String serverName, Socket socket, BlockingQueue<byte[]> blockingQueue) {
        this.socket = socket;
        this.serverName = serverName;
        this.jobName = jobName;
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(jobName);
        try {
            final InputStream iStream = socket.getInputStream();
            final OutputStream oStream = socket.getOutputStream();

            log.info("[{}] socket timeout={}", jobName, socket.getSoTimeout());
            while (true) {
                //1.read
                byte[] requestBytes = new byte[REQUEST_MAX_SIZE];
                log.info("[{}] wait read REQ...", jobName);
                long start0 = System.currentTimeMillis();
                int readed;
                socket.setSoTimeout(15000);
                readed = iStream.read(requestBytes, 0, REQUEST_MAX_SIZE);

                if (readed <= 0) {
                    log.warn("[{}] read readed={} break", jobName, readed);
                    break;
                }
                String request = new String(requestBytes, 0, readed);
                log.info("[{}] read REQ:{} readed={},delay={}ms", jobName, request, readed, (System.currentTimeMillis() - start0));
                Element element = DocumentHelper.parseText(request).getRootElement();
                String oragentNo = element.attributeValue("OragentNo");
                String size = element.attributeValue("SIZE");
                String zip = element.attributeValue("Zip", "0");
                String zipSize = element.attributeValue("ZipSize");
                //2.write
                Element command = DocumentHelper.createElement("DATARESULT");

                String asXML = command.addAttribute("Ok", "1").asXML();
                byte[] writeContent = asXML.getBytes();
                BytesUtils.writeBytes(oStream, writeContent, 0, writeContent.length);
                log.info("[{}] write RES:{},len={},delay={}ms", jobName, asXML, writeContent.length, (System.currentTimeMillis() - start0));
                //3.read data
                int readSize = Integer.parseInt(size);
                if ("1".equals(zip))
                    readSize = Integer.parseInt(zipSize);
                byte[] dataBytes = new byte[readSize];
                log.info("[{}] wait read DATA[{}]...", jobName, readSize);

                start0 = System.currentTimeMillis();
                socket.setSoTimeout(0);
                readed = BytesUtils.readBytes(iStream, dataBytes, readSize);
                if (readed == 0) {
                    log.warn("[{}] read DATA:readed={}/{} continue", jobName, readed, readSize);
                    asXML = command.addAttribute("Ok", "1").asXML();
                    writeContent = asXML.getBytes();
                    BytesUtils.writeBytes(oStream, writeContent, 0, writeContent.length);
                    continue;
                }
                //todo zip 解压
                byte[] content = !"1".equals(zip) ? dataBytes : BytesUtils.decompress(dataBytes);
                blockingQueue.put(content);
                log.info("[{}] read DATA:readed={}/{},delay={}ms", jobName, readed, readSize, (System.currentTimeMillis() - start0));
                //4.write
                asXML = command.addAttribute("Ok", "1").asXML();
                writeContent = asXML.getBytes();
                BytesUtils.writeBytes(oStream, writeContent, 0, writeContent.length);
                log.info("[{}] write RES:{},len={},delay={}ms", jobName, asXML, writeContent.length, (System.currentTimeMillis() - start0));
            }
        } catch (IOException | DocumentException e) {
            log.warn("[{}] error:{}", jobName, e.getMessage());
        } catch (InterruptedException e) {
            // do nothing
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
        log.info("[{}] exit", jobName);
    }
}
