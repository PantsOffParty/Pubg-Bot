package Util;

import java.awt.*;

public class Edges {
    public Edges(Point point1, Point point2){
        this.pointA = point1;
        this.pointB = point2;
        distanceCalc(pointA,pointB);
    }
    public Point getPointA() {
        return pointA;
    }

    public void setPointA(Point pointA) {
        this.pointA = pointA;
    }

    public Point getPointB() {
        return pointB;
    }

    public void setPointB(Point pointB) {
        this.pointB = pointB;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void distanceCalc(Point start, Point end){
        distance = Math.pow((end.getX() - start.getX()),2) + Math.pow((end.getY() - start.getY()),2);
        distance = Math.sqrt(distance);
        setDistance(distance);
    }

    public Point pointA;
    public Point pointB;
    public Double distance;
}
