package com.example.food_delivery.Socket;

import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {

    private static SocketManager instance;
    private Socket socket;
    private final String SOCKET_URL = "http://164.52.197.192:5678";
    private static final String TAG = "SocketManager";

    private SocketManager() {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.forceNew = true;

            socket = IO.socket(SOCKET_URL, options);

            // ✅ Attach all listeners BEFORE connect
            socket.on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "✅ Connected to server"));
            socket.on(Socket.EVENT_DISCONNECT, args -> Log.d(TAG, "⚠️ Disconnected from server"));
            socket.on(Socket.EVENT_CONNECT_ERROR, args ->
                    Log.e(TAG, "❌ Connect error: " + (args.length > 0 ? args[0].toString() : "null")));

            socket.on("new_order", args -> {
                if (args.length > 0 && args[0] != null)
                    Log.d(TAG, "🆕 New order received: " + args[0].toString());
                else
                    Log.w(TAG, "⚠️ new_order event with no data");
            });

        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid URL: " + e.getMessage());
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
            Log.d(TAG, "🔄 Connecting to socket...");
            socket.connect();
        }
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
            Log.d(TAG, "🔌 Socket disconnected");
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
