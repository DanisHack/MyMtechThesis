#include "Timer.h"
#include "SenseToRadio.h"
#include "printf.h"


module SenseToRadioC 
{

	uses {
		interface Boot;
		interface Leds;
		interface Timer<TMilli> as WakeupTimer;

		interface Read<uint16_t> as ReadTemp;
		interface Read<uint16_t> as ReadHumidity;
		
		interface Packet;
		interface AMPacket;
		interface AMSend;
		interface SplitControl as AMControl;
		
	     }
}

implementation 
{

  		/* Define Variables and Constants */

  		#define SAMPLING_FREQUENCY 500
  		uint16_t ChannelNo = 0;
		uint16_t counter = 0 ;
		
		/* Global variables to hold sensor readings */

		uint16_t TEMPdata, Hdata;
	        float temp,humidity; 	
		uint8_t len;
		bool busy = FALSE;  /* used to keep track if radio is busy */
		message_t pkt;
		void task getData();
		
		/* Initializations  */
		
		event void Boot.booted() {
			call WakeupTimer.startPeriodic(SAMPLING_FREQUENCY);
			call AMControl.start();
		}
			
		/* Event: WakeupTimer fired */
		event void WakeupTimer.fired()
		{
			call Leds.led0On();
			
			ChannelNo = 0;
			post getData();
			
			counter++;
			printf("Wakeup Counter: %u\n", counter);
		}
		
	        event void AMControl.startDone(error_t err) {
		}
		
		event void AMControl.stopDone(error_t err) {
		}
		
		event void AMSend.sendDone(message_t* msg, error_t error) {
			if(&pkt == msg) {
			busy = FALSE;
			call Leds.led2Off();
			printf("Wakeup Counter after packet Tx: %u\n", counter);
			printf("Sensor Readings: TEMPERATURE  HUMIDITY\n");
			printf("The values are: %4u %4u\n\n", TEMPdata, Hdata);
			}
		}
		
		void task getData() 
		{
		  call ReadTemp.read();
		}
	

		event void ReadTemp.readDone(error_t result, uint16_t data) {
			SenseToRadioMsg* ppkt = (SenseToRadioMsg*) (call Packet.getPayload(&pkt,len));
			ppkt->nodeid = TOS_NODE_ID;
			ppkt->counter = counter;
			temp = -39.60 + 0.01* (float)data;
			ppkt->data[ChannelNo++] = temp;
			TEMPdata = temp;
			call ReadHumidity.read();
		}
	
		
		event void ReadHumidity.readDone(error_t result, uint16_t data) {
			SenseToRadioMsg* ppkt = (SenseToRadioMsg*) (call Packet.getPayload(&pkt,len));
			humidity = -4.0 + 0.0405*(float) data + (-2.8*10/1000000)*(float) (data*data);	
			ppkt->data[ChannelNo++] = humidity;
			Hdata = humidity;
			call Leds.led0Off();
			printf("Time elapsed since Wakeup Timer fired:%lu ms\n",
				(call WakeupTimer.getNow())-(call WakeupTimer.gett0()));
			
			if(!busy) {
				if(call AMSend.send(AM_BROADCAST_ADDR, &pkt, sizeof(SenseToRadioMsg)) == SUCCESS) {
					call Leds.led2On();
					busy= TRUE;
				}
			}
		}
		
		
	
			
}			
