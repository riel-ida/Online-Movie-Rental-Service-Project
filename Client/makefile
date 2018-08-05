CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread

all: EchoClient
	g++ -o bin/BBclient bin/connectionHandler.o bin/echoClient.o bin/TaskOne.o bin/TaskTwo.o $(LDFLAGS)

EchoClient: bin/connectionHandler.o bin/echoClient.o bin/TaskOne.o bin/TaskTwo.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/TaskOne.o: src/TaskOne.cpp
	g++ $(CFLAGS) -o bin/TaskOne.o src/TaskOne.cpp

bin/TaskTwo.o: src/TaskTwo.cpp
	g++ $(CFLAGS) -o bin/TaskTwo.o src/TaskTwo.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
