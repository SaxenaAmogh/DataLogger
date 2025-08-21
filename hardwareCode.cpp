#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// // --- IMPORTANT ---
// // Use the EXACT SAME UUIDs here as you do in your Android app!
// #define SERVICE_UUID        "0000abcd-0000-1000-8000-00805f9b34fb"
// #define CHARACTERISTIC_UUID "00001234-0000-1000-8000-00805f9b34fb"

// --- BLE UUIDs ---
#define SERVICE_UUID        "0000abcd-0000-1000-8000-00805f9b34fb"
#define CHARACTERISTIC_UUID "00001234-0000-1000-8000-00805f9b34fb"

// --- Rain Sensor ---
// Using GPIO34 (ADC1 pin)
#define RAIN_PIN 34
constexpr float VCC = 3.3f;       // ESP32 supply voltage

// BLE characteristic pointer
BLECharacteristic *pCharacteristic;

// Packet values
float sensorValue1 = 0.0f;  // voltage
float sensorValue2 = 0.0f;  // raw ADC
float sensorValue3 = 0.0f;  // wetness %
float sensorValue4 = 0.0f;  // reserved

// EMA filter
constexpr float ALPHA = 0.15f;
float emaVoltage = 0.0f;

void setup() {
  Serial.begin(9600);

  // --- ADC Setup ---
  analogReadResolution(12);                  // 0–4095
  analogSetPinAttenuation(RAIN_PIN, ADC_11db); // ~0–3.3V range
  pinMode(RAIN_PIN, INPUT);

  // --- BLE Setup ---
  BLEDevice::init("ESP32 Rain Sensor");
  BLEServer *pServer = BLEDevice::createServer();
  BLEService *pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );

  pCharacteristic->addDescriptor(new BLE2902());

  pService->start();

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();

  Serial.println("Rain sensor BLE ready. Now advertising...");
}

void loop() {
  // --- Read rain sensor ---
  int raw = analogRead(RAIN_PIN);           // 0..4095
  float voltage = (raw / 4095.0f) * VCC;   // Convert to volts

  // Smooth with EMA
  if (emaVoltage == 0.0f) emaVoltage = voltage;
  emaVoltage = (1.0f - ALPHA) * emaVoltage + ALPHA * voltage;

  // Convert to percentage: dry=100%, wet=0%
  float wetnessPercent = map(raw, 4095, 0, 0, 100);

  // --- Fill packet ---
  sensorValue1 = emaVoltage;        // smoothed voltage
  sensorValue2 = (float)raw;        // raw ADC
  sensorValue3 = wetnessPercent;    // 0..100 %
  sensorValue4 = 0.0f;              // reserved

  // Pack into byte array (16 bytes for 4 floats)
  uint8_t data[16];
  memcpy(data + 0,  &sensorValue1, 4);
  memcpy(data + 4,  &sensorValue2, 4);
  memcpy(data + 8,  &sensorValue3, 4);
  memcpy(data + 12, &sensorValue4, 4);

  // Send via BLE
  pCharacteristic->setValue(data, 16);
  pCharacteristic->notify();

  // Debug output
  Serial.print("Rain [V, ADC, %, Rsv]: ");
  Serial.print(sensorValue1, 3); Serial.print(" V, ");
  Serial.print(sensorValue2, 0); Serial.print(", ");
  Serial.print(sensorValue3, 1); Serial.print(" %, ");
  Serial.println(sensorValue4, 0);

  delay(1000); // Update every second
}