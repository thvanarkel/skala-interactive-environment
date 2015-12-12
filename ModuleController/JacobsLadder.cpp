/*
 * JacobsLadder.cpp - Library for controlling JacobsLadder objects
 * in the SKALA prototype
 * Written by Thomas van Arkel
 * Last updated: Dec 5, 2015
 * This work is licensed under a Creative Commons Attribution 4.0 International License
 */

#include "Arduino.h"
#include "JacobsLadder.h"

JacobsLadder::JacobsLadder(int pin)
{
  _servo.attach(_pin, MIN_PULSE, MAX_PULSE);
  _servo.write(0);
}

bool JacobsLadder::rustle() {
  if (!_didStart) {
    if (_currentAngle > 90) {
      _destinationAngle = 180;
    } else {

    }
  }
  if (_currentAngle < _destinationAngle) {
    _currentAngle += 5;
    _servo.write(_currentAngle);
  }
}


bool JacobsLadder::cascade() {
  if (!_didStart) {
    _movementPhase = 0;
    _didStart = true;
  } else {
    switch (_movementPhase) {
      case 0:
        if (_currentAngle > 90) {
          _destinationAngle = 0;
        } else {
          _destinationAngle = 180;
        }
        stepTowardsDestination();
        break;

      case 1:

        break;

      case 2:
        _didStart = false;
        return true;
        break;

      default:
        return false;
        break;
    }
  }
  return false;
}


bool JacobsLadder::stepTowardsDestination() {
  if (_destinationAngle > _currentAngle) {

  } else if (_destinationAngle < _currentAngle) {

  } else if (_destinationAngle == _currentAngle) {

  }
}

