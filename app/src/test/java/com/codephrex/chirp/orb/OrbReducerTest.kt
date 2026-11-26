package com.codephrex.chirp.orb
import org.junit.Assert.*
import org.junit.Test

class OrbReducerTest {
    private val ready = OrbModel(OrbState.CONNECTED, connected = true, permitted = true)
    @Test fun pressRequiresPermissionAndConnection() {
        assertFalse(OrbReducer.reduce(ready.copy(permitted=false), OrbEvent.Press).requestFloor)
        assertFalse(OrbReducer.reduce(ready.copy(connected=false), OrbEvent.Press).requestFloor)
    }
    @Test fun floorGrantRequired() {
        val p = OrbReducer.reduce(ready, OrbEvent.Press)
        assertTrue(p.requestFloor)
        assertFalse(p.startCapture)
        val g = OrbReducer.reduce(p.model, OrbEvent.FloorGranted(p.model.session))
        assertTrue(g.startCapture)
        assertEquals(OrbState.TRANSMITTING, g.model.state)
        assertEquals(Haptic.ACTIVATE, g.haptic)
        assertFalse(OrbReducer.reduce(g.model, OrbEvent.FloorGranted(p.model.session)).startCapture)
    }
    @Test fun cancelRejectsStaleGrant() {
        val p = OrbReducer.reduce(ready, OrbEvent.Press).model
        val c = OrbReducer.reduce(p, OrbEvent.Cancel)
        assertTrue(c.stopCapture)
        assertFalse(OrbReducer.reduce(c.model, OrbEvent.FloorGranted(p.session)).startCapture)
    }
    @Test fun disconnectAndRevocationStopCapture() {
        val p = OrbReducer.reduce(ready, OrbEvent.Press).model
        val g = OrbReducer.reduce(p, OrbEvent.FloorGranted(p.session)).model
        assertTrue(OrbReducer.reduce(g, OrbEvent.Connection(false)).stopCapture)
        assertTrue(OrbReducer.reduce(g, OrbEvent.Permission(false)).stopCapture)
    }
    @Test fun hapticsCanBeDisabled() {
        assertEquals(Haptic.NONE, OrbReducer.reduce(ready.copy(haptics=false), OrbEvent.Incoming).haptic)
    }
    @Test fun allStatesRepresented() { assertEquals(9, OrbState.entries.size) }
}
