/*
 *
 */

#ifndef JacobsLadder_h
#define JacobsLadder_h

#include "Arduino.h"

class JacobsLadder {
  public:
    JacobsLadder(int pin);
    void setAngle(int angle);
  private:
    int _pin;
    int _angle;
    Servo _servo;
}
