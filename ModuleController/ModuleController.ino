#include <Servo.h>
#include "JacobsLadder.h"
#include <QueueList.h>

///////////////////////
//  SETUP VARIABLES  //
///////////////////////
const int NUM_NODES = 5;

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
}

void saveNode(struct Node node) {
  nodes[node.index] = node;
}

