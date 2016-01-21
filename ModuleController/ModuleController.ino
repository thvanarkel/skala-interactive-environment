#include "QueueList.h"
#include <Servo.h>
#include "JacobsLadder.h"

///////////////////////////
///   SETUP VARIABLES   ///
///////////////////////////

const int MODULE_ID = 0;
const int NUM_LADDERS = 5;
const int PINS[NUM_LADDERS] = {2,3,7,5,6};

///////////////////////////
///  GLOBAL VARIABLES   ///
///////////////////////////

JacobsLadder* ladders[NUM_LADDERS];

bool moduleCalibrated = false;

unsigned long lastUpdated;

// For serial communication
String inputString = ""; //Available input is appended each loop
boolean stringComplete = false; //Indicates the incoming command is complete and available and should be processed

///////////////////////////
///  DEBUG/CALIBRATION  ///
///////////////////////////

const bool debugging = false;
byte currentLadder = 0;

void setup() {
//	Serial.println("Setup");
	Serial.begin(9600);
  Serial.setTimeout(500);
  lastUpdated = millis();

  for (int i = 0; i < NUM_LADDERS; i++) {
    JacobsLadder* ladder = new JacobsLadder;
    ladder->init(i, PINS[i], 500, 2500);
    ladders[i] = ladder;
    Serial.print("L");
    Serial.print(i);
    Serial.println(";");
  }
  
  Serial.println("R;");
}

void onStart(byte index) {
  Serial.print("S");
  Serial.print(index);
  Serial.println(";");
}

void onEnd(byte index) {
  Serial.print("E");
  Serial.print(index);
  Serial.println(";");
}

void loop() {
  if (debugging) {
    if (Serial.available() > 0) {
      char c = Serial.read();
      switch(c) {
        case '0':
          currentLadder = 0;
          break;
        case '1':
          currentLadder = 1;
          break;
        case '2':
          currentLadder = 2;
          break;
        case '3':
          currentLadder = 3;
          break;
        case '4':
          currentLadder = 4;
          break;

        case 'b':
          ladders[currentLadder]->addMovement(Buzz, 150, 0, onStart, onEnd);
          break;
        case 'c':
          ladders[currentLadder]->addMovement(Cascade, 150, 0, onStart, onEnd);
          break;
      } 
    }
    updateLadders();
    return;
  }

  if (Serial.available() > 0) {
    byte message[3];
    Serial.readBytesUntil(';', message, 3);
    byte index = message[0];
    MovementType type = (MovementType) message[1];
    byte velocity = message[2];
    ladders[index]->addMovement(type, velocity, onStart, onEnd);
  }

  // Update the ladders
  updateLadders();
}

void updateLadders() {
  for (int i = 0; i < NUM_LADDERS; i++) {
    JacobsLadder* ladder = ladders[i];
    ladder->updateLadder();
  }
}
