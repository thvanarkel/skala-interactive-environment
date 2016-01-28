#include "QueueList.h"
#include <Servo.h>
#include "JacobsLadder.h"

const int NUM_LADDERS = 5;
const int PINS[NUM_LADDERS] = {2,3,7,5,6};

JacobsLadder* ladders[NUM_LADDERS];

const bool debugging = false;
byte currentLadder = 0;

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

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.setTimeout(500);
  for (int i = 0; i < NUM_LADDERS; i++) {
    JacobsLadder* ladder = new JacobsLadder;
    ladder->init(i, PINS[i], 550, 2450);
    ladders[i] = ladder;
    Serial.print("L");
    Serial.print(i);
    Serial.println(";");
  }
  
  Serial.println("R;");
}

void loop() {
  // put your main code here, to run repeatedly:
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
          ladders[currentLadder]->addMovement(Buzz, 0);
          break;
        case 'c':
          ladders[currentLadder]->addMovement(Cascade, 0);
          break;
        case 'h':
          ladders[currentLadder]->addMovement(Halfway, 0);
          break;
      } 
    }
    //ladders[currentLadder]->addMovement(Cascade, 0);

   
  }
   if (Serial.available() > 0) {
      byte message[2];
      Serial.readBytesUntil(';', message, 2);
      byte index = message[0];
      MovementType type = (MovementType) message[1];
      byte velocity = message[2];
      ladders[index]->addMovement(type);
    }
    updateLadders();
//    delay(100);
    return;
}

  void updateLadders() {
  for (int i = 0; i < NUM_LADDERS; i++) {
    JacobsLadder* ladder = ladders[i];
    ladder->updateLadder();
  }
}

