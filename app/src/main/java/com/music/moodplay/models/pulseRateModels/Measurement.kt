package com.music.moodplay.models.pulseRateModels

import java.util.*

//Measurement class

class Measurement<T>(val timestamp: Date, val measurement: T)


//timestamp member stores the date and time at which the heart rate was measured
//measurement member stores the data that was measured