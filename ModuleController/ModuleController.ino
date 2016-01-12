#include <QueueList.h>
#include <Servo.h>
#include "JacobsLadder.h"

///////////////////////////
///   SETUP VARIABLES   ///
///////////////////////////
const int MODULE_ID = 0;
const int NUM_LADDERS = 1;
const int PINS[NUM_LADDERS] = {3};

const char CALIBRATE_START = 's';
const char CALIBRATE_REGISTERED = 'c';

///////////////////////////
///  GLOBAL VARIABLES   ///
///////////////////////////

JacobsLadder* ladders[NUM_LADDERS];

bool moduleCalibrated = false;

long lastUpdated;

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(500);
  lastUpdated = millis();

  for (int i = 0; i < NUM_LADDERS; i++) {
    JacobsLadder* ladder = new JacobsLadder;
    ladder->init(PINS[i], 500, 2500);
    ladders[i] = ladder;
  }
}

void loop() {
  // TODO: calibrate the module if not calibrated yet
//  if (!moduleCalibrated) {
//    calibrate();
//    moduleCalibrated = true;
//  }

  // TODO: Read input from computer over serial
//  if (Serial.available() > 0) {
//    byte message[3];
//    Serial.readBytesUntil(0x04, message, 3);
//    int index = message[0];
//    MovementType type = (MovementType) message[1];
//    int velocity = message[2];
//    
//    ladders[index]->addMovement(type, velocity);
//  }
  
  if (Serial.available() > 0) {
    char c = Serial.read();
    if (c == 'b') {
      for (int i = 0; i < NUM_LADDERS; i++) {
        ladders[i]->addMovement(Buzz, 150);
      }
    } else if (c == 'c') {
      for (int i = 0; i < NUM_LADDERS; i++) {
        ladders[i]->addMovement(Cascade, 150);
      }
    }
  }

  if (millis() - lastUpdated > 6000) {
    ladders[0]->addMovement(Cascade, 150);
    lastUpdated = millis();
  }

  // Update the ladders
  for (int i = 0; i < NUM_LADDERS; i++) {
    JacobsLadder* ladder = ladders[i];
    ladder->updateLadder();
  }
}

void calibrate() {
  for (int i = 0; i < NUM_LADDERS; i++) {
    JacobsLadder* ladder = ladders[i];
    byte message[2] = {MODULE_ID, i};
    Serial.write(message, 2);
    bool startAllowed = false;
    while (!startAllowed) {
      if (Serial.available() > 0) {
        if (Serial.read() == CALIBRATE_START) {
          startAllowed = true;
        }
      }
    }
    bool didCalibrate = false;
    long lastAdded = millis();
    while (!didCalibrate) {
      ladder->updateLadder();
      if (millis() - lastAdded > 500) {
        ladder->addMovement(Buzz, 150);
        lastAdded = millis();
      }
      if (Serial.available() > 0) {
        if (Serial.read() == CALIBRATE_REGISTERED) {
          didCalibrate = true;
        }
      }
    }
  }
}
