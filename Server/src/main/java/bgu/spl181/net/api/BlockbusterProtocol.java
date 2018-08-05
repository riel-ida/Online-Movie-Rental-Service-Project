package bgu.spl181.net.api;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockbusterProtocol extends TextBasedProtocol {
    private BBProtocolSharedData sharedData;

    public BlockbusterProtocol(BBProtocolSharedData sharedData, TBProtocolSharedData taxedBasedSharedData) {
        super(taxedBasedSharedData);
        this.sharedData = sharedData;
    }

    @Override
    public boolean checkIfCanRegister(String userName, String dataBlock) {
        if (sharedData.searchUserByName(userName)==null && dataBlock.contains("country=")) {
            return true;
        }
        return false;
    }

    @Override
    public void registerUser(String userName, String password, String country) {
        sharedData.lockUsers.writeLock().lock();
        User newUser = new User(userName, "normal", password,
                country, new ArrayList<MovieDetails>(), 0);
        sharedData.addUser(newUser);
        String msg = new String("ACK registration succeeded");
        connections.send(this.connectionId, msg);
        sharedData.writeUsersDataToJson();
        sharedData.lockUsers.writeLock().unlock();
        return;
    }

    @Override
    public boolean checkIfCanLogin(String userName, String password) {
        if (!(sharedData.searchUserByName(userName)==null) &&
                sharedData.searchUserByName(userName).getPassword().equals(password)) {
            return true;
        }
        return false;
    }

    @Override
    public void tryToFulfillRequest(String[] message, String userName) {
        String requestName = message[1];
        String movieName = "";
        User user = sharedData.searchUserByName(userName);

        if (requestName.contains("balance")) {
            handleBalance(message, userName);
            return;
        } else if (requestName.contains("info")) {
            movieName = getMovieName(message);
            handleInfo(movieName);
            return;
        } else if (requestName.contains("rent")) {
            movieName = getMovieName(message);
            handleRent(movieName, userName);
            return;
        } else if (requestName.contains("return")) {
            movieName = getMovieName(message);
            handleReturn(movieName, userName);
            return;
            //admin commands
        } else if (requestName.contains("addmovie")) {
            if (user.getType().equalsIgnoreCase("admin")) {
                movieName = getMovieName(message);
                handleAddMovie(message, movieName);
                return;
            } else {
                String msg = new String("Error request addmovie failed");
                connections.send(this.connectionId, msg);
                return;
            }
        } else if (requestName.contains("remmovie")) {
            if (user.getType().equalsIgnoreCase("admin")) {
                movieName = getMovieName(message);
                handleRemoveMovie(movieName);
                return;

            } else {
                String msg = new String("Error request remmovie failed");
                connections.send(this.connectionId, msg);
                return;

            }
        } else if (requestName.contains("changeprice")) {
            if (user.getType().equalsIgnoreCase("admin")) {
                movieName = getMovieName(message);
                handleChangePrice(message, movieName);
                return;

            } else {
                String msg = new String("Error request changeprice failed");
                connections.send(this.connectionId, msg);
                return;

            }
        }

    }


    private String getMovieName(String[] message) {
        String movieName = "";
        int startingIndex = getFirstMovieNameIndex(message);
        int lastIndex = getLastMovieNameIndex(message, startingIndex);
        if (startingIndex != -1 && lastIndex != -1) {
            for (int i = startingIndex; i <= lastIndex -1; i++) { //merge the array into one Movie Name String
                movieName = movieName + message[i] + " ";
            }
            movieName = movieName + message[lastIndex];
            return movieName.substring(1, movieName.length() - 1); //a name without quotation marks
        }
        return null;
    }

    public int getFirstMovieNameIndex(String[] message) { // finds the first index of the MovieName
        int startingIndex = -1;
        for (int i = 0; startingIndex == -1 && i < message.length; i++) {
            if (message[i].charAt(0) == '"')
                startingIndex = i;
        }
        return startingIndex;
    }

    public int getLastMovieNameIndex(String[] message, int startingIndex) { // finds the last index of the Movie Name
        int lastIndex = -1;
        if (startingIndex != -1) {
            for (int i = startingIndex; lastIndex == -1 && i < message.length; i++) {
                if (message[i].charAt(message[i].length() - 1) == '"')
                    lastIndex = i;
            }
        }
        return lastIndex;
    }

    public ArrayList<String> getBannedCountries(String[] message, int index) {
        int firstCountryIndex = index + 3;
        ArrayList<String> bannedCountries = new ArrayList<>();
        for (int i = firstCountryIndex; i < message.length; i++) {
            String s = message[i].substring(1, message[i].length()-1);
            bannedCountries.add(s);
        }
        return bannedCountries;
    }



    private void handleBalance(String[] message, String userName) {// handles BALANCE request
        if(message[2].equalsIgnoreCase("info")) {
            sharedData.lockUsers.readLock().lock();
            String msg = new String("ACK balance " + sharedData.searchUserByName(userName).getBalance());
            connections.send(this.connectionId, msg);
            sharedData.lockUsers.readLock().unlock();
            return;
        }
        if(message[2].equalsIgnoreCase("add")){
            sharedData.lockUsers.writeLock().lock();
            int amount = Integer.parseInt(message[3]);// TODO CHECK
            sharedData.searchUserByName(userName).setBalanceUp(amount);
            String msg = new String("ACK balance " + sharedData.searchUserByName(userName).getBalance()
                    + " added " + amount);
            connections.send(this.connectionId, msg);
            sharedData.writeUsersDataToJson();
            sharedData.lockUsers.writeLock().unlock();
            return;
        }
    }


    private void handleInfo(String movieName) {
        sharedData.lockMovies.readLock().lock();
        if (movieName==null) { // no movie name was given
            String msg = new String("ACK info " + sharedData.getAllMovies().substring(1));
            connections.send(this.connectionId, msg);
            sharedData.lockMovies.readLock().unlock();
            return;
        } else if (sharedData.searchMovieByName(movieName)==null) {
            String msg = new String("Error request info failed");
            connections.send(this.connectionId, msg);
            sharedData.lockMovies.readLock().unlock();
            return;
        } else {
            Movie movie = sharedData.searchMovieByName(movieName);
            String bannedCountries = "";
            for (String s : movie.getBannedCountries())
                bannedCountries = bannedCountries + " " + '"' + s + '"';
            String msg = new String("ACK info " + '"' + movieName + '"' + " "
                    + movie.getAvailableAmount() + " " + movie.getPrice() + bannedCountries);
            connections.send(this.connectionId, msg);
            sharedData.lockMovies.readLock().unlock();
            return;

        }
    }

    public void handleRent(String movieName, String userName) {
        sharedData.lockUsers.writeLock().lock();
        sharedData.lockMovies.writeLock().lock();
        if (movieName==null || sharedData.searchMovieByName(movieName)==null) {
            String msg = new String("Error request rent failed");
            connections.send(this.connectionId, msg);
            sharedData.lockUsers.writeLock().unlock();
            sharedData.lockMovies.writeLock().unlock();
            return;
        } else {
            User user = sharedData.searchUserByName(userName);
            Movie movie = sharedData.searchMovieByName(movieName);
            if (user.isRentedByThis(movieName) || user.getBalance() < movie.getPrice()
                    || movie.getAvailableAmount().get()==0 || movie.getBannedCountries().contains(user.getCountry())) {
                String msg = new String("Error request rent failed");
                connections.send(this.connectionId, msg);
                sharedData.lockUsers.writeLock().unlock();
                sharedData.lockMovies.writeLock().unlock();
                return;
            }
            else{
                movie.decreaseAvailability(); // decreases the available copies of the movie
                sharedData.addMovieToUser(user, movie); // add the movie the user's movies list
                user.setBalanceDown(movie.getPrice()); // sets the user's balance according to the movie's price
                String msg = new String("ACK rent " + '"' + movieName + '"' + " success");
                connections.send(this.connectionId, msg);
                String broadMsg = new String("BROADCAST movie " + '"' + movieName + '"'
                        + " " + movie.getAvailableAmount() + " " + movie.getPrice());
                sendToAll(broadMsg);
                sharedData.writeUsersDataToJson();
                sharedData.writeMoviesDataToJson();
                sharedData.lockUsers.writeLock().unlock();
                sharedData.lockMovies.writeLock().unlock();
                return;
            }

        }
    }


    public void handleReturn(String movieName, String userName) {
        sharedData.lockUsers.writeLock().lock();
        sharedData.lockMovies.writeLock().lock();
        if (movieName==null || sharedData.searchMovieByName(movieName)==null) {
            String msg = new String("Error request return failed");
            connections.send(this.connectionId, msg);
            sharedData.lockUsers.writeLock().unlock();
            sharedData.lockMovies.writeLock().unlock();
            return;
        } else {
            User user = sharedData.searchUserByName(userName);
            Movie movie = sharedData.searchMovieByName(movieName);
            if (!(user.isRentedByThis(movieName))) {
                String msg = new String("Error request return failed");
                connections.send(this.connectionId, msg);
                sharedData.lockUsers.writeLock().unlock();
                sharedData.lockMovies.writeLock().unlock();
                return;
            } else {
                movie.increaseAvailability(); // increase the available copies of the movie
                sharedData.removeMovieFromUser(user, movieName); // removes the movie from the user's movies list
                String msg = new String("ACK return " + '"' + movieName + '"' + " success");
                connections.send(this.connectionId, msg);
                String broadMsg = new String("BROADCAST movie " + '"' + movieName + '"' + " " +
                        movie.getAvailableAmount() + " " + movie.getPrice());
                sendToAll(broadMsg);
                sharedData.writeUsersDataToJson();
                sharedData.writeMoviesDataToJson();
                sharedData.lockUsers.writeLock().unlock();
                sharedData.lockMovies.writeLock().unlock();
                return;
            }
        }
    }


    public void handleAddMovie(String[] message, String movieName) {
        sharedData.lockMovies.writeLock().lock();
        int startingIndex = getLastMovieNameIndex(message, getFirstMovieNameIndex(message));
        ArrayList<String> bannedCountries = getBannedCountries(message, startingIndex); // represents the banned countries of the movie
        int amount = Integer.parseInt(message[startingIndex + 1]);
        int price = Integer.parseInt(message[startingIndex + 2]);
        AtomicInteger availableAmount = new AtomicInteger(amount);
        if (!(sharedData.searchMovieByName(movieName)==null) || amount <= 0 || price <= 0){
            String msg = new String("Error request addmovie failed");
            connections.send(this.connectionId, msg);
            sharedData.lockMovies.writeLock().unlock();
            return;
        }
        else {
            Movie movieToAdd = new Movie(sharedData.getHighestID(), movieName,
                    price, bannedCountries, availableAmount, amount); // creating a new movie to be added
            sharedData.addMovie(movieToAdd);
            String msg = new String("ACK addmovie " + '"' + movieName + '"' + " success");
            connections.send(this.connectionId, msg);
            String broadMsg = new String("BROADCAST movie " + '"' + movieName + '"'
                    + " " + movieToAdd.getAvailableAmount() + " " + movieToAdd.getPrice());
            sendToAll(broadMsg);
            sharedData.writeMoviesDataToJson();
            sharedData.lockMovies.writeLock().unlock();
            return;
        }
    }



    public void handleRemoveMovie(String movieName) {
        sharedData.lockUsers.readLock().lock();
        sharedData.lockMovies.writeLock().lock();
        if(sharedData.searchMovieByName(movieName)==null || sharedData.checkIfRented(movieName)) {
            String msg = new String("Error request remmovie failed");
            connections.send(this.connectionId, msg);
            sharedData.lockUsers.readLock().unlock();
            sharedData.lockMovies.writeLock().unlock();
            return;
        }
        else{
            sharedData.removeMovie(sharedData.searchMovieByName(movieName));
            String msg = new String("ACK remmovie " + '"' + movieName + '"' + " success");
            connections.send(this.connectionId, msg);
            String broadMsg = new String("BROADCAST movie " + '"' + movieName + '"' + " remuved");
            sendToAll(broadMsg);
            sharedData.writeMoviesDataToJson();
            sharedData.lockUsers.readLock().unlock();
            sharedData.lockMovies.writeLock().unlock();
            return;
            }
        }


    public void handleChangePrice(String[]message, String movieName){
        sharedData.lockMovies.writeLock().lock();//TODO CHECK
        int price = Integer.parseInt(message[message.length-1]);
        if(sharedData.searchMovieByName(movieName)==null || price<=0){
            String msg = new String("Error request changeprice failed");
            connections.send(this.connectionId, msg);
            sharedData.lockMovies.writeLock().unlock();
            return;
        }
        else{
            Movie movie = sharedData.searchMovieByName(movieName);
            movie.setPrice(price); // sets the new price
            String msg = new String("ACK changeprice " + '"' + movieName + '"' + " success");
            connections.send(this.connectionId, msg);
            String broadMsg = new String("BROADCAST movie " + '"' + movieName + '"'
                    + " " + movie.getAvailableAmount() + " " + movie.getPrice());
            sendToAll(broadMsg);
            sharedData.writeMoviesDataToJson();
            sharedData.lockMovies.writeLock().unlock();
            return;
        }
    }
}



