//
// Created by riel on 12/01/18.
//

#ifndef CLIENT_TASKTWO_H
#define CLIENT_TASKTWO_H

#include <boost/thread.hpp>
#include <connectionHandler.h>

class TaskTwo {

private:
    ConnectionHandler*  _connectionHandler;
    boost::mutex* _mutex;
    boost::condition_variable * _conditionVariable;
public:
    TaskTwo(ConnectionHandler* connectionHandler, boost::mutex* mutex,
            boost::condition_variable * conditionVariable);
    void run();
    void declareMessage();
};


#endif //CLIENT_TASKTWO_H
