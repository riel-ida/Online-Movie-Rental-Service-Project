package bgu.spl181.net.api;

public class MovieDetails {
    private int id;
    private String name;

    public MovieDetails(int id, String name){
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
