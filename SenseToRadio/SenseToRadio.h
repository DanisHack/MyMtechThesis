#ifndef SENSETORADIO_H
#define SENSETORADIO_H

enum {
	AM_SENSETORADIOMSG = 6,
	BUFFER_SIZE = 8,
};

typedef nx_struct SenseToRadioMsg {
	nx_uint16_t nodeid;
	nx_uint16_t counter;
	nx_uint16_t data[BUFFER_SIZE];
} SenseToRadioMsg;

#endif
