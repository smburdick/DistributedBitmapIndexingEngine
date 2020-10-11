/**
 * This file contains configurations used for the experiments.
 * @author David
 * @version 12/28/19
 */
public class RunConfig {

	/** Global options */
	public final static BitmapEncoding ENCODING = BitmapEncoding.WAH32; // encoding to use throughout
	public final static String PATH = "Experiments/";   // parent path to raw, compressed, & query files

	/** For either GenerateSyntheticDataMain or GenerateQueriesMain */
	public final static int NUM_ATTRIBUTES = 10; 	// number of attributes
	public final static int BIN_CARDINALITY = 100; 	// cardinality (number of bins per attribute)

	/** For GenerateSyntheticDataMain */
	public final static boolean DATAGEN_GREYCODE = true;	// use grey code reordering
	public final static int DATAGEN_NUM_ROWS = 1000000; 	// number of rows
	public final static int DATAGEN_BIN_SKEW = 2; 		// skew for which bins are chosen to be favored with a 1
														// 0 = uniform distribution
														// \infty = first rank always gets picked

	public final static String DATAGEN_OUT_FILE = "bitmap_raw" +
												  "_attr" + NUM_ATTRIBUTES +
												  "_card" + BIN_CARDINALITY +
												  "_rows" + DATAGEN_NUM_ROWS +
												  "_skew" + DATAGEN_BIN_SKEW +
												  "_gc" + DATAGEN_GREYCODE +
												  ".txt"; // file that will contain raw bitmap (without path)

	public final static String DATAGEN_FULLPATH_TO_OUT_FILE = PATH + DATAGEN_OUT_FILE; // path to file that will contain raw bitmap


	/** For GenerateQueriesMain */
	public final static QueryGenerator.Mode QUERYGEN_MODE = QueryGenerator.Mode.MIXED;		// must be one of {POINT_ONLY, RANGE_ONLY, MIXED_MODE}
	public final static double QUERYGEN_PT_LOAD_FACTOR = 0.0;             // % of queries should be POINT in MIXED_MODE. this parameter is ignored for POINT_ONLY and RANGE_ONLY
	public final static double QUERYGEN_PT_AND_LOAD_FACTOR = .5;		 // % of time an AND operator is generated in a point query, instead of an OR
	public final static int QUERYGEN_NUM_QUERIES = 1000000;				 // number of queries to generate
	public final static int QUERYGEN_ATTR_SKEW = 2;						 // skew of number of attributes to query and attribute selection (default = 2)
	public final static int QUERYGEN_BIN_SKEW = 2;						 // skew of bin selection (default = 2)
	public final static String QUERYGEN_OUT_FILE = "query_out" +
													"_mode" + QUERYGEN_MODE +
													"_ptload" + QUERYGEN_PT_LOAD_FACTOR +
													"_queries" + QUERYGEN_NUM_QUERIES +
													"_attSkew" + QUERYGEN_ATTR_SKEW +
													"_binSkew" + QUERYGEN_BIN_SKEW +
													".txt";  // without path
	public final static String QUERYGEN_FULLPATH_TO_OUT_FILE = PATH + QUERYGEN_OUT_FILE;  // without path

	/** For CompressionMain */
	public final static String CMP_COMPRESSED_INDEX_FILE_PREFIX = "col_";           // all compressed files start with..
	public final static String CMP_COMPRESSED_INDEX_FILE_EXTENSION = ".dat";        // all compressed files end with..
	public final static String CMP_RAW_FILE_FOR_COMPRESSION = DATAGEN_OUT_FILE;  	// without path. (May want to change to specific file)
	public final static String CMP_FULLPATH_TO_RAW_FILE = PATH + CMP_RAW_FILE_FOR_COMPRESSION;      // derived
	public final static String CMP_FULLPATH_TO_COMPRESSED_INDEX =
			CMP_FULLPATH_TO_RAW_FILE.substring(0, CMP_FULLPATH_TO_RAW_FILE.indexOf(".")) + "/";   // derived

	/** For QueryMain files */
	public final static String QUERY_FILE = QUERYGEN_OUT_FILE;	// without path
	public final static String QUERY_FULLPATH_TO_FILE = PATH + QUERY_FILE;

	/** Caching support */
	public final static CachePolicy CACHE_POLICY = CachePolicy.NO_CACHE;
//	public final static CachePolicy CACHE_POLICY = CachePolicy.FIND_AND_SPLIT;
//	public final static CachePolicy CACHE_POLICY = CachePolicy.JARVIS;
//	public final static CachePolicy CACHE_POLICY = CachePolicy.TAKE_ALL;
	public final static int CACHE_THRESHOLD = 0;				// don't cache a result unless range exceeds threshold
	public final static boolean CACHE_REMAINDER_QUERY = false;	// cache results from remainder query?

	/**
	 * For experiments and logging
	 */
	public final static String EXPR_OUT_FILE = "expr_out_" + CACHE_POLICY + ".txt";	// out file for logs



	/**
	 * A listing of supported encodings
	 */
	public enum BitmapEncoding {
		WAH32(32),
		WAH64(64),
		VAL32(32),
		PLWAH32(32),
		VLC(32);

		int wordLen;
		BitmapEncoding(int wordLen) {
			this.wordLen = wordLen;
		}

		public int getWordLen() {
			return this.wordLen;
		}
	}

	/**
	 * Listing of supported cache policies
	 */
	public enum CachePolicy {
		NO_CACHE, FIND_AND_SPLIT, JARVIS, TAKE_ALL
	}
}
