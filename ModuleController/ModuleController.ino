#include <Servo.h>
#include "JacobsLadder.h"
#include <QueueList.h>

///////////////////////
//  SETUP VARIABLES  //
///////////////////////
const int NUM_NODES = 1;
const int START_PIN = 8;

struct Node {
  int index;
  JacobsLadder ladder;
  QueueList <int> movementQueue;
  bool movementInProgress;
};

Node nodes[NUM_NODES];

enum movementType
{
  Wait = 0,
  Cascade = 1,
  Tease = 2
};

void setup() {
  // put your setup code here, to run once:
  for (int i = 0; i < NUM_NODES; i++) {
    struct Node node;
    node.index = i;
    node.ladder.init(i);
    node.movementQueue;
    node.movementInProgress = false;
    saveNode(node);
  }
}

void loop() {
  // put your main code here, to run repeatedly:

  // TODO: Read input from computer over serial

  // TODO: Interpret message into changing node data

  // TODO: Update nodes (= calling motion functions)
  for (int i = 0; i < NUM_NODES; i++) {
    struct Node node = nodes[i];
    if (!node.movementQueue.isEmpty()) {
      int type = node.movementQueue.peek();
      bool finished;
      switch(type) {
        case Wait:
          finished = node.ladder.wait(1000);  // TODO: Allow parameters for movement to be sent
          break;
        case Cascade:
          finished = node.ladder.cascade();
          break;
        case Tease:
          finished = node.ladder.tease();
          break;
      }
      if (finished) node.movementQueue.pop();
    }
    saveNode(node);
  }
}

void saveNode(struct Node node) {
  nodes[node.index] = node;
}

