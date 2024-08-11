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

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_core.DMatch;
import org.bytedeco.opencv.opencv_core.KeyPoint;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.opencv.opencv_features2d.SIFT;
import org.cloud.sonic.vision.models.FindResult;
import org.cloud.sonic.vision.tool.Logger;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;

import java.io.File;
import java.util.*;

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

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

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

//        LinkedList<DMatch> goodMatchesList = new LinkedList<>();

        DMatchVectorVector goodMatches = new DMatchVectorVector();
        LinkedList<org.opencv.core.Point> objectPoints = new LinkedList<>();
        LinkedList<org.opencv.core.Point> scenePoints = new LinkedList<>();


        for (long i = 0; i < matchPoints.size(); i++) {
            if (matchPoints.get(i).size() >= 2) {
                DMatch match1 = matchPoints.get(i).get(0);
                DMatch match2 = matchPoints.get(i).get(1);

                if (match1.distance() <= 0.6 * match2.distance()) {
                    Point2f templatePointf = originalDescriptors.get(match1.queryIdx()).pt();
                    objectPoints.addLast(new org.opencv.core.Point(templatePointf.x(),templatePointf.y()));

                    Point2f originalPointf = originalDescriptors.get(match1.trainIdx()).pt();
                    objectPoints.addLast(new org.opencv.core.Point(originalPointf.x(),originalPointf.y()));
                    objectPoints.addLast(new org.opencv.core.Point(templatePointf.x(),originalPointf.y()));

                    goodMatches.push_back(matchPoints.get(i));
                }
            }
        }

        int resultX;
        int resultY;
        Mat result = new Mat();

        if (goodMatches.size() >= 4) {

            logger.info("The template diagram is successfully matched in the original image!");
            drawMatchesKnn(image01, originalDescriptors, image02, templateDescriptors, goodMatches, result);

            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
            objMatOfPoint2f.fromList(objectPoints);
            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
            scnMatOfPoint2f.fromList(scenePoints);
            //使用 findHomography 寻找匹配上的关键点的变换
            org.opencv.core.Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

            /**
             * 透视变换(Perspective Transformation)是将图片投影到一个新的视平面(Viewing Plane)，也称作投影映射(Projective Mapping)。
             */
            org.opencv.core.Mat templateCorners = new org.opencv.core.Mat(4, 1, CvType.CV_32FC2);
            org.opencv.core.Mat templateTransformResult = new org.opencv.core.Mat(4, 1, CvType.CV_32FC2);
            templateCorners.put(0, 0, 0, 0);
            templateCorners.put(1, 0, image1.cols(), 0);
            templateCorners.put(2, 0, image1.cols(), image1.rows());
            templateCorners.put(3, 0, 0, image1.rows());
            //使用 perspectiveTransform 将模板图进行透视变以矫正图象得到标准图片
            Core.perspectiveTransform(templateCorners, templateTransformResult, homography);

            //矩形四个顶点
            double[] pointA = templateTransformResult.get(0, 0);
            double[] pointB = templateTransformResult.get(1, 0);
            double[] pointC = templateTransformResult.get(2, 0);
            double[] pointD = templateTransformResult.get(3, 0);

            //指定取得数组子集的范围
            int rowStart = (int) pointA[1];
            int rowEnd = (int) pointC[1];
            int colStart = (int) pointD[0];
            int colEnd = (int) pointB[0];
            resultX = (rowStart+rowEnd)/2;
            resultY = (colStart+colEnd)/2;
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
