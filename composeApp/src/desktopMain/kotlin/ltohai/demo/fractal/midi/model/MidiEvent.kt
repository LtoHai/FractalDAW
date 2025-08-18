package ltohai.demo.fractal.midi.model

import ltohai.demo.fractal.midi.constant.MINUTE_TO_MICROSECONDS

sealed class MidiEvent(open val absoluteTick: Long) {
    data class NoteOn(
        val channel: MidiChannel,
        val pitch: MidiPitch,
        val velocity: MidiVelocity,
        override val absoluteTick: Long
    ) : MidiEvent(absoluteTick)

    data class NoteOff(
        val channel: MidiChannel,
        val pitch: MidiPitch,
        val velocity: MidiVelocity,
        override val absoluteTick: Long
    ) : MidiEvent(absoluteTick)

    data class ProgramChange(
        val channel: MidiChannel,
        val program: MidiProgram,
        override val absoluteTick: Long
    ) : MidiEvent(absoluteTick)

    data class ControlChange(
        val channel: MidiChannel,
        val controllerNumber: MidiControllerNumber,
        val controllerValue: MidiControllerValue,
        override val absoluteTick: Long
    ) : MidiEvent(absoluteTick)

    data class GenericChannelEvent(
        val status: Int,
        val data1: Int,
        val data2: Int?,
        override val absoluteTick: Long
    ) : MidiEvent(absoluteTick)

    data class SystemEvent(
        val status: Int,
        val data: MidiData, // 使用 value class 包装
        override val absoluteTick: Long
    ) : MidiEvent(absoluteTick)

    sealed class Meta(override val absoluteTick: Long) : MidiEvent(absoluteTick) {
        data class Tempo(
            val microsecondsPerQuarterNote: Int,
            override val absoluteTick: Long
        ) : Meta(absoluteTick) {
            // actually quarter notes per minute
            val bpm: Double get() = MINUTE_TO_MICROSECONDS / microsecondsPerQuarterNote
        }

        data class TrackName(
            val name: String,
            override val absoluteTick: Long
        ) : Meta(absoluteTick)

        data class EndOfTrack(override val absoluteTick: Long) : Meta(absoluteTick)

        data class GenericMetaEvent(
            val type: Int,
            val data: MidiData, // 使用 value class 包装
            override val absoluteTick: Long
        ) : Meta(absoluteTick)
    }
}
