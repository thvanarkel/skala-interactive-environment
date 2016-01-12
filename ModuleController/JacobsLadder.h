#include <Servo.h>
#include <QueueList.h>

/*
 * JacobsLadder.cpp - Library for controlling JacobsLadder objects
 * in the SKALA prototype.
 * Written by Thomas van Arkel and Mo de Ruiter
 * Last updated: Jan 12, 2016
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
  byte destinationAngle;
  int updateDelay;
  float ratio;
};

class JacobsLadder {
  public:
    bool isPaused;
    
    // Initialises the servo by attaching to the pin parameter
    void init(int pin, int minPulse, int maxPulse);

    // Adds a movement with a specified velocity. If a movement wit a higher priority than the
    // current  running movement is added, lower priority movements are removed. 
    // If a movement with a lower priority than the running movement is added the movement is ignored.
    void addMovement(MovementType type, int velocity);

    // Updates the position of the ladder when movements are in queue. 
    void updateLadder();
    
    // Pauses all movements currently in the queue.
    void pause();
    
  private:
    Servo servo;
    QueueList <struct Movement> queue;
    byte _angle;
    unsigned long _lastUpdated = millis();

    void cascade(int velocity);
    
    void buzz(int velocity);

    bool hasPriority(MovementType type);
    void emptyQueue();
    void resetPosition(MovementType type);
    void resetPosition(MovementType type, int velocity);
    byte nextAngleToDestination(byte destinationAngle, byte increment);
    byte getFinalDestinationAngle();
    int calculateUpdateDelay(int velocity, byte destinationAngle, byte startingAngle);
    byte incrementForRatio(float ratio);
};

#endif
