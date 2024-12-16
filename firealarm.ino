#include <Arduino.h>
#if defined(ESP32)
  #include <WiFi.h>
#elif defined(ESP8266)
  #include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>
#include "DHT.h"
#include <time.h>
#include <Servo.h>

#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

#define WIFI_SSID "ThanhHau"
#define WIFI_PASSWORD "0833228824"
#define API_KEY "AIzaSyCGIu6ZWulShGtfJEY6-ae7_sOKd8Bh3pE"
#define DATABASE_URL "https://firealarm-iot-3cc88-default-rtdb.asia-southeast1.firebasedatabase.app/"


FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long sendDataPrevMillis = 0;
bool signupOK = false;

#define DHTPIN D3
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

#define GAS_SENSOR_PIN A0
#define FLAME_SENSOR_PIN D2
const int gasThreshold = 1000;

const int buzzerPin = D4;
const int servoPin = D5;
Servo myServo;

// State tracking
bool previousAbnormal = false;
String abnormalStartTime = "";  // Thời gian bắt đầu 
String abnormalEndTime = "";    // Thời gian kết thúc
bool flameDetectedCause = false;
float maxTemperatureCause = 0.0;
int maxGasValueCause = 0;

void setup() {
  Serial.begin(115200);
  pinMode(FLAME_SENSOR_PIN, INPUT);
  pinMode(buzzerPin, OUTPUT);
  myServo.attach(servoPin);
  myServo.write(0);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("Sign up successful");
    signupOK = true;
  } else {
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }

  config.token_status_callback = tokenStatusCallback;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  dht.begin();
  configTime(7 * 3600, 0, "pool.ntp.org", "time.nist.gov");
  while (time(nullptr) < 1000000000) {
    delay(100);
  }

  Serial.print("Setup successful !!! ");
}

String getFormattedTime() {
  time_t now = time(nullptr);
  struct tm* p_tm = localtime(&now);
  char timeStr[20];
  strftime(timeStr, sizeof(timeStr), "%Y-%m-%d %H:%M:%S", p_tm);
  return String(timeStr);
}

time_t strtotime(String timeStr) {
  struct tm t;
  sscanf(timeStr.c_str(), "%d-%d-%d %d:%d:%d", &t.tm_year, &t.tm_mon, &t.tm_mday, &t.tm_hour, &t.tm_min, &t.tm_sec);
  t.tm_year -= 1900;
  t.tm_mon -= 1;
  return mktime(&t);
}

void logAbnormalEvent(String startTime, String endTime) {
  String path = "/abnormalEvents/" + startTime;
  FirebaseJson json;

  json.set("startTime", startTime);
  json.set("endTime", endTime);
  json.set("duration", (endTime != "" && startTime != "") ? (strtotime(endTime) - strtotime(startTime)) : 0);
  json.set("gasValue", maxGasValueCause);
  json.set("flameDetected", flameDetectedCause);
  json.set("temperature", maxTemperatureCause);

  if (Firebase.RTDB.setJSON(&fbdo, path.c_str(), &json)) {
    Serial.println("Abnormal event logged.");
  } else {
    Serial.println("Failed to log abnormal event.");
    Serial.println("Reason: " + fbdo.errorReason());
  }
}

void activateEmergencyMode() {
  myServo.write(180);
  digitalWrite(buzzerPin, HIGH);
}

void deactivateEmergencyMode() {
  myServo.write(0);
  digitalWrite(buzzerPin, LOW);
}

void updateFirebaseState() {
  Firebase.RTDB.setBool(&fbdo, "state/buzzer", digitalRead(buzzerPin));
  Firebase.RTDB.setBool(&fbdo, "state/door", myServo.read() == 180);
}

void updateDeviceStates() {
  if (Firebase.RTDB.getBool(&fbdo, "state/buzzer")) {
    bool buzzerState = fbdo.boolData();
    digitalWrite(buzzerPin, buzzerState ? HIGH : LOW);
  } 
  if (Firebase.RTDB.getBool(&fbdo, "state/door")) {
    bool doorState = fbdo.boolData();
    myServo.write(doorState ? 180 : 0); // 180 độ mở cửa, 0 độ đóng cửa
  }
}

void loop() {
  if (Firebase.ready() && signupOK) {
    updateDeviceStates();
    float temperature = dht.readTemperature();
    float humidity = dht.readHumidity();
    int gasValue = analogRead(GAS_SENSOR_PIN);
    bool flameDetected = digitalRead(FLAME_SENSOR_PIN) == LOW;

    bool isAbnormal = (temperature >= 40.0 || gasValue > gasThreshold || flameDetected);

    if (isAbnormal && !previousAbnormal) {
      Serial.println("Abnormal condition detected!");
      abnormalStartTime = getFormattedTime();
      flameDetectedCause = flameDetected;
      maxTemperatureCause = temperature;
      maxGasValueCause = gasValue;
      activateEmergencyMode();
      updateFirebaseState();
    }
    else if (!isAbnormal && previousAbnormal) {
      Serial.println("Environment returned to normal.");
      abnormalEndTime = getFormattedTime();
      logAbnormalEvent(abnormalStartTime, abnormalEndTime);
      deactivateEmergencyMode();
      updateFirebaseState();
      flameDetectedCause = false;
      maxTemperatureCause = 0.0;
      maxGasValueCause = 0;
      abnormalStartTime = "";
      abnormalEndTime = "";
    }

    Firebase.RTDB.setFloat(&fbdo, "sensors/temperature", temperature);
    Firebase.RTDB.setFloat(&fbdo, "sensors/humidity", humidity);
    Firebase.RTDB.setInt(&fbdo, "sensors/gas", gasValue);
    Firebase.RTDB.setBool(&fbdo, "sensors/flame", flameDetected);

    previousAbnormal = isAbnormal;
    delay(1000);
  }
}