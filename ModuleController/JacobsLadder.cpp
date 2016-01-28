/*
 * JacobsLadder.cpp - Library for controlling JacobsLadder objects
 * in the SKALA prototype
 * Written by Thomas van Arkel and Mo de Ruiter
 * Last updated: Jan 12, 2016
 * This work is licensed under a Creative Commons Attribution 4.0 International License
 */

#include "Arduino.h"
#include "JacobsLadder.h"

void JacobsLadder::init(byte ladderIndex, int pin, int minPulse, int maxPulse)
{
  index = ladderIndex;
  servo.attach(pin, minPulse, maxPulse);
  servo.write(0);
  _angle = 0;
  isPaused = false;
}

void JacobsLadder::addMovement(MovementType type, int velocity, LadderCallback onStart, LadderCallback onEnd) {
  addMovement(type, velocity, 0, onStart, onEnd);
}

void JacobsLadder::addMovement(MovementType type, int velocity, byte angle, LadderCallback onStart, LadderCallback onEnd) {
  if(queue.count() > 4) {
    return;
  }
  
  if (!hasPriority(type)) {
    return;
  }
  byte startingAngle = getFinalDestinationAngle();
  byte destinationAngle = 0;
  byte buzzAngle = 0;
  int updateDelay = 0;
  bool needsResetting = false;
  bool shouldSendOnStart = true;
  bool shouldSendOnEnd = true;
  
  switch(type) {
    case Cascade:
      destinationAngle = 0;
      if (startingAngle < 90) {
        destinationAngle = 180;
      }
      updateDelay = calculateUpdateDelay(velocity, destinationAngle, startingAngle);
      break;

    case Buzz:
      addMovement(Reset, velocity, 0, NULL, NULL);
      buzzAngle = 30;
      if (angle != 0) {
         buzzAngle = angle;
      }
      destinationAngle = buzzAngle;
      if (startingAngle > 90) {
        destinationAngle = 180 - buzzAngle;
      }
      updateDelay = calculateUpdateDelay(velocity, destinationAngle, startingAngle);
      needsResetting = true;
      shouldSendOnEnd = false;
      break;

    case Reset:
      if (startingAngle != 0 || startingAngle != 180) {
        destinationAngle = 0;
        if (startingAngle > 90) {
          destinationAngle = 180;
        }
        updateDelay = calculateUpdateDelay(velocity, destinationAngle, startingAngle);
      }
      break;

    case Timeout:
      destinationAngle = startingAngle;
      updateDelay = velocity;
      break;
  }

  struct Movement movement;
  movement.type = type;
  movement.destinationAngle = destinationAngle;
  movement.updateDelay = updateDelay;
  movement.ratio = (float) movement.updateDelay / (int) (abs(movement.destinationAngle - startingAngle) != 0 ? abs(movement.destinationAngle - startingAngle) : 1);
  if (shouldSendOnStart) {
    movement.onStart = onStart;
  } else {
    movement.onStart = NULL;
  }
  if (shouldSendOnEnd) {
    movement.onEnd = onEnd;
  } else {
    movement.onEnd = NULL;
  }
  queue.push(movement);
  if (movement.type != Timeout) addMovement(Timeout, 10, 0, NULL, NULL);
  if (needsResetting) {
    addMovement(Reset, velocity, 0, NULL, onEnd);
    addMovement(Timeout, 10, 0, NULL, NULL);
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
  
  if (!started) {
    if (movement.onStart != NULL) movement.onStart(index);
    started = true;
    _lastUpdated = millis();
  }
  //Serial.println(movement.updateDelay);
  if (millis() - _lastUpdated < incrementForRatio(movement.ratio) * movement.updateDelay) {
    return;
  }
  _angle = nextAngleToDestination(movement.destinationAngle, incrementForRatio(movement.ratio));
  servo.write(_angle);
//  Serial.println(_angle);
  if (abs(movement.destinationAngle - _angle) <= incrementForRatio(movement.ratio)) {
    _angle = movement.destinationAngle;
    servo.write(_angle);
    if (movement.onEnd != NULL) movement.onEnd(index);
    started = false;
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

bool JacobsLadder::hasPriority(MovementType type) {
  if (queue.isEmpty() || type == Timeout) {
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
  
  int updateDelay = (velocity / 1000.0) * abs(destinationAngle - startingAngle);
  if (updateDelay <= 5) {
    return 5;
  }
  return updateDelay;
}

byte JacobsLadder::incrementForRatio(float ratio) {
	byte increment = 1;
	//Serial.println(ratio);
  if (ratio < .15) {
    increment = 3;
  }
  return increment;
}

