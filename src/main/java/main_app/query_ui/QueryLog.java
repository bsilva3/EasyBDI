package main_app.query_ui;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.concurrent.TimeUnit;

public class QueryLog {

    private String query;
    private DateTime queryTimeBegin;
    private DateTime queryTimeEnd;
    private long durationSeconds;
    private long durationMiliseconds;
    private int nResultsLines;
    private DateTimeFormatter formatter;
    private DateTimeFormatter formatter2;
    private int queryStatus;
    private static final int QUERY_STATUS_SUCCESS = 0;
    private static final int QUERY_STATUS_FAILED = -1;
    private static final int QUERY_STATUS_NOT_EXECUTED = 1;

    public QueryLog(String query, DateTime queryTimeBegin, DateTime queryTimeEnd, int nResultsLines) {
        this.query = query;
        this.queryTimeBegin = queryTimeBegin;
        this.queryTimeEnd = queryTimeEnd;
        if (this.queryTimeEnd == null){
            queryStatus = QUERY_STATUS_FAILED;
        }
        else {
            queryStatus = QUERY_STATUS_SUCCESS;
            //determine difference
            long diffInMillis = queryTimeEnd.getMillis() - queryTimeBegin.getMillis();
            this.durationSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
            this.durationMiliseconds = diffInMillis;
        }
        this.nResultsLines = nResultsLines;
        this.formatter = DateTimeFormat.forPattern("HH:mm:ss");
        this.formatter2 = DateTimeFormat.forPattern("mm:ss");
    }

    public QueryLog(String query, DateTime queryTimeBegin) {
        this.query = query;
        this.queryTimeBegin = queryTimeBegin;
        queryStatus = QUERY_STATUS_NOT_EXECUTED;
        this.formatter = DateTimeFormat.forPattern("HH:mm:ss");
        this.formatter2 = DateTimeFormat.forPattern("mm:ss");
    }

    public String getQuery() {
        return this.query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public DateTime getQueryTimeBegin() {
        return this.queryTimeBegin;
    }

    public void setQueryTimeBegin(DateTime queryTimeBegin) {
        this.queryTimeBegin = queryTimeBegin;
    }

    public DateTime getQueryTimeEnd() {
        return this.queryTimeEnd;
    }

    public void setQueryTimeEnd(DateTime queryTimeEnd) {
        this.queryTimeEnd = queryTimeEnd;
    }

    public long getDurationSeconds() {
        return this.durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getnResultsLines() {
        return this.nResultsLines;
    }

    public void setnResultsLines(int nResultsLines) {
        this.nResultsLines = nResultsLines;
    }

    public long getDurationMiliseconds() {
        return durationMiliseconds;
    }

    @Override
    public String toString() {
        if (queryStatus == QUERY_STATUS_SUCCESS) {
            return "Start time: " + formatter.print(queryTimeBegin) + " - Query Time End: " + formatter.print(queryTimeEnd) +
                    " (" + durationSeconds + " seconds and " + durationMiliseconds + " miliseconds) "
                    + "\nNumber of rows: " + nResultsLines
                    + "\n Query: \n" + query;
        } else if (queryStatus == QUERY_STATUS_FAILED) {
            return "Start time: " + formatter.print(queryTimeBegin) + " - QUERY FAILED"
                    + "\n Query: \n" + query;
        } else if (queryStatus == QUERY_STATUS_NOT_EXECUTED) {//used for query creation logs
            return "time: " + formatter.print(queryTimeBegin)
                    + "\n Query: \n" + query;
        }
        return "";
    }
}
