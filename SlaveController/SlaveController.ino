#include <Wire.h>
#include <Servo.h>

//slave 0:
//const byte SLAVE_ID = 0;
//const byte  NUM_SERVOS = 4;
//const byte  ports[NUM_SERVOS] = {2, 3, 4, 5};

//slave 1:
const byte SLAVE_ID = 1;
const byte  NUM_SERVOS = 2;
const byte  ports[NUM_SERVOS] = {2, 3};

// slave 2:
//const byte SLAVE_ID = 2;
//const byte NUM_SERVOS = 5;
//const byte ports[NUM_SERVOS] = {2, 3, 5, 6, 7};

const int RUSTLE_DELAY = 400;
const int RUSTLE_ANGLE = 50;

const int BUZZING_DELAY = 50;
const int BUZZING_ANGLE = 10;

const int stateLadderNormal = 0;
const int stateLadderRustling = 1;
const int stateLadderCascading = 2;
const int stateLadderBuzzing = 4;

struct aServo {
  Servo theServo;
  int state;
  int angle;
  bool didTurn;
  unsigned long previousMovement;
};

struct aServo servos[NUM_SERVOS];

void setup() {
  // initiate the servos:
  for (int i = 0; i < NUM_SERVOS; i++) {
    struct aServo servo;
    servo.theServo.attach(ports[i]);
    servo.state = stateLadderNormal;
    servo.angle = 0;
    servo.theServo.write(0);
    servo.previousMovement = millis();
    servo.didTurn = false;
    servos[i] = servo;
  }

  // Start the I2C comms as a slave
  Wire.begin(SLAVE_ID);
  Wire.onReceive(receiveMessage);
  //Wire.onRequest(sendMessage);
  Serial.begin(9600);
}

void receiveMessage(int numBytes) {
  Serial.println("Did receive");
  byte message[numBytes];
  for ( int i = 0; i < numBytes; i++) {
    message[i] = Wire.read();
  }
  byte index = message[0];
  Serial.print(index);
  Serial.print(", ");
  byte state = message[1];
  Serial.println(state);
  setState(index, state);

}

void updateServos() {
  for (int i = 0; i < NUM_SERVOS; i++) {
    struct aServo servo = servos[i];
    switch (servo.state) {
      case stateLadderNormal:
        if (servo.angle > 90) {
          servo.angle = 180;
        } else {
          servo.angle = 0;
        }
        servo.theServo.write(servo.angle);
        break;

      case stateLadderRustling:
        //        Serial.println("State rustling");
        if (millis() - servo.previousMovement > RUSTLE_DELAY) {
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
          servo.previousMovement = millis();
          servo.theServo.write(servo.angle);
        }
        break;

      case stateLadderCascading:
        //Serial.println("State cascading");
        if (!servo.didTurn) {
          if (servo.angle > 90) {
            servo.angle = 0;
          } else {
            servo.angle = 180;
          }
          servo.didTurn = true;
          servo.theServo.write(servo.angle);  
        }
        
        break;

      case stateLadderBuzzing:
        //        Serial.println("State buzzing");
        if (millis() - servo.previousMovement > BUZZING_DELAY) {
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
          servo.previousMovement = millis();
          servo.theServo.write(servo.angle);
        }
        break;
    }
    servos[i] = servo;
  }
}

void setState(int index, int state) {
  int realIndex = 0;
  for (int i = 0; i < NUM_SERVOS; i++) {
    if (index == ports[i]) {
      realIndex = i;
      break;
    }
  }
  struct aServo servo = servos[realIndex];
  servo.state = state;
  servo.didTurn = false;
  servos[realIndex] = servo;
}

void loop() {
  updateServos();
}

