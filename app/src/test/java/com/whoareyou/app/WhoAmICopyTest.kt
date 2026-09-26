package com.whoareyou.app

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WhoAmICopyTest {
    @Test
    fun `known trait IDs resolve to a label resource`() {
        assertNotNull(WhoAmICopy.traitLabelResource("social_energy"))
        assertNotNull(WhoAmICopy.traitLabelResource("curiosity"))
        assertNotNull(WhoAmICopy.traitLabelResource("boundaries"))
    }

    @Test
    fun `unknown trait IDs do not become display labels`() {
        assertNull(WhoAmICopy.traitLabelResource("internal_secret_trait"))
    }
}
