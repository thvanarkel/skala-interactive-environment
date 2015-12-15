#include <Servo.h>

/*
 * JacobsLadder.cpp - Library for controlling JacobsLadder objects
 * in the SKALA prototype. Apart from including the JacobsLadder.h library
 * it is also required to include the Servo.h library
 * Written by Thomas van Arkel
 * Last updated: Dec 5, 2015
 * This work is licensed under a Creative Commons Attribution 4.0 International License
 */

#ifndef JacobsLadder_h
#define JacobsLadder_h

#include "Arduino.h"

enum LadderMovement
{
  None,
  Rustle,
  Cascade
};

class JacobsLadder {
  public:
    JacobsLadder(int pin);
    //Returns whether the movement has finished or not
    bool cascade();
    bool tease();
  private:
    int nextAngleToDestination();
    void reset();
    
    int _pin;
    int _angle;
    int _destinationAngle;
    Servo _servo;
    int _movementPhase = 0;
    long _lastUpdated = millis();
    int _updateDelay;

    const int MIN_PULSE = 500;
    const int MAX_PULSE = 2500;

};

#endif
