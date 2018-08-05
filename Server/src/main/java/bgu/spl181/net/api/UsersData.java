package bgu.spl181.net.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UsersData {
    private List<User> users = new ArrayList<>();

    public List<User> getUsers() {
        return users;
    }
}






