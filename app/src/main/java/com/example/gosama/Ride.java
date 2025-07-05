package com.example.gosama;

import com.google.firebase.firestore.GeoPoint;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Ride implements Parcelable {
    private String documentId;
    private String driverId;
    private String pickupAddress;
    private String dropoffAddress;
    private GeoPoint pickupLocation;
    private GeoPoint dropoffLocation;
    private String departureDate;
    private String departureTime;
    private long availableSeats;
    private @ServerTimestamp Date timestamp;

    // Required empty public constructor for Firestore
    public Ride() {}

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDropoffAddress() {
        return dropoffAddress;
    }

    public void setDropoffAddress(String dropoffAddress) {
        this.dropoffAddress = dropoffAddress;
    }

    public GeoPoint getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(GeoPoint pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public GeoPoint getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(GeoPoint dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public long getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(long availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Parcelable implementation
    protected Ride(Parcel in) {
        documentId = in.readString();
        driverId = in.readString();
        pickupAddress = in.readString();
        dropoffAddress = in.readString();
        if (in.readByte() == 1) {
            pickupLocation = new GeoPoint(in.readDouble(), in.readDouble());
        } else {
            pickupLocation = null;
        }
        if (in.readByte() == 1) {
            dropoffLocation = new GeoPoint(in.readDouble(), in.readDouble());
        } else {
            dropoffLocation = null;
        }
        departureDate = in.readString();
        departureTime = in.readString();
        availableSeats = in.readLong();
        long tmpTimestamp = in.readLong();
        timestamp = tmpTimestamp == -1 ? null : new Date(tmpTimestamp);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentId);
        dest.writeString(driverId);
        dest.writeString(pickupAddress);
        dest.writeString(dropoffAddress);
        if (pickupLocation != null) {
            dest.writeByte((byte) 1);
            dest.writeDouble(pickupLocation.getLatitude());
            dest.writeDouble(pickupLocation.getLongitude());
        } else {
            dest.writeByte((byte) 0);
        }
        if (dropoffLocation != null) {
            dest.writeByte((byte) 1);
            dest.writeDouble(dropoffLocation.getLatitude());
            dest.writeDouble(dropoffLocation.getLongitude());
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeString(departureDate);
        dest.writeString(departureTime);
        dest.writeLong(availableSeats);
        dest.writeLong(timestamp != null ? timestamp.getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Ride> CREATOR = new Creator<Ride>() {
        @Override
        public Ride createFromParcel(Parcel in) {
            return new Ride(in);
        }

        @Override
        public Ride[] newArray(int size) {
            return new Ride[size];
        }
    };
}
