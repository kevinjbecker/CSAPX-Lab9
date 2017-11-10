import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class QTree
{
    public static int QUAD_SPLIT = 4;

    private int compressedSize;
    private int dim;
    private int [][] rawImage;
    private int rawSize;
    private FourZipNode root;

    public QTree()
    {
        this.compressedSize = 0;
        this.dim = 0;
        this.rawImage = null;
        this.rawSize = 0;
        this.root = null;
    }

    /**
     *
     * @param start
     * @param size
     * @return
     */
    private boolean canCompressBlock(Coordinate start, int size)
    {
        // gets the first pixel as a reference
        int ref = rawImage[start.getRow()][start.getCol()];

        // if we run into a mismatch, we can't compress (at least without any loss of data)
        for(int row = start.getRow(); row < start.getRow()+size; ++row)
            for(int col = start.getCol(); col < start.getCol()+size; ++col)
                if(rawImage[row][col] != ref)
                    return false;

        // if all of the blocks are the same, we are set to compress
        return true;
    }

    /**
     *
     * @throws FourZipException
     */
    public void compress() throws FourZipException
    {
        // compresses everything
        this.root = compress(Coordinate.ORIGIN, this.dim);
    }

    /**
     *
     * @param start
     * @param size
     * @return
     */
    private FourZipNode	compress(Coordinate start, int size)
    {
        return (size == 1 || canCompressBlock(start, size)) ? new FourZipNode(this.rawImage[start.getRow()][start.getCol()]):
                new FourZipNode(
                        compress(new Coordinate(start.getRow(), start.getCol()), size/2),
                        compress(new Coordinate(start.getRow(), (start.getCol() + size/2)), size/2),
                        compress(new Coordinate((start.getRow() + size/2), start.getCol()), size/2),
                        compress(new Coordinate((start.getRow() + size/2), (start.getCol() + size/2)), size/2)
                );
    }

    /**
     *
     * @return
     */
    public int getCompressedSize()
    {
        return compressedSize;
    }

    /**
     *
     * @return
     */
    public int[][] getRawImage()
    {
        return rawImage;
    }

    /**
     *
     * @return
     */
    public int getRawSize()
    {
        return rawSize;
    }

    /**
     *
     * @return
     */
    public int getSideDim()
    {
        return dim;
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static FourZipNode parse(BufferedReader file) throws IOException
    {
        int line = Integer.parseInt(file.readLine());
        return (line != -1) ? new FourZipNode(line) : new FourZipNode(parse(file), parse(file), parse(file), parse(file));
    }

    /**
     *
     * @param node
     * @return
     */
    private String preorder(FourZipNode node)
    {
        return (node.getValue() != -1) ? Integer.toString(node.getValue()) :
                "(" + preorder(node.getChild(Quadrant.UL)) + ", " + preorder(node.getChild(Quadrant.UR)) + ", " +
                        preorder(node.getChild(Quadrant.LL)) + ", " + preorder(node.getChild(Quadrant.LR)) + ")";
    }

    /**
     *
     * @throws FourZipException
     */
    public void uncompress() throws FourZipException
    {
        // sets the raw image array to a new one
        this.rawImage = new int [this.dim][this.dim];

        // runs the uncompress routine
        uncompress(Coordinate.ORIGIN, this.dim, this.root);
    }

    /**
     *
     * @param coord
     * @param dim2
     * @param node
     */
    private void uncompress(Coordinate coord, int dim2, FourZipNode node)
    {

        // if this node doesn't have children, set the rawImage to our value
        if(node.getValue() != -1)
        {
            for (int row = coord.getRow(); row < coord.getRow() + dim2; ++row)
                for(int col = coord.getCol(); col < coord.getCol() + dim2; ++col)
                    rawImage[row][col] = node.getValue();
            return;
        }

        // otherwise keep going
        // upper left original row, original column
        uncompress(new Coordinate(coord.getRow(), coord.getCol()), dim2/2, node.getChild(Quadrant.UL));
        // upper right, original row, column + remaining columns/2
        uncompress(new Coordinate(coord.getRow(), (coord.getCol() + dim2/2)), dim2/2, node.getChild(Quadrant.UR));
        // lower left original row + remaining rows/2, original column
        uncompress(new Coordinate(coord.getRow() + dim2/2, coord.getCol()), dim2/2, node.getChild(Quadrant.LL));
        // lower right original row + remaining rows/2 , column + remaining columns/2
        uncompress(new Coordinate(coord.getRow() + dim2/2, coord.getCol() + dim2/2), dim2/2, node.getChild(Quadrant.LR));
    }

    /**
     *
     * @param outFile
     * @throws IOException
     */
    public void writeCompressed(String outFile) throws IOException, FourZipException
    {
        // if the root is null, image hasn't been compressed yet
        // throw this back to the compressor
        if(this.root == null)
            throw new FourZipException("Image has not been compressed yet.");

        // make a new writer with outFile
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        // first writes the size of the file
        writer.write((this.dim*this.dim) + "\n");
        writer.flush();

        // then, writes the tree
        writeCompressed(this.root, writer);
    }

    /**
     *
     * @param node
     * @param writer
     * @throws IOException
     */
    private void writeCompressed(FourZipNode node, BufferedWriter writer) throws IOException
    {
        // adds 1 to compressed size
        ++compressedSize;
        writer.write(node.getValue() + "\n");
        writer.flush();

        // if this node has children
        if(node.getValue() == -1)
        {
            // writes the upper left child
            writeCompressed(node.getChild(Quadrant.UL), writer);
            // writes the upper right child
            writeCompressed(node.getChild(Quadrant.UR), writer);
            // writes the lower left child
            writeCompressed(node.getChild(Quadrant.LL), writer);
            // writes the lower right child
            writeCompressed(node.getChild(Quadrant.LR), writer);
        }
    }

    /**
     *
     * @param inputFile
     * @return
     * @throws IOException
     */
    public static QTree rawFromFile(String inputFile) throws IOException
    {
        // initially makes an empty QTree
        QTree tree = new QTree();

        // reads in all the lines from inputFile at once and puts them in a List
        List<String> file = Files.readAllLines(Paths.get(inputFile), Charset.defaultCharset());

        // number of lines in the file
        tree.rawSize = file.size();
        // sets the size of a side in the image
        tree.dim = (int)Math.round(Math.sqrt(tree.rawSize));
        // makes an empty rawImage array
        tree.rawImage = new int[tree.dim][tree.dim];

        // goes through each number and sets the image's greyscale value
        for(int row = 0; row < tree.dim; ++row)
            for(int col = 0; col < tree.dim; ++col)
                tree.rawImage[row][col] = Integer.parseInt(file.remove(0));

        return tree;
    }

    /**
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static QTree compressedFromFile(String filename) throws IOException
    {
        // initially makes an empty QTree
        QTree tree = new QTree();

        // constructs a new BufferedReader reading in the file
        BufferedReader file = new BufferedReader(new FileReader(filename));

        // the compressed size is the number of lines in the file
        tree.rawSize = Integer.parseInt(file.readLine());
        // set compressedSize to one since we just read the first line
        tree.compressedSize = 1;
        // the dimension is the square-root of the first line
        tree.dim = (int)Math.round(Math.sqrt(tree.rawSize));

        // set the root of the tree to be the return of the parse file on the remaining lines
        tree.root = parse(file);

        // return our newly generated tree
        return tree;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
        // returns the preorder traversal of the root
        return preorder(this.root);
    }
}
