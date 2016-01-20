/*
 * JacobsLadder.cpp - Library for controlling JacobsLadder objects
 * in the SKALA prototype
 * Written by Thomas van Arkel and Mo de Ruiter
 * Last updated: Jan 12, 2016
 * This work is licensed under a Creative Commons Attribution 4.0 International License
 */

#include "Arduino.h"
#include "JacobsLadder.h"

void JacobsLadder::init(int pin, int minPulse, int maxPulse)
{
  servo.attach(pin, minPulse, maxPulse);
  servo.write(0);
  _angle = 0;
  isPaused = false;
}

void JacobsLadder::addMovement(MovementType type, int velocity) {

  Serial.println(queue.count());
  if(queue.count() > 15) {
    return;
  }
  
  switch (type) {
    case Cascade:
      cascade(velocity);
      break;

    case Buzz:
      buzz(velocity);
      break;

    default:
      break;
  }
}

void JacobsLadder::updateLadder() {
  if (isPaused) {
    return;
  }
  
  if (queue.isEmpty()) {
    return;
  }
  
  Movement movement = queue.peek();
  if (millis() - _lastUpdated < incrementForRatio(movement.ratio) * movement.updateDelay) {
    return;
  }
  _angle = nextAngleToDestination(movement.destinationAngle, incrementForRatio(movement.ratio));
  servo.write(_angle);
  if (_angle == movement.destinationAngle) {
    queue.pop();
  }
  _lastUpdated = millis();
}

void JacobsLadder::pause() {
  if (isPaused) {
    isPaused = false;
  } else {
    isPaused = true;
  }
}

void JacobsLadder::cascade(int velocity) {
  if (hasPriority(Cascade)) {
    struct Movement movement;
    movement.type = Cascade;
    movement.destinationAngle = 0;
    byte startingAngle = getFinalDestinationAngle();
    if (startingAngle < 90) {
      movement.destinationAngle = 180;
    }   
    movement.updateDelay = calculateUpdateDelay(velocity, movement.destinationAngle, startingAngle);
    movement.ratio = (float) movement.updateDelay / (int) (abs(movement.destinationAngle - startingAngle) != 0 ? abs(movement.destinationAngle - startingAngle) : 1);
    
    queue.push(movement);
  }
}

void JacobsLadder::buzz(int velocity) {
  const byte buzzAngle = 30;
  if (hasPriority(Buzz)) {
    resetPosition(Buzz, velocity);

    struct Movement movement;
    movement.type = Buzz;
    movement.destinationAngle = buzzAngle;
    byte startingAngle = getFinalDestinationAngle();
    if (startingAngle > 90) {
      movement.destinationAngle = 180 - buzzAngle;
    }
    movement.updateDelay = calculateUpdateDelay(velocity, movement.destinationAngle, startingAngle);
    movement.ratio = (float) movement.updateDelay / (int) (abs(movement.destinationAngle - startingAngle) != 0 ? abs(movement.destinationAngle - startingAngle) : 1);
    
    queue.push(movement);

    resetPosition(Buzz, velocity);
  }
}

bool JacobsLadder::hasPriority(MovementType type) {
  if (queue.isEmpty()) {
    return true;
  }
  MovementType frontType = (MovementType) queue.peek().type;
  if (type < frontType) {
    return false;
  } else if (type > frontType) {
    emptyQueue();
  }
  return true;
}

void JacobsLadder::emptyQueue() {
  if(!queue.isEmpty())
    while (!queue.isEmpty()) {
      queue.pop();
    }
}

void JacobsLadder::resetPosition(MovementType type) {
  resetPosition(type, 10);  
}

void JacobsLadder::resetPosition(MovementType type, int velocity) {  
  struct Movement movement;
  movement.type = type;
  movement.destinationAngle = 0;
  byte startingAngle = getFinalDestinationAngle();
  if (startingAngle > 90) {
    movement.destinationAngle = 180;
  }
  movement.updateDelay = calculateUpdateDelay(velocity, movement.destinationAngle, startingAngle);
  movement.ratio = (float) movement.updateDelay / (int) (abs(movement.destinationAngle - startingAngle) != 0 ? abs(movement.destinationAngle - startingAngle) : 1);
  
  queue.push(movement);
}

byte JacobsLadder::nextAngleToDestination(byte destinationAngle, byte increment) {
  if (destinationAngle > _angle) {
    _angle += increment;
  } else if (destinationAngle < _angle) {
    _angle -= increment;
  }
  return _angle;
}

byte JacobsLadder::getFinalDestinationAngle() {
  if (queue.isEmpty()) {
    return _angle;
  }
  Movement lastMovement = queue.peekTail();
  return lastMovement.destinationAngle;
}

int JacobsLadder::calculateUpdateDelay(int velocity, byte destinationAngle, byte startingAngle) {
  return (velocity / 1000.0) * abs(destinationAngle - startingAngle);
}

byte JacobsLadder::incrementForRatio(float ratio) {
	byte increment = 1;
	//Serial.println(ratio);
  if (ratio < .15) {
    increment = 1;
  }
  return increment;
}

