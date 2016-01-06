/*
 * JacobsLadder.cpp - Library for controlling JacobsLadder objects
 * in the SKALA prototype
 * Written by Thomas van Arkel
 * Last updated: Dec 26, 2015
 * This work is licensed under a Creative Commons Attribution 4.0 International License
 */

#include "Arduino.h"
#include "JacobsLadder.h"

void JacobsLadder::init(int pin, int minPulse, int maxPulse)
{
  servo.attach(pin, minPulse, maxPulse);
  servo.write(0);
  _angle = 0;
}

void JacobsLadder::addMovement(MovementType type, int velocity) {
  switch (type) {

    case Cascade:
      cascade(velocity);
      break;

//    case Tease:
//      tease();
//      break;

    case Buzz:
      buzz(velocity);
      break;

    default:
      break;
  }
}

void JacobsLadder::updateLadder() {
  if (queue.isEmpty()) {
    return;
  }
  Movement movement = queue.peek();
  if (millis() - _lastUpdated < movement.updateDelay) {
    return;
  }
  _angle = nextAngleToDestination(movement.destinationAngle);
  servo.write(_angle);
  if (_angle == movement.destinationAngle) {
    queue.pop();
  }
  _lastUpdated = millis();
}

bool JacobsLadder::pause(int timeout = 0) {
  return false;
}


void JacobsLadder::cascade() {
  cascade(90);
}
void JacobsLadder::cascade(int velocity) {
  if (hasPriority(Cascade)) {
    struct Movement movement;
    movement.type = Cascade;
    movement.destinationAngle = 0;
    int startingAngle = getFinalDestinationAngle();
    if (startingAngle < 90) {
      movement.destinationAngle = 180;
    }
    movement.updateDelay = (velocity / 1000.0) * abs(movement.destinationAngle - startingAngle);
    queue.push(movement);
  }
}

void JacobsLadder::buzz(){
  buzz(400);
}
void JacobsLadder::buzz(int velocity) {
  const int buzzAngle = 50;
  if (hasPriority(Buzz)) {
    Serial.println("Buzzing");
    resetPosition(Buzz, velocity);

    struct Movement movement;
    movement.type = Buzz;
    movement.destinationAngle = buzzAngle;
    int startingAngle = getFinalDestinationAngle();
    if (startingAngle > 90) {
      movement.destinationAngle = 180 - buzzAngle;
    }
    movement.updateDelay = (velocity / 1000.0) * abs(movement.destinationAngle - startingAngle);
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
  int startingAngle = getFinalDestinationAngle();
  if (startingAngle > 90) {
    movement.destinationAngle = 180;
  }
  movement.updateDelay = (velocity / 1000.0) * abs(movement.destinationAngle - startingAngle);
  queue.push(movement);
}

int JacobsLadder::nextAngleToDestination(int destinationAngle) {
  if (destinationAngle > _angle) {
    _angle ++;
  } else if (destinationAngle < _angle) {
    _angle --;
  }
  return _angle;
}

int JacobsLadder::getFinalDestinationAngle() {
  if (queue.isEmpty()) {
    return _angle;
  }
  Movement lastMovement = queue.peekTail();
  return lastMovement.destinationAngle;
}

