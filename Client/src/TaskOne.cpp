//
// Created by riel on 12/01/18.
//

#include "../include/TaskOne.h"

TaskOne::TaskOne(ConnectionHandler* connectionHandler, boost::mutex* mutex,
                 boost::condition_variable * conditionVariable) :
        _connectionHandler(connectionHandler), _mutex(mutex),
        _conditionVariable(conditionVariable){}

void TaskOne::run(){
    while (!_connectionHandler->shouldTerminate && (!std::cin.eof())) {
        const short bufSize = 1024;
        char buf[bufSize];

        std::cin.getline(buf, bufSize);
        std::string line(buf);

        if (!_connectionHandler->sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }

        waitForMessage();
    }
};

void TaskOne::waitForMessage(){
    boost::unique_lock<boost::mutex> lock(*_mutex);
    _conditionVariable->wait(lock);
}