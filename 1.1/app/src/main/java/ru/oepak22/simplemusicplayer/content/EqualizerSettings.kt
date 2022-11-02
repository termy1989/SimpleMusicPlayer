package ru.oepak22.simplemusicplayer.content

import java.io.Serializable

// класс настроек эквалайзера
class EqualizerSettings : Serializable {
    var isEqualizerEnabled = false                          // h
    var seekbarpos: IntArray = IntArray(5)             //
    var presetPos = 0                                       // g
    //var bassStrength: Short = -1                            // g
    //var reverbPreset: Short = -1                            // g
    var bassStrength = 0                            // g
    var reverbPreset = 0                            // g
}