package ltohai.demo.fractal.midi.engine

import ltohai.demo.fractal.midi.compat.toJavaxSequence
import ltohai.demo.fractal.midi.model.MidiFile
import javax.sound.midi.MidiSystem
import javax.sound.midi.MidiUnavailableException
import javax.sound.midi.Sequencer
import javax.sound.midi.Synthesizer

/**
 * 一个封装了 JavaX Sound Sequencer 的 MIDI 播放器类。
 *
 * 这个类处理了 Sequencer 和 Synthesizer 的初始化、连接和资源释放，
 * 为上层应用提供了简洁的播放控制接口。
 */
class MidiPlayer {
    private var sequencer: Sequencer? = null
    private var synthesizer: Synthesizer? = null

    val isReady: Boolean
        get() = sequencer?.isOpen == true

    init {
        try {
            // 1. 获取并打开 Sequencer
            sequencer = MidiSystem.getSequencer(false).also { it.open() }

            // 2. 获取并打开默认的 Synthesizer
            synthesizer = MidiSystem.getSynthesizer().also { it.open() }

            // 3. 将 Sequencer 的输出连接到 Synthesizer 的输入
            sequencer?.transmitter?.receiver = synthesizer?.receiver

        } catch (e: MidiUnavailableException) {
            println("MIDI 设备不可用: ${e.message}")
            close() // 如果初始化失败，清理所有资源
        }
    }

    fun play(midiFile: MidiFile) {
        if (!isReady) {
            println("播放器未准备好。")
            return
        }
        sequencer?.let {
            if (it.isRunning) {
                it.stop()
            }
            it.sequence = midiFile.toJavaxSequence()
            it.tickPosition = 0
            it.start()
            println("开始播放...")
        }
    }

    fun stop() {
        sequencer?.stop()
        println("播放停止。")
    }

    fun close() {
        sequencer?.close()
        synthesizer?.close()
    }
}