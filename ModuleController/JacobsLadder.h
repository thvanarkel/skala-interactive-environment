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
    bool rustle();
    bool cascade();
  private:
    bool stepTowardsDestination();
    
    int _pin;
    int _currentAngle;
    int _destinationAngle;
    Servo _servo;
    bool _didStart = false;
    int _movementPhase = 0;
    bool _movingClockwise = true;

    const int MIN_PULSE = 544;
    const int MAX_PULSE = 2400;

};

#endif
