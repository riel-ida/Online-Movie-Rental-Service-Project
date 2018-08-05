package bgu.spl181.net.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TBProtocolSharedData {
    private ConcurrentHashMap<String, Integer> loggedInUsers = new ConcurrentHashMap<>();
    final ReentrantReadWriteLock lockLoggedIn = new ReentrantReadWriteLock();

    public ConcurrentHashMap<String, Integer> getLoggedInUsers() {
        return loggedInUsers;
    }



}
