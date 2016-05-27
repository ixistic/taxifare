package th.ac.ku.ixistic.taxifare;

/**
 * Created by ixistic on 5/22/16 AD.
 */
public class History {
    private String date;
    private double latStart;
    private double lonStart;
    private double latStop;
    private double lonStop;
    private double price;
    private double trafficTime;
    private double distance;
    private String comment;
    private String trackLocation;

    public History() {}

    public History(String date, double latStart, double lonStart, double latStop, double lonStop, double price, double trafficTime, double distance, String trackLocation) {
        this.date = date;
        this.latStart = latStart;
        this.lonStart = lonStart;
        this.latStop = latStop;
        this.lonStop = lonStop;
        this.price = price;
        this.trafficTime = trafficTime;
        this.distance = distance;
        this.comment = "";
        this.trackLocation = trackLocation;

    }

    public String getDate() {
        return date;
    }

    public double getLatStart() {
        return latStart;
    }

    public double getLonStart() {
        return lonStart;
    }

    public double getLatStop() {
        return latStop;
    }

    public double getLonStop() {
        return lonStop;
    }

    public double getPrice() {
        return price;
    }

    public double getTrafficTime() {
        return trafficTime;
    }

    public double getDistance() {
        return distance;
    }

    public String getComment() {
        return comment;
    }

    public String getTrackLocation() {
        return trackLocation;
    }
}
