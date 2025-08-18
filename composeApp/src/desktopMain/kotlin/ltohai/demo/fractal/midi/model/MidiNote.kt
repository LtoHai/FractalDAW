package ltohai.demo.fractal.midi.model

data class MidiNote(
    val channel: MidiChannel,
    val pitch: MidiPitch,
    val velocity: MidiVelocity,
    val startTick: Long,
    val durationTicks: Long
) {
    /**
     * 计算并返回音符结束的绝对时间 (以 tick 为单位)。
     */
    val endTick: Long
        get() = startTick + durationTicks

    /**
     * 将此 MidiNote 对象转换回一对原始的 NoteOn 和 NoteOff 事件。
     *
     * @return 一个包含 NoteOn 和 NoteOff 事件的 Pair。
     */
    fun toMidiEvents(): Pair<MidiEvent.NoteOn, MidiEvent.NoteOff> {
        val noteOn = MidiEvent.NoteOn(channel, pitch, velocity, startTick)
        val noteOff = MidiEvent.NoteOff(
            channel = channel,
            pitch = pitch,
            velocity = MidiVelocity(0),
            absoluteTick = endTick
        )
        return noteOn to noteOff
    }
}