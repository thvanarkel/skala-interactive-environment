#include <Servo.h>
#include <QueueList.h>

/*
 * JacobsLadder.cpp - Library for controlling JacobsLadder objects
 * in the SKALA prototype. Apart from including the JacobsLadder.h library
 * it is also required to include the Servo.h library
 * Written by Thomas van Arkel
 * Last updated: Dec 26, 2015
 * This work is licensed under a Creative Commons Attribution 4.0 International License
 */

#ifndef JacobsLadder_h
#define JacobsLadder_h

#include "Arduino.h"

enum MovementType
{
  Buzz = 0,
  Tease = 1,
  Cascade = 2,
};

struct Movement 
{
  MovementType type;
  int destinationAngle;
  int updateDelay;
};

class JacobsLadder {
  public:
    // Initialises the servo by attaching to the pin parameter
    void init(int pin, int minPulse, int maxPulse);

    void addMovement(MovementType type, int velocity);

    // Updates the position of the ladder when movements are in queue. 
    void updateLadder();
    
    // Pauses all movements currently in the queue. A timeout can be specified, continuing movement
    // after the timeout elapsed. If no timeout is specified, the system can be unpaused by calling
    // the function again. Returns whether the system is paused or not (true if paused, false if not).
    bool pause(int timeout);
    
  private:
    Servo servo;
    QueueList <struct Movement> queue;
    int _angle;
    long _lastUpdated = millis();

    void cascade();
    void cascade(int velocity);
    
    void buzz();
    void buzz(int velocity);

    bool hasPriority(MovementType type);
    void emptyQueue();
    void resetPosition(MovementType type);
    void resetPosition(MovementType type, int velocity);
    int nextAngleToDestination(int destinationAngle);
    int getFinalDestinationAngle();
};

#endif
