package ltohai.demo.fractal.midi.model

data class BasicMidiTrack(
    val events: List<MidiEvent> = emptyList()
)

object MidiTrackEngine {
    fun addEvents(track: BasicMidiTrack, eventsToAdd: Collection<MidiEvent>): BasicMidiTrack {
        val newEvents = eventsToAdd.filter { it !is MidiEvent.Meta.EndOfTrack }
        val currentEvents = track.events.filter { it !is MidiEvent.Meta.EndOfTrack }
        val combinedEvents = (currentEvents + newEvents).sortedBy { it.absoluteTick }
        return track.copy(events = ensureEndOfTrack(combinedEvents))
    }

    fun removeEvents(track: BasicMidiTrack, eventsToRemove: Collection<MidiEvent>): BasicMidiTrack {
        val eventsToRemoveSet = eventsToRemove.toSet()
        val currentEvents = track.events.filter { it !is MidiEvent.Meta.EndOfTrack }
        val remainingEvents = currentEvents.filter { it !in eventsToRemoveSet }
        return track.copy(events = ensureEndOfTrack(remainingEvents))
    }

    fun addNotes(track: BasicMidiTrack, notesToAdd: Collection<MidiNote>): BasicMidiTrack {
        val eventsToAdd = notesToAdd.flatMap {
            val (noteOn, noteOff) = it.toMidiEvents()
            listOf(noteOn, noteOff)
        }
        return addEvents(track, eventsToAdd)
    }

    fun removeNotes(track: BasicMidiTrack, notesToRemove: Collection<MidiNote>): BasicMidiTrack {
        val eventsToRemove = notesToRemove.flatMap {
            val (noteOn, noteOff) = it.toMidiEvents()
            listOf(noteOn, noteOff)
        }
        return removeEvents(track, eventsToRemove)
    }

    private fun ensureEndOfTrack(events: List<MidiEvent>): List<MidiEvent> {
        val lastTick = events.maxOfOrNull { it.absoluteTick } ?: 0L
        return events + MidiEvent.Meta.EndOfTrack(lastTick)
    }
}