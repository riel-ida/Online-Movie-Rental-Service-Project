#include <stdlib.h>
#include <iostream>
#include <connectionHandler.h>
#include "../include/TaskOne.h"
#include "../include/TaskTwo.h"

class thread;

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/

int main (int argc, char *argv[]) {
    boost::mutex mutex;
    boost::condition_variable conditionVar;

    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    ConnectionHandler connectionHandler(host, port);

    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    TaskOne task1(&connectionHandler, &mutex, &conditionVar);
    TaskTwo task2(&connectionHandler, &mutex, &conditionVar);

    boost::thread th1(&TaskOne::run, &task1);
    boost::thread th2(&TaskTwo::run, &task2);
    th1.join();
    th2.join();
    
    return 0;

};