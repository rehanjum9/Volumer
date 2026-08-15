package com.volumer.app

import org.junit.Assert.*
import org.junit.Test

class VolumePolicyTest {
 @Test fun crowdedAtThreshold(){ assertFalse(VolumePolicy.isCrowded(5,6)); assertTrue(VolumePolicy.isCrowded(6,6)) }
 @Test fun returnVolumeRestoresHigherSavedValue(){ assertEquals(70, VolumePolicy.returnVolume(70,45)) }
 @Test fun returnVolumeUsesFloorAfterPublicTransition(){ assertEquals(45, VolumePolicy.returnVolume(8,45)) }
}
