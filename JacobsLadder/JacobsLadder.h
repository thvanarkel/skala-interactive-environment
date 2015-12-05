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

class JacobsLadder {
  public:
    JacobsLadder(int pin);
    void setAngle(int angle);
  private:
    int _pin;
    int _angle;
    Servo _servo;
}
