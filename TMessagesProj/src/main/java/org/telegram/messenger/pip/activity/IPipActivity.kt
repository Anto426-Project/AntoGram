package org.telegram.messenger.pip.activity

import org.telegram.messenger.pip.PipActivityController

interface IPipActivity {
    fun getPipController(): PipActivityController?
}
