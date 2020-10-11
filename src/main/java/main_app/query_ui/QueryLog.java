package main_app.query_ui;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class QueryLog {

    private String query;
    private DateTime queryTimeBegin;
    private DateTime queryTimeEnd;
    private DateTime duration;
    private int nResultsLines;
    private DateTimeFormatter formatter;
    private DateTimeFormatter formatter2;
    private boolean queryFailed = false;

    public QueryLog(String query, DateTime queryTimeBegin, DateTime queryTimeEnd, int nResultsLines) {
        this.query = query;
        this.queryTimeBegin = queryTimeBegin;
        this.queryTimeEnd = queryTimeEnd;
        if (this.queryTimeEnd == null){
            queryFailed = true;
        }
        else {
            //determine difference
            long diffInMillis = queryTimeBegin.getMillis() - queryTimeEnd.getMillis();
            this.duration = new DateTime(diffInMillis);
        }
        this.nResultsLines = nResultsLines;
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

    public DateTime getDuration() {
        return this.duration;
    }

    public void setDuration(DateTime duration) {
        this.duration = duration;
    }

    public int getnResultsLines() {
        return this.nResultsLines;
    }

    public void setnResultsLines(int nResultsLines) {
        this.nResultsLines = nResultsLines;
    }

    @Override
    public String toString() {
        if (!queryFailed) {
            return "Start time: " + formatter.print(queryTimeBegin) + " - Query Time End: " + formatter.print(queryTimeEnd) + " (" + formatter2.print(duration) + ") "
                    + "\nNumber of rows: " + nResultsLines
                    + "\nQuery: \n" + query;
        }
        return "Start time: " + formatter.print(queryTimeBegin) + " - QUERY FAILED"
                + "\nQuery: \n" + query;
    }
}
