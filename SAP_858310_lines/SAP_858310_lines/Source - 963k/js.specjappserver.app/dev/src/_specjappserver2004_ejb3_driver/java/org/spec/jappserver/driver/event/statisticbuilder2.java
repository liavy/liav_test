package org.spec.jappserver.driver.event;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatisticBuilder2 extends Thread {

    private static final Logger logger = Logger.getLogger(StatisticBuilder2.class.getName());

    private static final StatisticBuilder2 instance = new StatisticBuilder2();

    public static final byte SEPARATOR = ' ';
    public static final char SEPARATOR_CHAR = (char) SEPARATOR;
    public static final byte LINE_FEED = 10;
    public static final char LINE_FEED_CHAR = (char) LINE_FEED;
    public static final byte ONCE_EVALUATED = 0;
    public static final char ONCE_EVALUATED_CHAR = (char) ONCE_EVALUATED;
    public static final String ONCE_EVALUATED_STR = "" + ONCE_EVALUATED;

    public static final int COLLECTOR_PORT = 8889;
    private static final String COLLECTOR_HOST_PROP_NAME = "bernstat.collector.host";
    static InetAddress sCollectorHost;
    static int sCollectorPort = COLLECTOR_PORT;

    private static DatagramSocket socket = null;

    public static final long FLUSH_AFTER_MILLIS = 2000;
    private static final int QUEUE_CAPACITY = 20;
    private static final ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(QUEUE_CAPACITY);
    private static final int MAX_MESSAGE_LENGTH = 1400;
    private final byte[] message = new byte[MAX_MESSAGE_LENGTH]; 
    private int pos = 0;
    private long lastFlushTime = 0;

    static {
        sCollectorHost = null;
        String collectorHost = System.getProperty(COLLECTOR_HOST_PROP_NAME);
        if (collectorHost != null) {
            try
            {
                sCollectorHost = InetAddress.getByName(collectorHost);
                socket = new DatagramSocket();
                instance.start();
            }
            catch (Exception e)
            {
                sCollectorHost = null;
                logger.log(Level.INFO, "", e);
            }
        }
    }

    static String getAppendString(String key, long time, String value)
    {
       if (key == null || value == null)
       {
          logger.log(Level.SEVERE, "Value is null: key=" + key + ", value=" + value);
          return null;
       }
       if (value.indexOf("\n") != -1 || value.indexOf("\r") != -1)
       {
          logger.log(Level.SEVERE, "Value of key=" + key + " cannot be sent since it contains \\n or \\r: " + value);
          return null;
       }

       StringBuilder line = new StringBuilder(key.length() + value.length() + 20);
       line.append(key);
       line.append(SEPARATOR_CHAR);
       line.append(time); // TODO: add as bytes, not as string!!!
       line.append(SEPARATOR_CHAR);
       line.append(value);
       line.append(LINE_FEED_CHAR);

       if (line.length() > MAX_MESSAGE_LENGTH)
       {
          logger.log(Level.SEVERE, "Message with key=" + key + ", value = " + value + "is too long");
          return null;
       }
       
       return line.toString();
    }

    public static void send(String key, long value) {
        send(key, System.currentTimeMillis(), Long.toString(value));
    }
    
    public static void send(String key, int value) {
        send(key, System.currentTimeMillis(), Integer.toString(value));
    }
    
    public static void send(String key, String value) {
        send(key, System.currentTimeMillis(), value);
    }
    
    public static void send(String key, long time, String value)
    {
       if (sCollectorHost == null) {
           return;
       }

       if (key == null || value == null)
       {
          logger.log(Level.SEVERE, "Value is null: key=" + key + ", value=" + value);
          return;
       }
       if (value.indexOf("\n") != -1 || value.indexOf("\r") != -1)
       {
          logger.log(Level.SEVERE, "Value of key=" + key + " cannot be sent since it contains \\n or \\r: " + value);
          return;
       }

       byte[] line = new byte[key.length() + value.length() + 11];
       int pos = 0;
       for (int i = 0; i < key.length(); i++) {
           line[pos++] = (byte)key.charAt(i);
       }

       line[pos++] = SEPARATOR_CHAR;
       line[pos++] = (byte)(time >>> 56);
       line[pos++] = (byte)(time >>> 48);
       line[pos++] = (byte)(time >>> 40);
       line[pos++] = (byte)(time >>> 32);
       line[pos++] = (byte)(time >>> 24);
       line[pos++] = (byte)(time >>> 16);
       line[pos++] = (byte)(time >>>  8);
       line[pos++] = (byte)(time >>>  0);
       line[pos++] = SEPARATOR_CHAR;
       for (int i = 0; i < value.length(); i++) {
           line[pos++] = (byte)value.charAt(i);
       }
       line[pos++] = LINE_FEED_CHAR;
       try {
            queue.put(line);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private StatisticBuilder2() {}

    @Override
    public void run() {
        byte[] line;
        while (true) {
            try {
                line = queue.poll(FLUSH_AFTER_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                line = null;
            }
            long curTime = System.currentTimeMillis();
            if (pos > 0
                    && (lastFlushTime < curTime - FLUSH_AFTER_MILLIS
                    || (line != null && pos + line.length > MAX_MESSAGE_LENGTH))) {
                send();
                logger.log(Level.FINEST, "sending since time condition="
                        + (lastFlushTime < curTime - FLUSH_AFTER_MILLIS) + " lengthcondition="
                        + (line == null ? "" : (pos + line.length > MAX_MESSAGE_LENGTH)));
                lastFlushTime = curTime;
                pos = 0;
            }
            if (line != null) {
                System.arraycopy(line, 0, message, pos, line.length);
                pos += line.length;
            }
        }
    }

    private void send() {
        try {
            // Always reconnect to remote host/port!
            // Otherwise if first bind failed then all subsequent calls fail
            DatagramPacket messagePacket = new DatagramPacket(message, pos, sCollectorHost, sCollectorPort);
            socket.send(messagePacket);
            logger.finest("message.length=" + pos);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unexpected exception during sending UDP packet", e);
        }
    }
    
    public static void main(String[] args) {
        for (int i = 0; i < 10000000; i++) {
            send("StatisticName", "" + i);
            System.out.println("written " + i);
            if (i % 10 == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
