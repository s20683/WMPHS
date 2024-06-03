package com.s20683.wmphs.tools;

import java.sql.Timestamp;

public class QueryTimer {
    private final long ts1 = System.currentTimeMillis();
    private final Timestamp sqlTimestamp;
    private long ts2;

    public QueryTimer() {
        this.ts2 = this.ts1;
        this.sqlTimestamp = new Timestamp(this.ts1);
    }

    public final void start() {
        this.ts2 = System.currentTimeMillis();
    }

    public Timestamp getSqlTimestamp() {
        return this.sqlTimestamp;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("query waited ");
        sb.append(this.ts2 - this.ts1);
        sb.append("ms executed in ");
        sb.append(System.currentTimeMillis() - this.ts2);
        sb.append("ms");
        return sb.toString();
    }
}
