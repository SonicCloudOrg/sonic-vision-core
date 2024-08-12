package org.cloud.sonic.vision.tool;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point2f;

import java.nio.FloatBuffer;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CV_32FC2;

public class Util {
    static public Mat pointToMat(List<Point2f> point2fs){
        Mat dest = new Mat(point2fs.size(),1,  CV_32FC2);
        FloatBuffer index = dest.createBuffer();
        for (int i=0;i<point2fs.size();i++){
            index.put(2 * i, point2fs.get(i).x());
            index.put(2 * i + 1, point2fs.get(i).y());
        }
        return dest;
    }
}
