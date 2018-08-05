//
// Created by riel on 12/01/18.
//

#ifndef CLIENT_TASKONE_H
#define CLIENT_TASKONE_H

#include <boost/thread.hpp>
#include <connectionHandler.h>

class TaskOne {

private:
    ConnectionHandler*  _connectionHandler;
    boost::mutex* _mutex;
    boost::condition_variable * _conditionVariable;
public:
    TaskOne(ConnectionHandler* connectionHandler, boost::mutex* mutex,
            boost::condition_variable * conditionVariable);
    void run();
    void waitForMessage();
};


#endif //CLIENT_TASKONE_H
