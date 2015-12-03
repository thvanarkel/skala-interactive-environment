#include <Wire.h>

// const byte NUM_SLAVES = 3; // not used
const byte NUM_SERVOS = 11; // the amount of jacobs ladders in the system
const byte servoArray[NUM_SERVOS][2] = {
  // the message from the pc is 2 bytes: servoID (0-10), state
  {0, 2}, // servoID 0 = slave 0, pin 2
  {0, 3}, // servoID 1 = slave 0, pin 3
  {0, 4}, // servoID 2 = slave 0, pin 4
  {0, 5}, // servoID 3 = slave 0, pin 5
  {1, 2}, // servoID 4 = slave 1, pin 2
  {1, 3}, // servoID 5 = slave 1, pin 3
  {2, 2}, // servoID 6 = slave 2, pin 2
  {2, 3}, // servoID 7 = slave 2, pin 3
  {2, 5}, // servoID 8 = slave 2, pin 5
  {2, 6}, // servoID 9 = slave 2, pin 6
  {2, 7}, // servoID 10 = slave 2, pin 7
};

// States
const byte stateLadderNormal = 0;
const byte stateLadderRustling = 1;
const byte stateLadderCascading = 2;
const byte stateLadderBuzzing = 4;

const int RUSTLE_DELAY = 400;
const byte RUSTLE_ANGLE = 50;

const byte BUZZING_DELAY = 50;
const byte BUZZING_ANGLE = 10;

const byte CASCADE_DELAY = 1100;
const byte CASCADE_ANGLE = 180;

//

byte sturen = 1;

struct aServo {
  byte state;
  byte angle;
//  bool didTurn;
  unsigned long lockTime;
};

struct aServo servos[NUM_SERVOS];

void setup() {
  Serial.begin(9600);
  Serial.print("initialize all servos");
  // Start the I2C Bus as Master
  Wire.begin();

  for (byte i = 0; i < NUM_SERVOS; i++) {
    struct aServo servo;
    servo.state = stateLadderNormal;
    servo.angle = 0;
    servo.lockTime = millis();
    servos[i] = servo;
  }
}

void getMessage() {
  //GET DATA FROM SERIAL input, so thru the USB cable  

  if(Serial.available()){
  #define MAX_MILLIS_TO_WAIT 1000  //or whatever
  byte numBytes = 2; // expects 2 bytes
  unsigned long starttime;
  byte message[2];
  starttime = millis();
    while ( (Serial.available() < numBytes) && ((millis() - starttime) < MAX_MILLIS_TO_WAIT) )
    { // hang in this loop until we either get 2 bytes of data or 1 second has gone by
    }
    if (Serial.available() < numBytes)
    { // the data didn't come in - handle that problem here
  	Serial.println("ERROR - Didn't get 2 bytes of data!");
    }
    else {
  	for (byte i = 0; i < numBytes; i++) {
  	  message[i] = Serial.read(); // Then: Get them.
  	}
    }

    byte globalServoId = message[0];
    byte state = message[1];
    setState(globalServoId, state);

  }
}

void updateServos() {
  for (byte i = 0; i < NUM_SERVOS; i++) {
    struct aServo servo = servos[i];
    switch (servo.state) {
      case stateLadderNormal:
        if (millis() > servo.lockTime) {
          if (servo.angle > 90) {
            servo.angle = 180;
          } else {
            servo.angle = 0;
          }
          servo.lockTime = millis();
          sendCommand(servoArray[i][0], servoArray[i][1], servo.angle);
        }
        break;

      case stateLadderRustling:
        //        Serial.println("State rustling");
        if (millis() > servo.lockTime) {
          if (servo.angle > 90) {
            if (servo.angle == 180) {
              servo.angle = 180 - RUSTLE_ANGLE;
            } else {
              servo.angle = 180;
            }
          } else {
            if (servo.angle == 0) {
              servo.angle = RUSTLE_ANGLE;
            } else {
              servo.angle = 0;
            }
          }
          servo.lockTime = millis() + RUSTLE_DELAY;
          sendCommand(servoArray[i][0], servoArray[i][1], servo.angle);
        }
        break;

      case stateLadderCascading:
        //Serial.println("State cascading");
        if (millis() > servo.lockTime) {
          if (servo.angle > 90) {
            servo.angle = 0;
          } else {
            servo.angle = CASCADE_ANGLE;
          }
          servo.lockTime = millis() + CASCADE_DELAY;
          sendCommand(servoArray[i][0], servoArray[i][1], servo.angle);
        }

        break;

      case stateLadderBuzzing:
        //        Serial.println("State buzzing");
        if (millis() > servo.lockTime) {
          if (servo.angle > 90) {
            if (servo.angle == 180) {
              servo.angle = 180 - BUZZING_ANGLE;
            } else {
              servo.angle = 180;
            }
          } else {
            if (servo.angle == 0) {
              servo.angle = BUZZING_ANGLE;
            } else {
              servo.angle = 0;
            }
          }
          servo.lockTime = millis() + BUZZING_DELAY;
          sendCommand(servoArray[i][0], servoArray[i][1], servo.angle);
        }
        break;
    }
    
    Serial.print("\n New State: ");
    Serial.println(servo.state);
    servos[i] = servo;
  }

}

void sendCommand(byte slaveId, byte localServoId, byte angle) {
  byte message[2] = {localServoId, angle};
  Wire.beginTransmission(slaveId);  // transmit to devices
  Wire.write(message, 2);    // sends the number if the sensor detects movement
  Serial.print("\n Wrote to slave # ");
  Serial.print(slaveId);
  Serial.print(", servo: ");
  Serial.print(localServoId);
  Serial.print(", angle: ");
  Serial.print(angle);
  Wire.endTransmission();    // stop transmitting
  Serial.print("\nEnded transmission");
}

void setState(byte globalServoId, byte state) {
  struct aServo servo = servos[globalServoId];
 // Serial.print("\nSETTING STATE \nGlobal servo ID: ");
  //Serial.print(globalServoId);
  servo.state = state;
//  Serial.print(", new state: ");
//  Serial.print(servo.state);
//  servo.didTurn = false;
  servos[globalServoId] = servo;
}

void loop() {
  getMessage();
  updateServos();
  // delay(1000);
}

