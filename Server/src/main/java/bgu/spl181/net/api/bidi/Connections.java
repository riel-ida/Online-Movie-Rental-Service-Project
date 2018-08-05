package bgu.spl181.net.api.bidi;

import bgu.spl181.net.srv.ConnectionHandler;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);

    void add(Integer connectionId, ConnectionHandler handler);

}
