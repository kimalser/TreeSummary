package utilities;

import main.Main;
import genericStructures.Interval;
import genericStructures.IntervalPoint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Utility {

    public static void addPointIntervals(Map<IntervalPoint, IntervalPoint> pointMap, List<IntervalPoint> points, Interval interval) {
        IntervalPoint start = new IntervalPoint(true, new Interval(interval.start, interval.end));
        IntervalPoint end = new IntervalPoint(false, new Interval(interval.start, interval.end));

        points.add(start);
        points.add(end);

        pointMap.put(end, start);
    }

    public static List<double[]> getMostOverlappedIntervals(List<IntervalPoint> points, Map<IntervalPoint, IntervalPoint> pointMap) {
        Collections.sort(points);

        List<IntervalPoint> curOverlap = new ArrayList<>();
        List<List<IntervalPoint>> mostOverlappedIntervalsPoints = new ArrayList<>();
        mostOverlappedIntervalsPoints.add(new ArrayList<>());

        for (IntervalPoint cur : points) {
            if (cur.isStartPoint()) {
                curOverlap.add(cur);
            } else {
                curOverlap.remove(pointMap.get(cur));
            }

            if (curOverlap.size() == mostOverlappedIntervalsPoints.get(0).size()) {
                mostOverlappedIntervalsPoints.add(new ArrayList(curOverlap));
            } else if (curOverlap.size() > mostOverlappedIntervalsPoints.get(0).size()) {
                mostOverlappedIntervalsPoints.clear();
                mostOverlappedIntervalsPoints.add(new ArrayList(curOverlap));
            }
        }

        List<double[]> mostOverlappedIntervals = new ArrayList<>();
        for (List<IntervalPoint> list : mostOverlappedIntervalsPoints) {
            double latestStart = Double.NEGATIVE_INFINITY;
            double earliestEnd = Double.POSITIVE_INFINITY;
            for (IntervalPoint p : list) {
                if (p.getInterval().start > latestStart)
                    latestStart = p.getInterval().start;
                if (p.getInterval().end < earliestEnd)
                    earliestEnd = p.getInterval().end;
            }
            mostOverlappedIntervals.add(new double[]{latestStart, earliestEnd});
        }

        return mostOverlappedIntervals;
    }


    public static double calculateSMAPE(double origVal, double recVal) {
        if (origVal == 0 && recVal == 0)
            return 0;
        return Math.abs(origVal - recVal) / (Math.abs(origVal) + Math.abs(recVal));
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return Math.ceil(value * scale) / scale;
    }

    // toRound is a boolean to indicate whether or not you would like to round the
    // raw values to one decimal place
    public static void readValuesFile(boolean toRound, String valuesFile) {
        Main.valMap = new HashMap<>();
        BufferedReader br;
        String line;
        try {
            br = new BufferedReader(new FileReader(valuesFile));
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                String[] attr = line.split(";");
                if (toRound) Main.valMap.put(attr[0], Utility.round(Double.parseDouble(attr[1]), 1));
                else Main.valMap.put(attr[0], Double.parseDouble(attr[1]));
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
