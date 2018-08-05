package bgu.spl181.net.api.bidi;

import bgu.spl181.net.srv.ConnectionHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, ConnectionHandler> clients = new ConcurrentHashMap<>();

    @Override
    public boolean send(int connectionId, T msg) {
        if (clients.containsKey(connectionId)) {
            clients.get(connectionId).send(msg);
            return true;
        }
        return false;
    }


    @Override
    public void broadcast(T msg) {
        for (Map.Entry<Integer, ConnectionHandler> entry : clients.entrySet()) {
            clients.get(entry).send(msg);
        }
    }


    @Override
    public void disconnect(int connectionId) {

            clients.remove(connectionId);
    }

    public void add(Integer connectionId, ConnectionHandler handler) {
        if (!(clients.containsKey(connectionId)))
            clients.put(connectionId, handler);
    }

}

