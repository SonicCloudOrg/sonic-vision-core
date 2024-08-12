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


import org.bytedeco.opencv.global.opencv_calib3d;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_core.DMatch;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.opencv.opencv_features2d.SIFT;
import org.cloud.sonic.vision.models.FindResult;
import org.cloud.sonic.vision.tool.Logger;
import org.cloud.sonic.vision.tool.Util;

import java.io.File;
import java.util.*;
import java.util.Arrays;

import static org.bytedeco.opencv.global.opencv_calib3d.CV_RANSAC;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.bytedeco.opencv.global.opencv_features2d.drawMatchesKnn;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class SIFTFinder {
    private Logger logger = new Logger();

//    public FindResult getSIFTFindResult(File temFile, File beforeFile, boolean isDelete) throws Exception {
//        Mat image01 = imread(beforeFile.getAbsolutePath());
//        Mat image02 = imread(temFile.getAbsolutePath());
//
//        Mat image1 = new Mat();
//        Mat image2 = new Mat();
//        cvtColor(image01, image1, COLOR_BGR2GRAY);
//        cvtColor(image02, image2, COLOR_BGR2GRAY);
//
//        KeyPointVector keyPointVector1 = new KeyPointVector();
//        KeyPointVector keyPointVector2 = new KeyPointVector();
//        Mat image11 = new Mat();
//        Mat image22 = new Mat();
//
//        long start = System.currentTimeMillis();
//        SIFT sift = SIFT.create();
//        sift.detectAndCompute(image1, image1, keyPointVector1, image11);
//        sift.detectAndCompute(image2, image2, keyPointVector2, image22);
//
//        FlannBasedMatcher flannBasedMatcher = new FlannBasedMatcher();
//        DMatchVectorVector matchPoints = new DMatchVectorVector();
//
//        flannBasedMatcher.knnMatch(image11, image22, matchPoints, 2);
//        logger.info(matchPoints.size() + " point from img");
//        DMatchVectorVector goodMatches = new DMatchVectorVector();
//
//        List<Integer> xs = new ArrayList<>();
//        List<Integer> ys = new ArrayList<>();
//        for (long i = 0; i < matchPoints.size(); i++) {
//            if (matchPoints.get(i).size() >= 2) {
//                DMatch match1 = matchPoints.get(i).get(0);
//                DMatch match2 = matchPoints.get(i).get(1);
//
//                if (match1.distance() <= 0.6 * match2.distance()) {
//                    xs.add((int) keyPointVector1.get(match1.queryIdx()).pt().x());
//                    ys.add((int) keyPointVector1.get(match1.queryIdx()).pt().y());
//                    goodMatches.push_back(matchPoints.get(i));
//                }
//            }
//        }
//        logger.info(goodMatches.size() + " point matched.");
//        if (goodMatches.size() <= 4) {
//            temFile.delete();
//            beforeFile.delete();
//            return null;
//        }
//        FindResult findResult = new FindResult();
//        findResult.setTime((int) (System.currentTimeMillis() - start));
//        Mat result = new Mat();
//
//        drawMatchesKnn(image01, keyPointVector1, image02, keyPointVector2, goodMatches, result);
//
//        int resultX = majorityElement(xs);
//        int resultY = majorityElement(ys);
//        findResult.setX(resultX);
//        findResult.setY(resultY);
//        logger.info("result rect: (" + resultX + "," + resultY + ")");
//        circle(result, new Point(resultX, resultY), 5, Scalar.RED, 10, CV_AA, 0);
//        long time = Calendar.getInstance().getTimeInMillis();
//        File parent = new File("test-output");
//        if (!parent.exists()) {
//            parent.mkdirs();
//        }
//        String fileName = "test-output" + File.separator + time + ".jpg";
//        imwrite(fileName, result);
//        findResult.setFile(new File(fileName));
//        if (isDelete) {
//            temFile.delete();
//            beforeFile.delete();
//        }
//        return findResult;
//    }

    public FindResult getSIFTFindResult(File temFile, File beforeFile, boolean isDelete) throws Exception {

        Mat image01 = imread(beforeFile.getAbsolutePath());
        Mat image02 = imread(temFile.getAbsolutePath());

        Mat image1 = new Mat();
        Mat image2 = new Mat();
        cvtColor(image01, image1, COLOR_BGR2GRAY);
        cvtColor(image02, image2, COLOR_BGR2GRAY);

        KeyPointVector originalDescriptors = new KeyPointVector();
        KeyPointVector templateDescriptors = new KeyPointVector();
        Mat image11 = new Mat();
        Mat image22 = new Mat();

        long start = System.currentTimeMillis();
        SIFT sift = SIFT.create();
        sift.detectAndCompute(image1, image1, originalDescriptors, image11);
        sift.detectAndCompute(image2, image2, templateDescriptors, image22);

        FlannBasedMatcher flannBasedMatcher = new FlannBasedMatcher();
        DMatchVectorVector matchPoints = new DMatchVectorVector();

        flannBasedMatcher.knnMatch(image11, image22, matchPoints, 2);
        logger.info(matchPoints.size() + " point from img");

        DMatchVectorVector goodMatches = new DMatchVectorVector();
        LinkedList<Point2f> objectPoints = new LinkedList<>();
        LinkedList<Point2f> scenePoints = new LinkedList<>();

        for (long i = 0; i < matchPoints.size(); i++) {
            if (matchPoints.get(i).size() >= 2) {
                DMatch match1 = matchPoints.get(i).get(0);
                DMatch match2 = matchPoints.get(i).get(1);

                if (match1.distance() <= 0.6 * match2.distance()) {
                    scenePoints.addLast(originalDescriptors.get(match1.queryIdx()).pt());
                    objectPoints.addLast(templateDescriptors.get(match2.trainIdx()).pt());
                    goodMatches.push_back(matchPoints.get(i));
                }
            }
        }

        Mat mask = new Mat(objectPoints.size(), 1, CV_8UC1);

        int resultX;
        int resultY;
        Mat result = new Mat();

        if (goodMatches.size() >= 4) {

            logger.info("The template diagram is successfully matched in the original image!");
            drawMatchesKnn(image01, originalDescriptors, image02, templateDescriptors, goodMatches, result);

            mask.resize(objectPoints.size());

            Mat objMat = Util.pointToMat(objectPoints);
            Mat scnMat = Util.pointToMat(scenePoints);

            Mat H = opencv_calib3d.findHomography(objMat, scnMat, mask,CV_RANSAC, 5);

            if (H.empty()) {
                return null;
            }

            double[] h = (double[]) H.createIndexer(false).array();
            double[] srcCorners = { 0, 0,  image1.cols(), image1.rows(),  image1.cols(), 0 };
            double[] dstCorners = new double[srcCorners.length];

            for(int i = 0; i < srcCorners.length/2; i++) {
                double x = srcCorners[2*i], y = srcCorners[2*i + 1];
                double Z = 1/(h[6]*x + h[7]*y + h[8]);
                double X = (h[0]*x + h[1]*y + h[2])*Z;
                double Y = (h[3]*x + h[4]*y + h[5])*Z;
                dstCorners[2*i    ] = X;
                dstCorners[2*i + 1] = Y;
            }
            double top = Double.MAX_VALUE;
            double left = Double.MAX_VALUE;
            double bottom = Double.MIN_VALUE;
            double right = Double.MIN_VALUE;

            for (int i = 0; i < dstCorners.length / 2; i++) {
                double x = dstCorners[2 * i];
                double y = dstCorners[2 * i + 1];

                if (x < left) left = x;
                if (x > right) right = x;
                if (y < top) top = y;
                if (y > bottom) bottom = y;
            }

            //指定取得数组子集的范围
            int rowStart = (int) left;
            int rowEnd = (int) right;
            int colStart = (int) top;
            int colEnd = (int) bottom;
            resultX = (rowStart + rowEnd) / 2;
            resultY = (colStart + colEnd) / 2;
        } else {
            temFile.delete();
            beforeFile.delete();
            return null;
        }
        FindResult findResult = new FindResult();
        findResult.setX(resultX);
        findResult.setY(resultY);


        logger.info("result rect: (" + resultX + "," + resultY + ")");
        circle(result, new Point(resultX, resultY), 5, Scalar.RED, 10, CV_AA, 0);
        long time = Calendar.getInstance().getTimeInMillis();
        File parent = new File("test-output");
        if (!parent.exists()) {
            parent.mkdirs();
        }
        String fileName = "test-output" + File.separator + time + ".jpg";
        imwrite(fileName, result);
        findResult.setFile(new File(fileName));
        if (isDelete) {
            temFile.delete();
            beforeFile.delete();
        }
        return findResult;
    }

    public static int majorityElement(List<Integer> nums) {
        double j;
        Collections.sort(nums);
        int size = nums.size();
        if (size % 2 == 1) {
            j = nums.get((size - 1) / 2);
        } else {
            j = (nums.get(size / 2 - 1) + nums.get(size / 2) + 0.0) / 2;
        }
        return (int) j;
    }
}
