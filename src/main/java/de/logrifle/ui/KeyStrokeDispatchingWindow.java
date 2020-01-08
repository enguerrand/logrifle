/*
 *  Copyright 2019, Enguerrand de Rochefort
 *
 * This file is part of logrifle.
 *
 * logrifle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logrifle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with logrifle.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.logrifle.ui;

import com.googlecode.lanterna.gui2.BasePaneListener;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.input.KeyStroke;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is a hack around the fact that there is no TextBox Input Listener in Lanterna 3.0.1
 */
class KeyStrokeDispatchingWindow extends BasicWindow {
    private final Map<Interactable, Set<InteractableKeystrokeListener>> interactables = new HashMap<>();
    KeyStrokeDispatchingWindow(String title, Executor uiRescheduler) {
        super(title);
        addBasePaneListener(new BasePaneListener<Window>() {
            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
                Interactable focusedInteractable = getFocusedInteractable();
                Set<InteractableKeystrokeListener> listeners = interactables.get(focusedInteractable);
                if (listeners != null) {
                    // reschedule so that the event gets delivered after being delivered to the interactable
                    uiRescheduler.execute(() -> {
                        for (InteractableKeystrokeListener l : listeners) {
                            l.onKeyStroke(focusedInteractable, keyStroke);
                        }
                    });
                }
            }

            @Override
            public void onUnhandledInput(Window window, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
            }
        });
    }

    void addInteractableListener(InteractableKeystrokeListener listener) {
        interactables.computeIfAbsent(listener.getInteractable(), i -> new LinkedHashSet<>()).add(listener);
    }
}