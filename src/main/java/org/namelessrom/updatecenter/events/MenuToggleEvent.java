package org.namelessrom.updatecenter.events;

/**
 * Created by alex on 22.04.14.
 */
public class MenuToggleEvent {

    private final boolean isOpen;

    public MenuToggleEvent(final boolean isOpen) { this.isOpen = isOpen; }

    public boolean isOpen() { return isOpen; }

}
