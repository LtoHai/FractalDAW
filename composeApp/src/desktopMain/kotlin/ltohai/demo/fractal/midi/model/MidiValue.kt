package ltohai.demo.fractal.midi.model

import ltohai.demo.fractal.midi.constant.*

@JvmInline
value class MidiChannel(val value: Int) {
    init {
        require(value in MIN_CHANNEL..MAX_CHANNEL) {
            "Channel must be between $MIN_CHANNEL and $MAX_CHANNEL, but was $value"
        }
    }
}

@JvmInline
value class MidiPitch(val value: Int) {
    init {
        require(value in MIN_PITCH..MAX_PITCH) {
            "Pitch must be between $MIN_PITCH and $MAX_PITCH, but was $value"
        }
    }
}

@JvmInline
value class MidiVelocity(val value: Int) {
    init {
        require(value in MIN_VELOCITY..MAX_VELOCITY) {
            "Velocity must be between $MIN_VELOCITY and $MAX_VELOCITY, but was $value"
        }
    }
}

@JvmInline
value class MidiProgram(val value: Int) {
    init {
        require(value in MIN_PROGRAM..MAX_PROGRAM) {
            "Program must be between $MIN_PROGRAM and $MAX_PROGRAM, but was $value"
        }
    }
}

@JvmInline
value class MidiControllerNumber(val value: Int) {
    init {
        require(value in MIN_CONTROLLER_NUMBER..MAX_CONTROLLER_NUMBER) {
            "Controller number must be between $MIN_CONTROLLER_NUMBER and $MAX_CONTROLLER_NUMBER, but was $value"
        }
    }
}

@JvmInline
value class MidiControllerValue(val value: Int) {
    init {
        require(value in MIN_CONTROLLER_VALUE..MAX_CONTROLLER_VALUE) {
            "Controller value must be between $MIN_CONTROLLER_VALUE and $MAX_CONTROLLER_VALUE, but was $value"
        }
    }
}

@JvmInline
value class MidiData(val bytes: ByteArray)