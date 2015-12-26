/*
 * JacobsLadder.cpp - Library for controlling JacobsLadder objects
 * in the SKALA prototype
 * Written by Thomas van Arkel
 * Last updated: Dec 26, 2015
 * This work is licensed under a Creative Commons Attribution 4.0 International License
 */

#include "Arduino.h"
#include "JacobsLadder.h"

void JacobsLadder::init(int pin)
{
  _servo.attach(_pin, MIN_PULSE, MAX_PULSE);
  _servo.write(0);
}

bool JacobsLadder::wait(int aDelay) {
  if (millis() - _lastUpdated > _updateDelay) {
    switch(_movementPhase) {
      case 0:
        _updateDelay = _aDelay;
        _movementPhase++;
        break;
      case 1:
        reset();
        return true;
    }
    _lastUpdated = millis();
  }
}

bool JacobsLadder::cascade() {
  bool finished = false;
  if (millis() - _lastUpdated > _updateDelay) {
    switch (_movementPhase) {
      case 0:
        _updateDelay = 50;
        if (_angle > 90) {
          _destinationAngle = 0;
        } else {
          _destinationAngle = 180;
        }
        _movementPhase++;
        finished = false;
        break;

      case 1:
        _servo.write(nextAngleToDestination());
        if (_angle == _destinationAngle) {
          reset();
          finished = true;
        } else {
          finished = false;
        }
        break;
    }
    _lastUpdated = millis();
  }
  return finished;
}

bool JacobsLadder::tease() {
  bool finished = false;
  if (millis() - _lastUpdated > _updateDelay) {
    switch (_movementPhase) {
      case 0:
        _updateDelay = 50;
        if (_angle > 90) {
          _destinationAngle = 180 - random(120, 140);
        } else {
          _destinationAngle = random(120, 140);
        }
        _movementPhase++;
        finished = false;
        break;

      case 1:
        _servo.write(nextAngleToDestination());
        if (_angle == _destinationAngle) {
          if (_destinationAngle >= 90) {
            _destinationAngle = 0; 
          } else {
            _destinationAngle = 180;
          }
          _movementPhase++;
        }
        finished = false;
        break;

      case 2:
        _servo.write(nextAngleToDestination());
        if (_angle == _destinationAngle) {
          reset();
          finished = true;
        } else {
          finished = false;
        }
        break;
    }
    _lastUpdated = millis();
  }
  return finished;
}

void JacobsLadder::reset() {
  _movementPhase = 0;
  _updateDelay = 50;
}

int JacobsLadder::nextAngleToDestination() {
  if (_destinationAngle > _angle) {
    _angle++;
  } else if (_destinationAngle < _angle) {
    _angle--;
  }
  return _angle;
}
