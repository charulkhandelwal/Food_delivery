package com.example.food_delivery.Socket;

import android.util.Log;
import org.json.JSONObject;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {
    private static SocketManager instance;
    private Socket socket;
    private final String SOCKET_URL = "http://164.52.197.192:5678";

    private SocketManager() {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.forceNew = true;
            socket = IO.socket(SOCKET_URL, options);

            // 🔹 Connected Event
            socket.on(Socket.EVENT_CONNECT, args ->
                    Log.d("Socket", "✅ Connected to server"));

            // 🔹 Error Event
            socket.on(Socket.EVENT_CONNECT_ERROR, args ->
                    Log.e("Socket", "❌ Connection error: " + args[0]));

            // 🔹 Disconnected Event
            socket.on(Socket.EVENT_DISCONNECT, args ->
                    Log.d("Socket", "⚠️ Disconnected from server"));

            // 🔹 NEW ORDER Event Listener (Important Part)
            socket.on("new_order", args -> {
                try {
                    if (args.length > 0) {
                        JSONObject orderData = (JSONObject) args[0];
                        Log.d("SocketEvent", "🆕 New order received: " + orderData.toString());
                    } else {
                        Log.w("SocketEvent", "⚠️ new_order event received with no data");
                    }
                } catch (Exception e) {
                    Log.e("SocketEvent", "❌ Error parsing new_order: " + e.getMessage());
                }
            });

        } catch (URISyntaxException e) {
            Log.e("Socket", "Invalid URL: " + e.getMessage());
        }
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void connect() {
        if (socket != null && !socket.connected()) {
            socket.connect();
            Log.d("Socket", "🔄 Connecting to socket...");
        }
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
            Log.d("Socket", "🔌 Socket disconnected");
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
