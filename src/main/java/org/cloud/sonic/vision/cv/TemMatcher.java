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
package org.cloud.sonic.vision.cv;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.*;
import org.cloud.sonic.vision.models.FindResult;
import org.cloud.sonic.vision.tool.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class TemMatcher {
    private Logger logger = new Logger();

    public FindResult getTemMatchResult(File temFile, File beforeFile, boolean isDelete) throws IOException {
        try {
            Mat sourceColor = imread(beforeFile.getAbsolutePath());
            Mat sourceGrey = new Mat(sourceColor.size(), CV_8UC1);
            cvtColor(sourceColor, sourceGrey, COLOR_BGR2GRAY);
            Mat template = imread(temFile.getAbsolutePath(), IMREAD_GRAYSCALE);
            Size size = new Size(sourceGrey.cols() - template.cols() + 1, sourceGrey.rows() - template.rows() + 1);
            Mat result = new Mat(size, CV_32FC1);

            long start = System.currentTimeMillis();
            matchTemplate(sourceGrey, template, result, TM_CCORR_NORMED);
            DoublePointer minVal = new DoublePointer();
            DoublePointer maxVal = new DoublePointer();
            Point min = new Point();
            Point max = new Point();
            minMaxLoc(result, minVal, maxVal, min, max, null);
            rectangle(sourceColor, new Rect(max.x(), max.y(), template.cols(), template.rows()), randColor(), 2, 0, 0);
            FindResult findResult = new FindResult();
            findResult.setTime((int) (System.currentTimeMillis() - start));
            long time = Calendar.getInstance().getTimeInMillis();
            File parent = new File("test-output");
            if (!parent.exists()) {
                parent.mkdirs();
            }
            String fileName = "test-output" + File.separator + time + ".jpg";
            imwrite(fileName, sourceColor);
            findResult.setX(max.x() + template.cols() / 2);
            findResult.setY(max.y() + template.rows() / 2);
            findResult.setFile(new File(fileName));
            logger.info(findResult.toString());
            return findResult;
        } finally {
            if (isDelete) {
                temFile.delete();
                beforeFile.delete();
            }
        }
    }

    public static Scalar randColor() {
        int b, g, r;
        b = ThreadLocalRandom.current().nextInt(0, 255 + 1);
        g = ThreadLocalRandom.current().nextInt(0, 255 + 1);
        r = ThreadLocalRandom.current().nextInt(0, 255 + 1);
        return new Scalar(b, g, r, 0);
    }
}