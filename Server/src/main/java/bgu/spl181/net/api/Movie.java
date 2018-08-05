package bgu.spl181.net.api;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Movie {
    private int id;
    private String name;
    private int price;
    private ArrayList<String> bannedCountries;
    private AtomicInteger availableAmount;
    private int totalAmount;

    public Movie(int id, String name, int price, ArrayList<String> bannedCountries, AtomicInteger availableAmount, int totalAmount) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.bannedCountries = bannedCountries;
        this.availableAmount = availableAmount;
        this.totalAmount = totalAmount;
    }


    public int getId() { return id; }

    public String getName() { return name; }

    public AtomicInteger getAvailableAmount() { return availableAmount; }

    public int getPrice() { return price; }

    public void setPrice(int price){
        this.price = price;
    }

    public ArrayList<String> getBannedCountries() {return bannedCountries; }

    public void decreaseAvailability(){
        int val;
        do {
            val = availableAmount.get();
        }while (!availableAmount.compareAndSet(val, val - 1));

    }

    public void increaseAvailability(){
        int val;
        do {
            val = availableAmount.get();
        }while (!availableAmount.compareAndSet(val, val + 1));
    }
}
