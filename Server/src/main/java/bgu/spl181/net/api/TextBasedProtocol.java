package bgu.spl181.net.api;

import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.api.bidi.ConnectionsImpl;

import java.util.Map;

public abstract class TextBasedProtocol implements BidiMessagingProtocol<String> {
    protected Connections<String> connections;
    protected int connectionId;
    private boolean shouldTerminate = false;
    private boolean isLoggedIn = false;
    protected String userNameLoggedIn;
    private TBProtocolSharedData sharedData;

    public TextBasedProtocol(TBProtocolSharedData sharedData) {
        this.sharedData = sharedData;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connections = (ConnectionsImpl) connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(String message) {
        this.shouldTerminate = message.contains("SIGNOUT");
        if (message.contains("REGISTER"))
            handleRegister(message);
        else if (message.contains("LOGIN"))
            handleLogin(message);
        else if (message.contains("SIGNOUT"))
            handleSignout();
        else if (message.contains("REQUEST"))
            handleRequest(message);

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public void sendToAll(String msg){
        sharedData.lockLoggedIn.readLock().lock();
        for(Map.Entry<String, Integer> entry : sharedData.getLoggedInUsers().entrySet()) {
            connections.send(entry.getValue(), msg);
        }
        sharedData.lockLoggedIn.readLock().unlock();
    }

    private void handleRegister(String message) {
        String[] messageParameters = message.split(" ");
        String userName = messageParameters[1]; //the first word (messageParameters[0]) is "REGISTER"
        String userPassword = messageParameters[2];
        String dataBlock = messageParameters[3];
        if (messageParameters.length < 4) { //missing info
            String msg = new String("ERROR registration failed");
            connections.send(this.connectionId, msg);
            return;
        } else if (isLoggedIn) {
            String msg = new String("ERROR registration failed");
            connections.send(this.connectionId, msg);
            return;
        } else if (!checkIfCanRegister(userName, dataBlock)) {
            String msg = new String("ERROR registration failed");
            connections.send(this.connectionId, msg);
            return;
        } else{
            String country = dataBlock.substring(9, dataBlock.length()-1);
            registerUser(userName, userPassword, country);
            return;
        }


    }


    private void handleLogin (String message){
        sharedData.lockLoggedIn.writeLock().lock();
        String[] messageParameters = message.split(" ");
        String userName = messageParameters[1]; //the first word is "LOGIN"
        String userPassword = messageParameters[2];
        if (isLoggedIn) {
            String msg = new String("ERROR login failed");
            connections.send(this.connectionId, msg);
            sharedData.lockLoggedIn.writeLock().unlock();
            return;
        } for (Map.Entry<String, Integer> entry : sharedData.getLoggedInUsers().entrySet()) {
                if(entry.getKey().equalsIgnoreCase(userName)) {
                    String msg = new String("ERROR login failed");
                    connections.send(this.connectionId, msg);
                    sharedData.lockLoggedIn.writeLock().unlock();
                    return;
                }
        } if (!this.checkIfCanLogin(userName, userPassword)) {
            String msg = new String("ERROR login failed");
            connections.send(this.connectionId, msg);
            sharedData.lockLoggedIn.writeLock().unlock();
            return;
        } else {
            this.isLoggedIn = true;
            this.userNameLoggedIn = userName;
            sharedData.getLoggedInUsers().putIfAbsent(userName, connectionId);
            String msg = new String("ACK login succeeded");
            connections.send(this.connectionId, msg);
            sharedData.lockLoggedIn.writeLock().unlock();
            return;
        }
    }

    private void handleSignout(){
        if (!isLoggedIn) {
            String msg = new String("ERROR signout failed");
            connections.send(this.connectionId, msg);
            return;
        } else {
            if(shouldTerminate) {
                sharedData.lockLoggedIn.writeLock().lock();
                sharedData.getLoggedInUsers().remove(this.userNameLoggedIn);
                this.isLoggedIn = false;
                String msg = new String("ACK signout succeeded");
                connections.send(this.connectionId, msg);
                connections.disconnect(connectionId);
                sharedData.lockLoggedIn.writeLock().unlock();
                return;
            }
        }
    }

    private void handleRequest (String message) {
        String[] messageParameters = message.split(" ");
        String requestName = messageParameters[1];
        if(!isLoggedIn){
            String msg = new String("Error request " + requestName  + " failed");
            connections.send(this.connectionId, msg);
        }
        else {
            tryToFulfillRequest(messageParameters,userNameLoggedIn);
            return;
        }

    }
    public abstract boolean checkIfCanRegister (String userName, String dataBlock);

    public abstract void registerUser (String userName, String password, String country);

    public abstract boolean checkIfCanLogin(String userName, String password);

    public abstract void tryToFulfillRequest(String[] message, String userName);



}
