import java.io.DataInputStream;

public interface CompressedBitmapReader {
	/**
	 * Reads in one column of compressed data (usually from a .dat file) and translates it to the 
	 * appropriate ActiveBitCollection
	 * 
	 * @param columnIn The input stream to the compressed file on hard disk
	 * @return The ActiveBitCollection representation of the compressed column
	 * */
	ActiveBitCollection readColumn(DataInputStream columnIn);

}
