#include <QueueArray.h>
#include <Servo.h>
#include "JacobsLadder.h"


///////////////////////
//  SETUP VARIABLES  //
///////////////////////
const int MODULE_ID = 0;
const int NUM_NODES = 1;
const int START_PIN = 8;
const int PINS[NUM_NODES] = {2};

const char CALIBRATE_START = 's';
const char CALIBRATE_REGISTERED = 'c';

struct Node {
  int index;
  JacobsLadder ladder;
  QueueArray <MovementType> movementQueue;
};

Node* nodes[NUM_NODES];

bool moduleCalibrated;



void setup() {
  // put your setup code here, to run once:
  for (int i = 0; i < NUM_NODES; i++) {
    struct Node node;
    node.index = i;
    node.ladder.init(PINS[i]);
  }
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:

  // TODO: Calibrate the system
  //  if (!moduleCalibrated) {
  //    calibrate();
  //    moduleCalibrated = true;
  //  }
  
//  // TODO: Read input from computer over serial
//  if (Serial.available() > 0) {
//    if (Serial.read() == 's') {
//      Serial.println("Cascade");
//      addMovement(nodes[0], Cascade);
//    }
//  }
//
//  // TODO: Interpret message into changing node data
//
//  // TODO: Update nodes (= calling motion functions)
//  //Serial.println(nodes[0].movementQueue.count());
//  for (int i = 0; i < NUM_NODES; i++) {
//    updateNode(nodes[i]);
//  }

  bool finished = false;
  while(!finished) {
     nodes[0]->ladder.performMovement(Cascade);
     finished = nodes[0]->ladder.finished;
  }
}

void updateNode(struct Node* node) {
  QueueArray <MovementType> queue = node->movementQueue;
  if (!queue.isEmpty()) {
    MovementType type = queue.peek();
    JacobsLadder ladder = node->ladder;
    ladder.performMovement(type);
    if (ladder.finished) {
      queue.pop();
    }
  }
}

void addMovement(struct Node* node, MovementType type) {
  QueueArray <MovementType> queue = node->movementQueue;
  queue.push(type);
}

/*void saveNode(struct Node node) {
  nodes[node.index] = node;
}*/

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

