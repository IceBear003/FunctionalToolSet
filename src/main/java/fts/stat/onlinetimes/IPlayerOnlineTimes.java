package fts.stat.onlinetimes;

import java.time.LocalDate;

public class IPlayerOnlineTimes {
    public int dayTime;
    public int totalTime;
    public int lastLoginDate;

    public IPlayerOnlineTimes(int dayTime, int totalTime, int lastLoginDate) {
        this.dayTime = dayTime;
        this.totalTime = totalTime;
        this.lastLoginDate = lastLoginDate;
        LocalDate date = LocalDate.now();
        if (date.getDayOfMonth() != this.lastLoginDate) {
            this.dayTime = 0;
            this.lastLoginDate = date.getDayOfMonth();
        }
    }
}
