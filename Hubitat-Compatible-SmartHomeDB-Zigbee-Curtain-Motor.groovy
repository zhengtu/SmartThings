/**
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License * for the specific language governing permissions and limitations under the License. * 2017 */

metadata {
    definition(name:"Curtains", namespace:"SmartHomeDB", author:"SmartHomeDB: Ray Zheng") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch Level"
        capability "windowShade"
        capability "Switch"

        fingerprint(
          profileId:"0104",
          inClusters:"0000, 0001, 0005, 0004, 0102", 
          outClusters:"0019", 
          manufacturer:"SmartHomeDB",
          model:"093199ff04984948b4c78167c8e7f47e"
        }
    }

    command "levelOpenClose"
    
    preferences {
    		input name:"mode", type:"bool", title:"Set SmartHomeDB Curtain Direction", description:"Reverse Mode ON", required:true, 
          	displayDuringSetup:true
				}

    tiles(scale:2) {

        multiAttributeTile(name:"windowShade", type:"windowShade", width:6, height:4) {

            tileAttribute("device.windowShade", key:"PRIMARY_CONTROL") {

                attributeState("close", label:'closed', action:"windowShade.open", icon:"st.Home.home9", backgroundColor:"#BDC3C7", nextState:"motion")

                attributeState("open", label:'open', action:"windowShade.close", icon:"st.Home.home9", backgroundColor:"#2ECC71", nextState:"motion")

                attributeState("motion", label:'in motion', action:"", icon:"st.Home.home9", backgroundColor:"#3498DB")

            }

        }

        standardTile("switch", "device.switch") {

            state("on", label:'open', action:"switch.off", icon:"st.Home.home9", backgroundColor:"#2ECC71")

            state("off", label:'closed', action:"switch.on", icon:"st.Home.home9", backgroundColor:"#BDC3C7")

        }

        standardTile("open", "device.windowShade", width:2, height:2, inactiveLabel:false, decoration:"flat") {

            state("open", label:'open', action:"windowShade.open", icon:"st.contact.contact.open")

        }

        standardTile("close", "device.windowShade", width:2, height:2, inactiveLabel:false, decoration:"flat") {

            state("close", label:'close', action:"windowShade.close", icon:"st.contact.contact.closed")

        }

        standardTile("refresh", "command.refresh", width:2, height:2, inactiveLabel:false, decoration:"flat") {

            state "default", label:" ", action:"refresh.refresh", icon:"st.sonos.pause-btn"

        }

        main(["windowShade"])

        details(["windowShade", "open", "close", "refresh"])

    }

}



// Parse incoming device messages to generate events

def parse(String description) {

    def parseMap = zigbee.parseDescriptionAsMap(description)

    def event = zigbee.getEvent(description)

    log.debug(parseMap)

    try {

        if (parseMap.raw.startsWith("0104")) {

            log.debug "Curtain"

        }else if (parseMap.raw.endsWith("0007")) {

            log.debug "running…"

        }else if (parseMap.endpoint.endsWith("01")) {

            if (parseMap["cluster"] == "0102" && parseMap["attrId"] == "0008") {



                long theValue = Long.parseLong(parseMap["value"], 16)

                def eventStack = []

                log.debug(theValue)
                log.debug(mode)
             if (mode == true) {

                if (theValue > 95) {
                    log.debug "Just Closed"

                    eventStack.push(createEvent(name:"windowShade", value:"close"))

                }else if (theValue < 5) {

                   log.debug "Just Fully Open"

                    eventStack.push(createEvent(name:"windowShade", value:"open"))

                }else {
                    log.debug 'Motion'
                    eventStack.push(createEvent(name:"windowShade", value:"motion"))

                }

             }

             else {

               if (theValue < 5) {

                    log.debug "Just Fully Open mode null"

                    eventStack.push(createEvent(name:"windowShade", value:"open"))
                    
                }else if (theValue > 95) {

                    log.debug "Just Closed mode null"

                    eventStack.push(createEvent(name:"windowShade", value:"close"))


                }else {

                    log.debug 'In Motion'
                    eventStack.push(createEvent(name:"windowShade", value:"motion"))

                }

			}

                return eventStack

            }

        }else {

            log.debug "Unhandled Event - description:${description}, parseMap:${parseMap}, event:${event}"

        }



        if (event["name"] == "switch") {

        	log.debug("add event: ${event}")

            return createEvent(name:"switch", value:event["value"])

        }

    }catch (Exception e) {

        log.warn e

    }

}



def close() {

    log.debug "Set Close"

	if (mode == true) {

    zigbee.command(0x0102, 0x00)

    }else {

    zigbee.command(0x0102, 0x01)

    }

}



def open() {

    log.debug "Set Open"

	if (mode == true) {

    zigbee.command(0x0102, 0x01)

    }else {

    zigbee.command(0x0102, 0x00)

    }

}



def off() {

    log.debug "off()"

	if (mode == true) {

    zigbee.command(0x0102, 0x00)

    }else {

    zigbee.command(0x0102, 0x01)

    }

}



def on() {

    log.debug "on()"

	if (mode == true) {

    zigbee.command(0x0102, 0x01)

    }else {

    zigbee.command(0x0102, 0x00)

    }

}


def refresh() {

    log.debug "refresh()"

    zigbee.command(0x0102, 0x02)

    /*log.debug("Current Level: ${zigbee.readAttribute(0x000d, 0x0055)}")*/

}
