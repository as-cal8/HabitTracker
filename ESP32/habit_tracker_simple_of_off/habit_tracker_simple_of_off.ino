#include <time.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

/*tracking variables*/
uint consequtive_days = 0;
uint32_t max_consequtive_days = 0;
bool today_done_flag = false;
int today_day = 0;

/*Switch pins*/
int switch1_pin = 34;
int switch2_pin = 35;
int switch3_pin = 32;
int switch4_pin = 33;

/*LED pins*/
int led1_pin = 23;
int led2_pin = 22;
int led3_pin = 21;
int led4_pin = 19;

/*BLE */
BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;
bool deviceConnected = false;

#define SERVICE_UUID            "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_TX  "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"

class MyServerCallbacks: public BLEServerCallbacks {
  void onConnect(BLEServer* pServer)
  {
    deviceConnected = true;
  };

  void onDisconnect(BLEServer* pServer) {
    deviceConnected = false;
  };
};

void updateState()
{
    if (
      digitalRead(switch1_pin) == LOW && 
      digitalRead(switch2_pin) == LOW && 
      digitalRead(switch3_pin) == LOW && 
      digitalRead(switch4_pin) == LOW &&
      !today_done_flag
    )
    {
      consequtive_days += 1;
      if (consequtive_days > max_consequtive_days)
      {
        max_consequtive_days = consequtive_days;
      }
      today_done_flag = true;
      time_t t = time(NULL);
      struct tm tm = *localtime(&t);
      today_day = tm.tm_mday;
    }
}

void ledControls()
{
  if(digitalRead(switch1_pin) == HIGH)
  {
    digitalWrite(led1_pin, LOW);
  }
  if(digitalRead(switch1_pin) == LOW)
  {
    digitalWrite(led1_pin, HIGH);
  }

  if(digitalRead(switch2_pin) == HIGH)
  {
    digitalWrite(led2_pin, LOW);
  }
  if(digitalRead(switch2_pin) == LOW)
  {
    digitalWrite(led2_pin, HIGH);
  }

  if(digitalRead(switch3_pin) == HIGH)
  {
    digitalWrite(led3_pin, LOW);
  }
  if(digitalRead(switch3_pin) == LOW)
  {
    digitalWrite(led3_pin, HIGH);
  }

  if(digitalRead(switch4_pin) == HIGH)
  {
    digitalWrite(led4_pin, LOW);
  }
  if(digitalRead(switch4_pin) == LOW)
  {
    digitalWrite(led4_pin, HIGH);
  }
}

void checkDay()
{
  time_t t = time(NULL);
  struct tm tm = *localtime(&t);
  if (today_day != tm.tm_mday)
  {
    today_done_flag = false;
  }
}

void setup() {
  pinMode(switch1_pin, INPUT);
  pinMode(led1_pin, OUTPUT);
  pinMode(switch2_pin, INPUT);
  pinMode(led2_pin, OUTPUT);
  pinMode(switch3_pin, INPUT);
  pinMode(led3_pin, OUTPUT);
  pinMode(switch4_pin, INPUT);
  pinMode(led4_pin, OUTPUT);

  BLEDevice::init("HabitTracker");

  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
                    CHARACTERISTIC_UUID_TX,
                    BLECharacteristic::PROPERTY_NOTIFY
  );

  pCharacteristic->addDescriptor(new BLE2902());
  pService->start();
  pServer->getAdvertising()->start();

}

void loop() 
{ // TODO ESP32 sleep mode with interrupt (use pins)
  ledControls();
  updateState();
  checkDay();
  if (deviceConnected)
  {
    char txString[8];
    dtostrf(max_consequtive_days, 1, 2, txString);
    pCharacteristic->setValue(txString);
    pCharacteristic->notify();
  }
}