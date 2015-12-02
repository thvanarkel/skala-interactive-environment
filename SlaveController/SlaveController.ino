#include <Wire.h>
#include <Servo.h>

//slave 0:

const byte SLAVE_ID = 0;
const byte  NUM_SERVOS = 4;
const byte  ports[NUM_SERVOS] = {2, 3, 4, 5};

//slave 1:
/*
const byte SLAVE_ID = 1;
const byte  NUM_SERVOS = 2;
const byte  ports[NUM_SERVOS] = {2, 3};
*/
// slave 2:
/*
const byte SLAVE_ID = 2;
const byte NUM_SERVOS = 5;
const byte ports[NUM_SERVOS] = {2, 3, 5, 6, 7};
*/
Servo servos[NUM_SERVOS];

void setup() {
  // initiate the servos:
  for (int i = 0; i < NUM_SERVOS; i++) {
    Servo theServo;
    theServo.attach(ports[i]);
    servos[i] = theServo;
  }

  // Start the I2C comms as a slave
  Wire.begin(SLAVE_ID);
  Wire.onReceive(receiveMessage);
  //Wire.onRequest(sendMessage);
  Serial.begin(9600);
}

void receiveMessage(int numBytes) {
  Serial.println("\nReceived message:");
  byte message[numBytes];
  for ( int i = 0; i < numBytes; i++) {
    message[i] = Wire.read();
  }
  byte servoId = message[0];
  byte angle = message[1];

   Serial.print("Local Servo ID: ");
   Serial.print(servoId);
   Serial.print(" must turn to ");
   Serial.print(angle);
   Serial.print(" degrees");

  byte servoArrayId;
  for (int i = 0; i < NUM_SERVOS; i++) {
    if (ports[i] == servoId) {
      servoArrayId = i;
    }
  }
    Serial.print("\nWriting to servo # ");
    Serial.print(servoArrayId);
    Serial.print(". Angle: ");
    Serial.print(angle);

  Servo theServo = servos[servoArrayId];
  theServo.write(angle);
}

void loop() {
 // servos[0].write(random(0,45));
//  delay(1000);
  delay(1);
}

