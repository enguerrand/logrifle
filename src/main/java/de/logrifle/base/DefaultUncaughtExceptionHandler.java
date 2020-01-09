/*
 *  Copyright 2020, Enguerrand de Rochefort
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

package de.logrifle.base;

import com.googlecode.lanterna.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Nullable
    private final Screen screen;

    public DefaultUncaughtExceptionHandler(@Nullable Screen screen) {
        this.screen = screen;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            if (screen != null) {
                screen.stopScreen();
            }
            printCrashInfo(thread, throwable);
        } catch (IOException e) {
            printCrashInfo(thread, throwable);
            System.err.print("Additionally, the following error occurred while trying to stop the current screen: ");
            e.printStackTrace();
        }
        System.exit(-1);
    }

    private void printCrashInfo(Thread thread, Throwable throwable) {
        System.err.println("Sorry, logrifle crashed.");
        System.err.println("Please help improving logrifle and send the information below to support@logrifle.de");
        System.err.println("Uncaught Exception in thread " + thread.getName() + " [" + thread.getId() + "]:");
        throwable.printStackTrace();
    }
}
