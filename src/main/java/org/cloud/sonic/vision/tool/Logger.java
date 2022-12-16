/*
 *   sonic-vision-core A vision library for sonic.
 *   Copyright (C) 2022  SonicCloudOrg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.cloud.sonic.vision.tool;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private SimpleDateFormat formatter;
    private boolean isShowLog;

    public Logger() {
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        isShowLog = true;
    }

    public void showLog() {
        isShowLog = true;
    }

    public void disableLog() {
        isShowLog = false;
    }

    private void print(String level, String msg, Object... args) {
        if (isShowLog) {
            System.out.println(String.format("[sonic-vision-core] %s [%s] %s",
                    formatter.format(new Date()), level, String.format(msg, args)));
        }
    }

    public void info(String msg, Object... args) {
        print("INFO", msg, args);
    }

    public void error(String msg, Object... args) {
        print("ERROR", msg, args);
    }
}
