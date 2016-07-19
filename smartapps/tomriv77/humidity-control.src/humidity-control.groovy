/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Humidity Control
 *
 *  Author: tomriv77
 */
definition(
    name: "Humidity Control",
    namespace: "tomriv77",
    author: "tomriv77",
    description: "Turn on/off humidifier or dehumidifier based on current humidity detected by external sensor.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture@2x.png"
)

preferences {
	section("Monitor humidity from:") {
		input "humiditySensor", "capability.relativeHumidityMeasurement", title: "Select sensor:", required: true
	}
    
    section("Device to turn on/off") {
    	input "deviceSwitch", "capability.switch", title: "Which switch?"
        input "deviceType", "enum", title: "Device Type", options: ["Humidifier", "Dehumidifier"], required: true
  	}

	section("Target humidity range") {
		input "highPoint", "number", title: "Max humidity", required: true
        input "lowPoint", "number", title: "Min humidity", required: true
	}
    
    section {
        input(name: "notify", title: "Notify when device turned on or off?", type: "bool", required: false, defaultValue: false)
    }
}

def installed() {
	subscribe(humiditySensor, "humidity", deviceControl)
}

def updated() {
	unsubscribe()
	subscribe(humiditySensor, "humidity", deviceControl)
}

def deviceControl(evt) {
	def highPointValue = highPoint as int
    def lowPointValue = lowPoint as int
    def switchValue = deviceSwitch.currentSwitch
    def currentHumidityValue = humiditySensor.currentState("humidity").value.toInteger()
    
	log.debug "Current humidity is ${currentHumidityValue}, switch is ${switchValue}"
    log.debug "Device type is ${deviceType}, highPoint: ${highPointValue}, lowPoint: ${lowPointValue}"
	if(deviceType == "Humidifier") {
    	if('on' == switchValue && currentHumidityValue >= highPointValue) {
        	deviceSwitch.off()
            sendMsg("off", currentHumidityValue)
        } else if('off' == switchValue && currentHumidityValue <= lowPointValue) {
        	deviceSwitch.on()
            sendMsg("on", currentHumidityValue)
        }
    } else if (deviceType == "Dehumidifier") {
		if('off' == switchValue && currentHumidityValue >= highPointValue) {
        	deviceSwitch.on()
            sendMsg("on", currentHumidityValue)
        } else if('on' == switchValue && currentHumidityValue <= lowPointValue) {
        	deviceSwitch.off()
            sendMsg("off", currentHumidityValue)
        }
    }
}

def sendMsg(newState, currentHumidityValue) {
	if(notify != null && true == notify) {
		sendPush("Humidity has reached ${currentHumidityValue}%, turned ${newState} ${deviceType}")
    }
}