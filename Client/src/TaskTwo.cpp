//
// Created by riel on 12/01/18.
//

#include "../include/TaskTwo.h"

TaskTwo::TaskTwo(ConnectionHandler* connectionHandler, boost::mutex* mutex,
                 boost::condition_variable * conditionVariable) :
        _connectionHandler(connectionHandler), _mutex(mutex),
        _conditionVariable(conditionVariable){}

void TaskTwo::run() {
    while (!_connectionHandler->shouldTerminate){
        std::string answer;
        if (!_connectionHandler->getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }

        unsigned long len = answer.length();
        answer.resize(len - 1);
        std::cout << answer << std::endl;

        if (answer.find("ACK signout succeeded") != std::string::npos) {
            _connectionHandler->shouldTerminate = true;
            declareMessage();
            break;
        }


        if(answer.find("BROADCAST") == std::string::npos){
            declareMessage();
        }
    }
};

void  TaskTwo::declareMessage(){
    boost::unique_lock<boost::mutex> lock(*_mutex);
    _conditionVariable->notify_one();
}