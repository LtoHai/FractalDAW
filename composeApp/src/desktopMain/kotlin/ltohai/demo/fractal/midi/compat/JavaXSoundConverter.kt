package ltohai.demo.fractal.midi.compat

import ltohai.demo.fractal.midi.model.MidiDivision
import ltohai.demo.fractal.midi.model.MidiEvent
import ltohai.demo.fractal.midi.model.MidiFile
import ltohai.demo.fractal.midi.model.SmpteFramesPerSecond
import javax.sound.midi.MetaMessage
import javax.sound.midi.Sequence
import javax.sound.midi.ShortMessage
import javax.sound.midi.SysexMessage

/**
 * 将我们自定义的、类型安全的 MidiFile 模型转换为 JavaX Sound API 可以理解的 Sequence 对象。
 *
 * @return 一个标准的 javax.sound.midi.Sequence 实例。
 * @throws javax.sound.midi.InvalidMidiDataException 如果 MIDI 数据无效。
 */
fun MidiFile.toJavaxSequence(): Sequence {
    // 1. 确定 division type 和 resolution
    val divisionType: Float
    val resolution: Int

    when (val div = this.header.division) {
        is MidiDivision.TicksPerQuarterNote -> {
            divisionType = Sequence.PPQ
            resolution = div.ticksPerQuarterNote
        }

        is MidiDivision.SmpteTimecode -> {
            divisionType = when (div.framesPerSecond) {
                SmpteFramesPerSecond.FPS_24 -> Sequence.SMPTE_24
                SmpteFramesPerSecond.FPS_25 -> Sequence.SMPTE_25
                SmpteFramesPerSecond.FPS_30_DROP -> Sequence.SMPTE_30DROP
                SmpteFramesPerSecond.FPS_30 -> Sequence.SMPTE_30
            }
            resolution = div.ticksPerFrame
        }
    }

    // 2. 创建 JavaX Sequence 对象
    val javaSequence = Sequence(divisionType, resolution)

    // 3. 遍历我们的轨道，并将其中的事件添加到 JavaX Track 中
    this.tracks.forEach { customTrack ->
        val javaTrack = javaSequence.createTrack()
        customTrack.events.forEach { customEvent ->
            val javaEvent = customEvent.toJavaxMidiEvent()
            javaTrack.add(javaEvent)
        }
    }

    return javaSequence
}

/**
 * 将我们自定义的 MidiEvent 转换为 JavaX Sound API 的 MidiEvent。
 */
private fun MidiEvent.toJavaxMidiEvent(): javax.sound.midi.MidiEvent {
    val message = when (this) {
        // --- Channel Events ---
        is MidiEvent.NoteOn -> ShortMessage(
            ShortMessage.NOTE_ON,
            this.channel.value,
            this.pitch.value,
            this.velocity.value
        )

        is MidiEvent.NoteOff -> ShortMessage(
            ShortMessage.NOTE_OFF,
            this.channel.value,
            this.pitch.value,
            this.velocity.value
        )

        is MidiEvent.ControlChange -> ShortMessage(
            ShortMessage.CONTROL_CHANGE,
            this.channel.value,
            this.controllerNumber.value,
            this.controllerValue.value
        )

        is MidiEvent.ProgramChange -> ShortMessage(
            ShortMessage.PROGRAM_CHANGE,
            this.channel.value,
            this.program.value,
            0
        )

        // Fallback for other channel events
        is MidiEvent.GenericChannelEvent -> ShortMessage(
            this.status,
            this.data1,
            this.data2 ?: 0 // data2 is nullable, provide a default value
        )

        // --- System Events ---
        is MidiEvent.SystemEvent -> SysexMessage(
            this.status,
            this.data.bytes,
            this.data.bytes.size
        )

        // --- Meta Events ---
        is MidiEvent.Meta.TrackName -> MetaMessage(
            0x03, // Meta-event type for Track Name
            this.name.toByteArray(),
            this.name.toByteArray().size
        )

        is MidiEvent.Meta.Tempo -> {
            val tempoBytes = byteArrayOf(
                (this.microsecondsPerQuarterNote shr 16 and 0xFF).toByte(),
                (this.microsecondsPerQuarterNote shr 8 and 0xFF).toByte(),
                (this.microsecondsPerQuarterNote and 0xFF).toByte()
            )
            MetaMessage(0x51, tempoBytes, tempoBytes.size)
        }

        is MidiEvent.Meta.TimeSignature -> {
            val timeSigBytes = byteArrayOf(
                this.numerator.toByte(),
                this.denominator.toByte(), // This assumes 'denominator' correctly holds the exponent
                this.clocksPerTick.toByte(),
                this.notesPer24Clocks.toByte()
            )
            MetaMessage(0x58, timeSigBytes, timeSigBytes.size)
        }

        // Fallback for other meta events
        is MidiEvent.Meta.GenericMetaEvent -> MetaMessage(
            this.type,
            this.data.bytes, // Assuming MidiData is a value class wrapping a ByteArray
            this.data.bytes.size
        )

        is MidiEvent.Meta.EndOfTrack -> MetaMessage(0x2F, byteArrayOf(), 0)
    }
    return javax.sound.midi.MidiEvent(message, this.absoluteTick)
}