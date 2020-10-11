public class WAHRangeQuery extends RangeQuery {
    /**
     * Creates a WAH range query
     * @param operator the operation to perform
     * @param start ID of start column
     * @param end   ID of end column
     * @pre start <= end
     */
    public WAHRangeQuery(Operator operator, int start, int end) {
        super(operator, start, end);
    }

    @Override
    public void setBitmapReader() {
        super.cbr = new VLCCompressedReader();
    }

    /**
     * Executes the point query
     * @return the result vector
     */
    @Override
    public ActiveBitCollection execute() {
        // initialize by retrieving the first column in the range
        ActiveBitCollection ret = Query.columns.get(this.startColID);
        if (ret == null) {
            ret = super.loadFile(this.startColID);
        }
        Query.columns.put(RangeQuery.TMP_ID, ret);

        // loop through columns for repeated point-query execution
        PointQuery pt;
        for (int i = this.startColID + 1; i <= this.endColID; i++) {
            pt = new WAHPointQuery(this.operator, RangeQuery.TMP_ID, i);
            // update intermediate result
            Query.columns.put(RangeQuery.TMP_ID, pt.execute());
        }

        // remove intermediate result and return it
        return Query.columns.remove(RangeQuery.TMP_ID);
    }

}
