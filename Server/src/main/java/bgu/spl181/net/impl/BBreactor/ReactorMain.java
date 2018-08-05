package bgu.spl181.net.impl.BBreactor;

import bgu.spl181.net.api.*;
import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.srv.Reactor;
import bgu.spl181.net.srv.Server;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactorMain {
    public static void main(String[] args){
        int port = Integer.parseInt(args[0]);
        System.out.println(port);
        String USERS_JSON_PATH = "DataBase/Users.json";
        String MOVIES_JSON_PATH = "DataBase/Movies.json";

        UsersData users;
        MoviesData movies;

        try (FileReader usersFileReader = new FileReader(USERS_JSON_PATH);
             FileReader moviesFileReader = new FileReader(MOVIES_JSON_PATH)) {
            users = new Gson().fromJson(usersFileReader, UsersData.class);
            movies = new Gson().fromJson(moviesFileReader, MoviesData.class);
        } catch (IOException e) {
            System.out.println("Didn't find json files");
            return;
        }
        TBProtocolSharedData TBProtoSharedData = new TBProtocolSharedData();
        BBProtocolSharedData BBProtoSharedData = new BBProtocolSharedData(users, movies);
        Server.<String>reactor(
                Runtime.getRuntime().availableProcessors(),port,
                ()->new BlockbusterProtocol(BBProtoSharedData, TBProtoSharedData),
                ()->new MessageEncoderDecoderImpl()).serve();



    }
}
