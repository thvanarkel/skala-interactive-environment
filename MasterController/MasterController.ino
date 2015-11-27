// 2d.ino

#include <Wire.h>

//const byte GRID_SIZE = 9;
const byte NUM_SLAVES = 3;
//const byte NUM_SENSORS = 1;
const byte NUM_LADDERS = 11;

const byte servoArray[NUM_LADDERS][2] = {	
  {0, 2},
  {0, 3},
  {0, 4},
  {0, 5},
  {1, 2},
  {1, 3},
  {2, 2},
  {2, 3},
  {2, 5},
  {2, 6},
  {2, 7},
};

const byte MAX_NUM_OF_SERVOS_PER_ARDUINO = 10;
// const byte dataArray[MAX_NUM_OF_SERVOS_PER_ARDUINO];

const int stateLadderNormal = 0;
const int stateLadderRustling = 1;
const int stateLadderCascading = 2;
const int stateLadderBuzzing = 4;

void setup() {
  // Start the I2C Bus as Master
  Wire.begin();

  Serial.begin(9600);
}

void sendCommand(byte slaveId, byte index, byte state) {
  byte message[2] = {index, state};
  Wire.beginTransmission(slaveId);  // transmit to devices
  Wire.write(message, 2);    // sends the number if the sensor detects movement
  Serial.println("Did write");
  Wire.endTransmission();    // stop transmitting
  Serial.println("Ended transmission");
}

void loop() {
  //Read Serial data stream:
//  #define MAX_MILLIS_TO_WAIT 1000  //or whatever
//  byte numBytes = 2; // expects 2 bytes
//  unsigned long starttime;
//  byte message[2];
//  starttime = millis();
//
//  while ( (Serial.available() < numBytes) && ((millis() - starttime) < MAX_MILLIS_TO_WAIT) )
//  {
//    // hang in this loop until we either get 2 bytes of data or 1 second
//    // has gone by
//  }
//  if (Serial.available() < numBytes)
//  {
//    // the data didn't come in - handle that problem here
//    //  Serial.println("ERROR - Didn't get 2 bytes of data!");
//  }
//  else
//  {
//    for (int i = 0; i < numBytes; i++) {
//      message[i] = Serial.read(); // Then: Get them.
//    }
//
//    byte globalServoId = message[0];
//    byte slaveId = servoArray[globalServoId][0];
//    byte localServoId = servoArray[globalServoId][1];
//    byte state = message[1];
//
//    sendCommand(slaveId, localServoId, state);
//  }
//
//  delay(1000);

  for (int i = 0; i < NUM_LADDERS; i++) {
    sendCommand(servoArray[i][0], servoArray[i][1], 2);
    Serial.print("Sent command to ");
    Serial.println(i);
    delay(2000);
  }

//  for (int i = 0; i < NUM_LADDERS; i++) {
//    sendCommand(servoArray[i][0], servoArray[i][1], 0);
//    Serial.print("Sent command to ");
//    Serial.print(i);
//  }
  
  
}




