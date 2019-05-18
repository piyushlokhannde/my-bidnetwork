package com.bid.contract;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;

public class    CurrentDateProvider {

    private static LocalDate todayDate = LocalDate.now();


    private static boolean isMock = false;

    public static void setDate(LocalDate todayDateIn) {
        todayDate = todayDateIn;
    }

    public  static LocalDate getDate() {
        return todayDate;
    }

    public static void enableMockMode() {
        isMock =true;
    }
    public static boolean isDateBeforeCurrentDate(ChronoLocalDate date) {
        if(isMock) {
            return  false;
        }
      return todayDate.isBefore(date);
    }


    public static boolean isCurrentDateBetweenDate(ChronoLocalDate bidStartDate, ChronoLocalDate bidEndDate) {
        if(isMock) {
            return  true;
        }
       return getDate().isAfter(bidStartDate)
                && CurrentDateProvider.getDate().isBefore(bidEndDate);
    }

}
