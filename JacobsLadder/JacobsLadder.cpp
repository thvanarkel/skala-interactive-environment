/*
 * JacobsLadder.cpp - Library for controlling JacobsLadder objects
 * in the SKALA prototype.
 * Written by Thomas van Arkel.
 * Last updated: Dec 5, 2015.
 */

#include "Arduino.h"
#include "JacobsLadder.h"

JacobsLadder::JacobsLadder(int pin)
{
  _pin = pin;
  _servo.attach(_pin);
}

void JacobsLadder::setAngle(int angle)
{
  _angle = angle;
  _servo.write(_angle);
}
