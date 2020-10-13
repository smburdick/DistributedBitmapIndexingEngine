import java.util.Iterator;

public class WAHPointQuery extends PointQuery {
	/**
	 * Creates a WAHPointQuery with a given set of bitmaps, and the columns to apply the query
	 * @param operator the operation to perform
	 * @param colID1
	 * @param colID2
	 */
	public WAHPointQuery(Operator operator, int colID1, int colID2) {
		super(operator, colID1, colID2);
	}

	@Override
	public ActiveBitCollection execute() {
		// check if columns are stored in memory
		ActiveBitCollection A = Query.columns.get(this.colID1);
		ActiveBitCollection B = Query.columns.get(this.colID2);

		// get A (and possibly B) from disk; store in 'columns'
		if (A == null) {
			A = super.loadFile(this.colID1);
		}
		if (B == null) {
			B = super.loadFile(this.colID2);
		}

		// perform the point query
		switch (this.operator) {
			case OR:
				return this.OrQuery(A,B);
			case AND:
				return this.AndQuery(A,B);
			default:
				throw new RuntimeException("Unsupported operator: " + this.operator.toString());
		}
	}

	/**
	 * Takes two compressed columns and performs a logical OR
	 * operation on them. The results are returned in a bit vector
	 * @param vec1 A compressed vector for querying
	 * @param vec2 A compressed vector for querying
	 * @return  the result of vec1 OR vec2
	 */
	@Override
	public ActiveBitCollection OrQuery(ActiveBitCollection vec1, ActiveBitCollection vec2) {
		if (vec1 == vec2) {
			return vec1;
		}

		ActiveBitCollection ret;

		//create the result bitcollection
		ret = (ActiveBitCollection) new VLCActiveBitCol(RunConfig.ENCODING.WAH32.getWordLen()-1,"Res_"+
				vec1.getColName()+"_OR_"+ vec2.getColName());
		Iterator<Long> col1It = vec1.getSegmentIterator();
		Iterator<Long> col2It = vec2.getSegmentIterator();

		//These decode the segments into the decodeLen
		WAHActiveSegment col1Seg = new WAHActiveSegment(col1It.next());
		WAHActiveSegment col2Seg = new WAHActiveSegment(col2It.next());

		//need to do this loop at least once even if there is only one segment
		do {
			//See if we need to fetch a new segment from either one of the columns
			if(col1Seg.numOfSegments() == 0){
				col1Seg = new WAHActiveSegment(col1It.next());
			}
			if(col2Seg.numOfSegments() == 0){
				col2Seg = new WAHActiveSegment(col2It.next());
			}
			//System.out.println("vec1 "+col1Seg.numOfSegments()+ "  vec2 "+col2Seg.numOfSegments());
			//process the decoded segments
			while(col1Seg.numOfSegments()!=0 && col2Seg.numOfSegments() != 0){

				if(col1Seg.isFill()){
					if(col2Seg.isFill()){//They are both fills
						//find the shortest run
						long minSegs = Math.min(col1Seg.numOfSegments(), col2Seg.numOfSegments());
						//append a run of that length the return value
						ret.appendFill(minSegs, (byte)(col1Seg.getFillValue()|col2Seg.getFillValue()));
						//mark those words as being used
						col1Seg.usedNumWords(minSegs);
						col2Seg.usedNumWords(minSegs);
					}else{//vec1 is a fill vec2 is a literal
						ret.appendLiteral((col1Seg.getLiteralRepOfFill()|col2Seg.getLiteralValue()));
					}

				}else{//col1Seg is a literal
					if(col2Seg.isFill()){
						ret.appendLiteral((col2Seg.getLiteralRepOfFill()|col1Seg.getLiteralValue()));
					}else{//both are literals
						ret.appendLiteral((col2Seg.getLiteralValue()|col1Seg.getLiteralValue()));
					}

				}
			}
			//need to use || here because of the way the columns are read from disk
			//This solves a problem when one column slops over and writes a single segment into
			//the last word.  The remainder of that word is filled with zeros.  If the other column
			//does not have the same slop the columns won't have the same number of bits.  Luckily all the
			//extra bits can be disregarded (since they are just extra padding) and so we can stop the loop when
			//one of the columns is exhausted.
		} while(col1It.hasNext() && col2It.hasNext()); //NOTE DC LOOKING INTO THIS
		return ret;
	}

	/** Takes two compressed vector and performs a logical AND
	 * operation on them. The results are returned in a bit vector
	 *
	 * @param vec1 A compressed vector for querying
	 * @param vec2 A compressed vector for querying
	 * @return  the result of vec1 AND vec2
	 * */
	@Override
	public ActiveBitCollection AndQuery(ActiveBitCollection vec1, ActiveBitCollection vec2) {
		if (vec1 == vec2) {
			return vec1;
		}

		ActiveBitCollection ret;

		//create the result bitcollection
		ret = (ActiveBitCollection) new VLCActiveBitCol(RunConfig.ENCODING.WAH32.getWordLen()-1,"Res_"+
				vec1.getColName()+"_AND_"+vec2.getColName());
		Iterator<Long> col1It = vec1.getSegmentIterator();
		Iterator<Long> col2It = vec2.getSegmentIterator();
		//These decode the segments into the decodeLen
		WAHActiveSegment col1Seg = new WAHActiveSegment(col1It.next());
		WAHActiveSegment col2Seg = new WAHActiveSegment(col2It.next());
		//need to do this loop at least once even if there is only one segment
		do {
			//See if we need to fetch a new segment from either one of the columns
			if(col1Seg.numOfSegments() == 0){
				col1Seg = new WAHActiveSegment(col1It.next());
			}
			if(col2Seg.numOfSegments() == 0){
				col2Seg = new WAHActiveSegment(col2It.next());
			}
			//System.out.println("vec1 "+col1Seg.numOfSegments()+ "  vec2 "+col2Seg.numOfSegments());
			//process the decoded segments
			while(col1Seg.numOfSegments()!=0 && col2Seg.numOfSegments() != 0){

				if(col1Seg.isFill()){
					if(col2Seg.isFill()){//They are both fills
						//find the shortest run
						long minSegs = Math.min(col1Seg.numOfSegments(), col2Seg.numOfSegments());
						//append a run of that length the return value
						ret.appendFill(minSegs, (byte)(col1Seg.getFillValue()&col2Seg.getFillValue()));
						//mark those words as being used
						col1Seg.usedNumWords(minSegs);
						col2Seg.usedNumWords(minSegs);
					}else{//vec1 is a fill vec2 is a literal
						ret.appendLiteral((col1Seg.getLiteralRepOfFill()&col2Seg.getLiteralValue()));
					}

				}else{//col1Seg is a literal
					if(col2Seg.isFill()){
						ret.appendLiteral((col2Seg.getLiteralRepOfFill()&col1Seg.getLiteralValue()));
					}else{//both are literals
						ret.appendLiteral((col2Seg.getLiteralValue()&col1Seg.getLiteralValue()));
					}

				}
			}
			//need to use || here because of the way the columns are read from disk
			//This solves a problem when one column slops over and writes a single segment into
			//the last word.  The remainder of that word is filled with zeros.  If the other column
			//does not have the same slop the columns won't have the same number of bits.  Luckily all the
			//extra bits can be disregarded (since they are just extra padding) and so we can stop the loop when
			//one of the columns is exhausted.
		} while(col1It.hasNext() && col2It.hasNext());
		return ret;
	}

	@Override
	public void setBitmapReader() {
		super.cbr = new VLCCompressedReader();
	}
}
