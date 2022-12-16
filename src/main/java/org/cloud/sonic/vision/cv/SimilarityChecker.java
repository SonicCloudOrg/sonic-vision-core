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

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.cloud.sonic.vision.tool.Logger;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

public class SimilarityChecker {
    private Logger logger = new Logger();

    public double getSimilarMSSIMScore(File file1, File file2, Boolean isDelete) {
        Mat i1 = imread(file1.getAbsolutePath());
        Mat n = new Mat();
        Mat i2 = imread(file2.getAbsolutePath());
        Size s = new Size();
        s.height(i2.arrayHeight());
        s.width(i2.arrayWidth());
        resize(i1, n, s);
        if (n.size().get() != i2.size().get()) {
            return 0;
        }
        double C1 = 6.5025, C2 = 58.5225;
        int d = opencv_core.CV_32F;
        Mat I1 = new Mat();
        Mat I2 = new Mat();
        n.convertTo(I1, d);
        i2.convertTo(I2, d);
        Mat I2_2 = I2.mul(I2).asMat();
        Mat I1_2 = I1.mul(I1).asMat();
        Mat I1_I2 = I1.mul(I2).asMat();
        Mat mu1 = new Mat();
        Mat mu2 = new Mat();
        GaussianBlur(I1, mu1, new Size(11, 11), 1.5);
        GaussianBlur(I2, mu2, new Size(11, 11), 1.5);
        Mat mu1_2 = mu1.mul(mu1).asMat();
        Mat mu2_2 = mu2.mul(mu2).asMat();
        Mat mu1_mu2 = mu1.mul(mu2).asMat();
        Mat sigma1_2 = new Mat();
        Mat sigma2_2 = new Mat();
        Mat sigma12 = new Mat();
        GaussianBlur(I1_2, sigma1_2, new Size(11, 11), 1.5);
        sigma1_2 = subtract(sigma1_2, mu1_2).asMat();
        GaussianBlur(I2_2, sigma2_2, new Size(11, 11), 1.5);
        sigma2_2 = subtract(sigma2_2, mu2_2).asMat();
        GaussianBlur(I1_I2, sigma12, new Size(11, 11), 1.5);
        sigma12 = subtract(sigma12, mu1_mu2).asMat();
        Mat t1, t2, t3;
        t1 = add(multiply(2, mu1_mu2), Scalar.all(C1)).asMat();
        t2 = add(multiply(2, sigma12), Scalar.all(C2)).asMat();
        t3 = t1.mul(t2).asMat();
        t1 = add(add(mu1_2, mu2_2), Scalar.all(C1)).asMat();
        t2 = add(add(sigma1_2, sigma2_2), Scalar.all(C2)).asMat();
        t1 = t1.mul(t2).asMat();
        Mat ssim_map = new Mat();
        divide(t3, t1, ssim_map);
        Scalar mSsim = mean(ssim_map);
        if (isDelete) {
            file1.delete();
            file2.delete();
        }
        double re = new BigDecimal((mSsim.get(0) + mSsim.get(1) + mSsim.get(2)) / 3).setScale(2, RoundingMode.DOWN).doubleValue();
        logger.info("similar rate: " + re);
        return re;
    }
}