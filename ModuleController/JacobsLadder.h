#include <Servo.h>

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
  Wait = 0,
  Cascade = 1,
  Tease = 2,
  Buzz = 3
};

class JacobsLadder {
  public:
    void init(int pin);
    //Returns whether the movement has finished or not
    void performMovement(MovementType type);
    bool finished = true;
  private:
    void cascade();
    void tease();
    bool wait(int aDelay);
    bool buzz();
    int nextAngleToDestination(int increment);
    void reset();
    int _angle;
    int _destinationAngle;
    Servo _servo;
    int _movementPhase = 0;
    unsigned long _lastUpdated = millis();
    int _updateDelay;

    //const int MIN_PULSE = 500;
    //const int MAX_PULSE = 2500;
};

#endif
