import java.io.BufferedReader;
import java.io.BufferedWriter;

public class QTree
{
    private int compressedSize;
    private int dim;
    private int [][] rawImage;
    private int rawSize;
    private FourZipNode root;
    public static int QUAD_SPLIT = -1;

    public QTree()
    {

    }

    private boolean canCompressBlock(Coordinate start, int size)
    {
        return true;
    }


    public void compress()
    {

    }

    private FourZipNode	compress(Coordinate start, int size)
    {
        return null;
    }

    public int getCompressedSize()
    {
        return -1;
    }

    public int[][] getRawImage()
    {
        return new int [1][1];
    }

    public int getRawSize()
    {
        return -1;
    }

    public int getSideDim()
    {
        return -1;
    }

    private static FourZipNode parse(BufferedReader file)
    {
        return null;
    }

    private String preorder(FourZipNode node)
    {
        return "";
    }

    @Override
    public String toString()
    {
        return preorder(null);
    }


    public void uncompress()
    {

    }

    public void writeCompressed(String outputFile)
    {
        FourZipNode node = new FourZipNode();
    }

    private void writeCompressed(FourZipNode node, BufferedWriter writer)
    {

    }

    public static QTree rawFromFile(String filename)
    {
        return null;
    }

    public static QTree compressedFromFile(String filename)
    {
        return null;
    }
}
