package ltohai.demo.fractal.midi.model

/**
 * 代表一个完整的标准 MIDI 文件 (SMF)，由一个文件头和多个轨道组成。
 *
 * @property header MIDI 文件头信息。
 * @property tracks 文件中包含的轨道列表。
 */
data class MidiFile(
    val header: MidiHeader,
    val tracks: List<BasicMidiTrack>
) {
    /**
     * 提供一个便捷的构造函数，允许通过独立的参数创建 MidiFile，
     * 内部会自动创建 MidiHeader。
     */
    constructor(format: Int, division: MidiDivision, tracks: List<BasicMidiTrack>) : this(
        header = MidiHeader(format, tracks.size, division),
        tracks = tracks
    )

    init {
        // 确保文件头中记录的轨道数与实际的轨道列表大小一致
        require(header.trackCount == tracks.size) {
            "Header track count (${header.trackCount}) does not match actual track list size (${tracks.size})."
        }
    }
}

/**
 * 代表 MIDI 文件的文件头 (Header Chunk)。
 *
 * @property format MIDI 文件格式 (0, 1, 或 2)。
 * @property trackCount 文件中轨道的数量。
 * @property division 定义了文件中 tick 的时间单位。
 */
data class MidiHeader(
    val format: Int,
    val trackCount: Int,
    val division: MidiDivision
) {
    init {
        require(format in 0..2) { "MIDI file format must be 0, 1, or 2, but was $format" }
        require(trackCount >= 0) { "Track count cannot be negative, but was $trackCount" }
    }

    /**
     * 获取此 MIDI 文件的时序分辨率，即一个时间单位被分割成的份数。
     */
    val resolution: Int
        get() = when (division) {
            is MidiDivision.TicksPerQuarterNote -> division.ticksPerQuarterNote
            is MidiDivision.SmpteTimecode -> division.ticksPerFrame
        }
}

/**
 * 定义 MIDI 文件的时间基准，即 tick 的含义。
 * 这是一个密封类，因为时间基准只有两种明确的类型。
 */
sealed class MidiDivision {
    /**
     * 基于节拍的时间基准。
     *
     * @property ticksPerQuarterNote 每个四分音符对应的 tick 数。
     */
    data class TicksPerQuarterNote(val ticksPerQuarterNote: Int) : MidiDivision()

    /**
     * 基于 SMPTE 时间码的时间基准。
     *
     * @property framesPerSecond 每秒的帧数。
     * @property ticksPerFrame 每一帧包含的 tick 数量。
     */
    data class SmpteTimecode(val framesPerSecond: SmpteFramesPerSecond, val ticksPerFrame: Int) : MidiDivision()
}

/**
 * 标准的 SMPTE 每秒帧数 (FPS) 类型。
 * 在 MIDI 文件规范中，这些值通常以负数形式存储。
 */
enum class SmpteFramesPerSecond(val value: Int) {
    FPS_24(-24),
    FPS_25(-25),
    FPS_30_DROP(-29), // 对应 29.97 fps (30 Drop-Frame)
    FPS_30(-30);   // 对应 30 fps (Non-Drop)
}