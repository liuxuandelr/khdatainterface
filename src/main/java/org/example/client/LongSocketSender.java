package org.example.client;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.config.DataType;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LongSocketSender extends Thread {
    public static int MAX_JSON_SIZE = 1024 * 512;
    public static String HEADER = "POINT-BEGIN\n\n";
    public static String TAIL = "\n\nPOINT-END\n\n";
    private BlockingQueue<JSONArray> blockingQueue = new LinkedBlockingDeque<>();

    private int maxSize;
    private String host;
    private int port;

    private DataType dataType;
    private Socket socket;

    private AtomicBoolean started = new AtomicBoolean(false);

    public LongSocketSender(String host, int port, DataType dataType) {
        this(host, port, MAX_JSON_SIZE, dataType);
    }

    public LongSocketSender(String host, int port, int maxSize, DataType dataType) {
        this.host = host;
        this.port = port;
        this.maxSize = maxSize;
        this.dataType = dataType;
        this.socket = null;
    }

    public boolean addData(JSONArray dcJsonArray) {
        return this.blockingQueue.offer(dcJsonArray);
    }

    private String packData(JSONArray dcJsonArray) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", this.dataType.getName());
        data.put("content", dcJsonArray);
        data.put("time", System.currentTimeMillis());
        return JSONObject.toJSONString(data);
    }

    @Override
    public void run() {
        BufferedWriter writer = null;
        while (true) {
            if (this.socket == null) {
                try {
                    this.socket = new Socket(this.host, this.port);
                    this.socket.setSoTimeout(30000);
                } catch (Exception e) {
                    log.error("create socket error: ", e);
                    try {
                        Thread.sleep(60000);
                    } catch (Exception e1) {
                    }
                    continue;
                }
            }

            if (writer == null) {
                try {
                    writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                } catch (Exception e2) {
                    log.error("create socket output error: ", e2);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e1) {
                    }
                    continue;
                }
            }

            if (blockingQueue.size() > this.maxSize) {
                try {
                    blockingQueue.clear();
                } catch (Exception e) {
                }
            }

            try {
                JSONArray jsonArray = blockingQueue.take();
                writer.write(HEADER);
                writer.flush();
                String pData = packData(jsonArray);
                writer.write(pData);
                writer.flush();
                writer.write(TAIL);
                writer.flush();
                log.info("[sendSocket]: TAIL");
            } catch (Exception e) {
                log.error("[sendSocket]: ", e);
                try {
                    this.socket.close();
                } catch (Exception e3) {
                }
                try {
                    Thread.sleep(60000);
                } catch (Exception e1) {
                }
                writer = null;
                this.socket = null;
            }
        }
    }
}
