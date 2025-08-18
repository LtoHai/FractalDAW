package ltohai.demo.fractal.midi.model

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MidiTrackEngineTest {

    private val channel = MidiChannel(0)
    private val pitch = MidiPitch(60) // C4
    private val velocity = MidiVelocity(100)

    // region Event Tests

    @Test
    fun `addEvents to an empty track should result in a track with those events and an EndOfTrack`() {
        val emptyTrack = BasicMidiTrack()
        val event1 = MidiEvent.NoteOn(channel, pitch, velocity, 100)

        val newTrack = MidiTrackEngine.addEvents(emptyTrack, listOf(event1))

        assertTrackContainsExactly(newTrack, event1)
    }

    @Test
    fun `addEvents should correctly sort the new events`() {
        val event1 = MidiEvent.NoteOn(channel, pitch, velocity, 100)
        val trackWithOneEvent = MidiTrackEngine.addEvents(BasicMidiTrack(), listOf(event1))

        val event0 = MidiEvent.NoteOff(channel, pitch, velocity, 0)
        val newTrack = MidiTrackEngine.addEvents(trackWithOneEvent, listOf(event0))

        assertTrackContainsExactly(newTrack, event0, event1)
    }

    @Test
    fun `removeEvents should remove the specified event and update EndOfTrack`() {
        val event1 = MidiEvent.NoteOn(channel, pitch, velocity, 100)
        val event2 = MidiEvent.NoteOn(channel, pitch, velocity, 200)
        val initialTrack = MidiTrackEngine.addEvents(BasicMidiTrack(), listOf(event1, event2))

        val newTrack = MidiTrackEngine.removeEvents(initialTrack, listOf(event2))

        assertTrackContainsExactly(newTrack, event1)
    }

    @Test
    fun `removeEvents from a track until it's empty should result in a track with only EndOfTrack at tick 0`() {
        val event1 = MidiEvent.NoteOn(channel, pitch, velocity, 100)
        val initialTrack = MidiTrackEngine.addEvents(BasicMidiTrack(), listOf(event1))

        val newTrack = MidiTrackEngine.removeEvents(initialTrack, listOf(event1))

        assertTrackContainsExactly(newTrack) // Asserting an empty track
    }

    // endregion

    // region Note Tests

    @Test
    fun `addNotes should add both NoteOn and NoteOff events`() {
        val note = MidiNote(channel, pitch, velocity, 100, 50)
        val (noteOn, noteOff) = note.toMidiEvents()

        val newTrack = MidiTrackEngine.addNotes(BasicMidiTrack(), listOf(note))

        // The engine sorts events by tick, so noteOn will appear before noteOff.
        assertTrackContainsExactly(newTrack, noteOn, noteOff)
    }

    @Test
    fun `removeNotes should remove both NoteOn and NoteOff events`() {
        val note1 = MidiNote(channel, pitch, velocity, 0, 100)
        val note2 = MidiNote(channel, MidiPitch(72), velocity, 200, 100)
        val (note2On, note2Off) = note2.toMidiEvents()
        val initialTrack = MidiTrackEngine.addNotes(BasicMidiTrack(), listOf(note1, note2))

        // Sanity check
        assertEquals(5, initialTrack.events.size)

        val newTrack = MidiTrackEngine.removeNotes(initialTrack, listOf(note1))

        // The events from note1 should be gone, leaving only the events from note2.
        assertTrackContainsExactly(newTrack, note2On, note2Off)
    }

    // endregion

    // region Edge Case Tests

    @Test
    fun `addEvents with an empty list to a non-empty track should not change it`() {
        val event1 = MidiEvent.NoteOn(channel, pitch, velocity, 100)
        val initialTrack = MidiTrackEngine.addEvents(BasicMidiTrack(), listOf(event1))
        val newTrack = MidiTrackEngine.addEvents(initialTrack, emptyList())
        assertEquals(initialTrack, newTrack)
    }

    @Test
    fun `addEvents with an empty list to an empty track should create a valid track with only EndOfTrack`() {
        val emptyTrack = BasicMidiTrack()
        val newTrack = MidiTrackEngine.addEvents(emptyTrack, emptyList())
        assertTrackContainsExactly(newTrack)
    }

    @Test
    fun `removeEvents with an empty list should not change a track`() {
        val event1 = MidiEvent.NoteOn(channel, pitch, velocity, 100)
        val initialTrack = MidiTrackEngine.addEvents(BasicMidiTrack(), listOf(event1))
        val newTrack = MidiTrackEngine.removeEvents(initialTrack, emptyList())
        assertEquals(initialTrack, newTrack)
    }

    // endregion

    // region Custom Assertions

    /**
     * A custom assertion to verify the complete state of a MidiTrack.
     * It checks that the track contains exactly the expected events in the correct order,
     * followed by a valid EndOfTrack event with the correct final tick.
     *
     * @param track The track to inspect.
     * @param expectedEvents The sequence of events the track should contain, excluding the EndOfTrack event.
     */
    private fun assertTrackContainsExactly(track: BasicMidiTrack, vararg expectedEvents: MidiEvent) {
        val expectedEventsList = expectedEvents.toList()
        val actualEvents = track.events.dropLast(1)

        assertContentEquals(
            expectedEventsList,
            actualEvents,
            "The events in the track do not match the expected events."
        )

        val lastEvent = track.events.last()
        assertIs<MidiEvent.Meta.EndOfTrack>(lastEvent, "The last event in the track must be EndOfTrack.")

        val expectedLastTick = expectedEvents.maxOfOrNull { it.absoluteTick } ?: 0L
        assertEquals(expectedLastTick, lastEvent.absoluteTick, "The EndOfTrack event has an incorrect tick value.")
    }
    // endregion
}