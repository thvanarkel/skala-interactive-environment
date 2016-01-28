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
  lockTime = millis() + 600;
}

void JacobsLadder::addMovement(MovementType type) {
  addMovement(type, 0);
}

void JacobsLadder::addMovement(MovementType type, byte angle) {
  if(queue.count() > 10) {
    return;
  }
  
  byte startingAngle = getFinalDestinationAngle();
  byte destinationAngle = 0;
  byte buzzAngle = 0;
  bool needsResetting = false;
  
  switch(type) {
    case Cascade:
      destinationAngle = 0;
      if (startingAngle < 90) {
        destinationAngle = 180;
      }
      addTurn(Cascade, destinationAngle);
      addTurn(Wait, destinationAngle);
      break;

    case Buzz:
      buzzAngle = 30;
      if (angle != 0) {
         buzzAngle = angle;
      }
      destinationAngle = buzzAngle;
      if (startingAngle > 90) {
        destinationAngle = 180 - buzzAngle;
      }
      addTurn(Buzz, destinationAngle);
      addTurn(Buzz, startingAngle);
      break;
   
    case Halfway:
      destinationAngle = 90;
      addTurn(Halfway, destinationAngle);
      addTurn(Wait, destinationAngle);
      break;
  }
}

void JacobsLadder::addTurn(MovementType type, byte destinationAngle) {
  struct Movement movement;
  movement.type = type;
  movement.destinationAngle = destinationAngle;
  if (movement.type == Cascade) {
    movement.waitTime = 10;
  } else if (movement.type == Halfway) {
    movement.waitTime = 10;
  } else if (movement.type == Buzz) {
    movement.waitTime = 16;
  } else if (movement.type == Wait) {
    movement.waitTime = 200;
  }
  queue.push(movement);
}

void JacobsLadder::updateLadder() {
  if (isPaused) {
    lockTime = millis() + 600;
    return;
  }
  
  if (queue.isEmpty()) {
    started = false;
    return;
  }

  if(millis() < lockTime) {
    return;
  }
  Movement movement = queue.peek();
  doMovement(movement);
  if (_angle == movement.destinationAngle) {
    queue.pop();
  }
}

void JacobsLadder::doMovement(struct Movement movement) {
  byte dAngle = max(_angle, movement.destinationAngle) - min(_angle, movement.destinationAngle);
  lockTime = millis() + (movement.waitTime*20);

  if (movement.type == Cascade) {
    if (movement.destinationAngle > _angle) {
      _angle += min(10, dAngle);
    } else {
      _angle -= min(10, dAngle);
    }
  } else if (movement.type ==Buzz) {
    _angle = movement.destinationAngle;
  }
  
  if (!started) {
    started = true;
  }

  //_angle = movement.destinationAngle;
  servo.write(_angle);
}

void JacobsLadder::pause() {
  if (isPaused) {
    isPaused = false;
  } else {
    isPaused = true;
  }
}

byte JacobsLadder::getFinalDestinationAngle() {
  if (queue.isEmpty()) {
    return _angle;
  }
  Movement lastMovement = queue.peekTail();
  return lastMovement.destinationAngle;
}
