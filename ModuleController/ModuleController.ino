#include <QueueList.h>
#include <Servo.h>
#include "JacobsLadder.h"

///////////////////////////
///   SETUP VARIABLES   ///
///////////////////////////
const int MODULE_ID = 0;
const int NUM_LADDERS = 5;
const int PINS[NUM_LADDERS] = {2, 3, 4, 5, 6};

const char CALIBRATE_START = 's';
const char CALIBRATE_REGISTERED = 'c';

///////////////////////////
///  GLOBAL VARIABLES   ///
///////////////////////////

JacobsLadder* ladders[NUM_LADDERS];

bool moduleCalibrated = false;

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(500);

  for (int i = 0; i < NUM_LADDERS; i++) {
    JacobsLadder* ladder = new JacobsLadder;
    ladder->init(PINS[i], 500, 2500);
    ladders[i] = ladder;
  }
}

void loop() {
  // TODO: calibrate the module
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
      ladders[0]->addMovement(Buzz, 150);
    } else if (c == 'c') {
      ladders[0]->addMovement(Cascade, 100);
    }
  }

  // Update the ladders
  for (int i = 0; i < NUM_LADDERS; i++) {
    JacobsLadder* ladder = ladders[i];
    ladder->updateLadder();
  }
}

void calibrate() {
  
}

/*void calibrate() {
  for (int i = 0; i < NUM_NODES; i++) {
    struct Node node = nodes[i];
    byte message[2] = {MODULE_ID, node.index};
    Serial.write(message, 2);
    bool startAllowed = false;
    while (!startAllowed) {
      if (Serial.available() > 0) {
        if (Serial.read() == CALIBRATE_START) {
          startAllowed = true;
        }
      }
    }
    bool didCalibrateNode = false;
    while (!didCalibrateNode) {
      bool finished = false;
      while (!finished) {
        finished = node.ladder.tease();
      }
      if (Serial.available() > 0) {
        if (Serial.read() == CALIBRATE_REGISTERED) {
          didCalibrateNode = true;
        }
      }
    }
    saveNode(node);
  }
}*/

