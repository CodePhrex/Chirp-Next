package com.codephrex.chirp.orb

enum class OrbState { IDLE, TRANSMITTING, RECEIVING, CONNECTED, TRANSITION, BUSY, DND, OFFLINE, ERROR }
enum class Haptic { NONE, ACTIVATE, RECEIVE, CONFIRM, RELEASE, WARNING, ERROR }
data class OrbModel(val state: OrbState = OrbState.OFFLINE, val connected: Boolean = false,
    val permitted: Boolean = false, val held: Boolean = false, val session: Long = 0,
    val haptics: Boolean = true)
sealed interface OrbEvent {
    data class Connection(val ready: Boolean): OrbEvent
    data class Permission(val granted: Boolean): OrbEvent
    data object Press: OrbEvent
    data object Release: OrbEvent
    data object Cancel: OrbEvent
    data class FloorGranted(val session: Long): OrbEvent
    data object Incoming: OrbEvent
    data object Busy: OrbEvent
    data object Dnd: OrbEvent
    data object Failure: OrbEvent
    data object Settled: OrbEvent
}
data class OrbResult(val model: OrbModel, val requestFloor: Boolean = false,
    val stopCapture: Boolean = false, val startCapture: Boolean = false,
    val haptic: Haptic = Haptic.NONE)
object OrbReducer {
    fun reduce(m: OrbModel, e: OrbEvent): OrbResult {
        val ready = if (m.connected) OrbState.CONNECTED else OrbState.OFFLINE
        fun result(next: OrbModel, request: Boolean = false, start: Boolean = false,
                   stop: Boolean = false, pulse: Haptic = Haptic.NONE) =
            OrbResult(next, request, stop, start, if (m.haptics && next.state != m.state) pulse else Haptic.NONE)
        fun end(state: OrbState, pulse: Haptic = Haptic.NONE) =
            result(m.copy(state = state, held = false, session = m.session + 1), stop = m.held || m.state == OrbState.TRANSMITTING, pulse = pulse)
        return when(e) {
            is OrbEvent.Connection -> if (!e.ready) {
                val r = end(OrbState.OFFLINE)
                r.copy(model = r.model.copy(connected = false))
            } else if (m.connected) result(m) else result(m.copy(connected = true, state = OrbState.CONNECTED), pulse = Haptic.CONFIRM)
            is OrbEvent.Permission -> if (!e.granted) {
                val r = end(ready)
                r.copy(model = r.model.copy(permitted = false))
            } else result(m.copy(permitted = true))
            OrbEvent.Press -> if (m.connected && m.permitted && !m.held && m.state in setOf(OrbState.IDLE, OrbState.CONNECTED, OrbState.TRANSITION))
                result(m.copy(held = true, session = m.session + 1), request = true) else result(m)
            is OrbEvent.FloorGranted -> if (m.held && m.connected && m.permitted && e.session == m.session && m.state != OrbState.TRANSMITTING)
                result(m.copy(state = OrbState.TRANSMITTING), start = true, pulse = Haptic.ACTIVATE) else result(m)
            OrbEvent.Release, OrbEvent.Cancel -> if (m.held) end(if(m.connected) OrbState.TRANSITION else OrbState.OFFLINE, Haptic.RELEASE) else result(m)
            OrbEvent.Incoming -> if (m.connected && !m.held && m.state != OrbState.DND) result(m.copy(state = OrbState.RECEIVING), pulse = Haptic.RECEIVE) else result(m)
            OrbEvent.Busy -> if(m.connected) end(OrbState.BUSY, Haptic.WARNING) else result(m)
            OrbEvent.Dnd -> end(OrbState.DND)
            OrbEvent.Failure -> end(OrbState.ERROR, Haptic.ERROR)
            OrbEvent.Settled -> if (!m.held && m.state != OrbState.DND) result(m.copy(state = if(m.connected) OrbState.IDLE else OrbState.OFFLINE)) else result(m)
        }
    }
}
