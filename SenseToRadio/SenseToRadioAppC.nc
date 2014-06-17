#include "Timer.h"
#include "SenseToRadio.h"
#include "printf.h"

configuration SenseToRadioAppC 
{
}

implementation {

	components SenseToRadioC;
	components MainC;
	components LedsC;
	components new TimerMilliC() as WakeupTimer;
        components PrintfC;
        components SerialStartC;
        
	SenseToRadioC.Boot->MainC;
	SenseToRadioC.Leds->LedsC;
	SenseToRadioC.WakeupTimer->WakeupTimer;
	
	components new SensirionSht11C() as Sensor;
	SenseToRadioC.ReadTemp->Sensor.Temperature;
	SenseToRadioC.ReadHumidity->Sensor.Humidity;
	
	components ActiveMessageC;
	components new AMSenderC(AM_SENSETORADIOMSG);

	SenseToRadioC.Packet->AMSenderC;
	SenseToRadioC.AMPacket->AMSenderC;
	SenseToRadioC.AMSend->AMSenderC;
	SenseToRadioC.AMControl->ActiveMessageC;
        
        
}
