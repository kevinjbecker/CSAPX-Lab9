import java.io.*;

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

    }

    private boolean canCompressBlock(Coordinate start, int size)
    {
        int first = rawImage[start.getRow()][start.getCol()];
        for(int row = start.getRow(); row < start.getRow()+size; row++)
        {
            for(int col = start.getRow(); col < start.getRow()+size; col++)
            {
                if(rawImage[row][col] != first)
                    return false;
            }
        }
        return true;
    }


    public void compress() throws FourZipException
    {
        // compresses everything
        this.root = compress(Coordinate.ORIGIN, this.dim);
    }

    private FourZipNode	compress(Coordinate start, int size)
    {
        return (canCompressBlock(start, size) || size == 1) ? new FourZipNode(this.rawImage[start.getRow()][start.getCol()]):
                new FourZipNode(
                        compress(new Coordinate(start.getRow(), start.getCol()), size/2),
                        compress(new Coordinate(start.getRow(), (start.getCol() + size/2)), size/2),
                        compress(new Coordinate((start.getRow() + size/2), start.getCol()), size/2),
                        compress(new Coordinate((start.getRow() + size/2), (start.getCol() + size/2)), size/2)
                );
    }

    public int getCompressedSize()
    {
        return compressedSize;
    }

    public int[][] getRawImage()
    {
        return rawImage;
    }

    public int getRawSize()
    {
        return rawSize;
    }

    public int getSideDim()
    {
        return dim;
    }

    private static FourZipNode parse(BufferedReader file) throws IOException
    {
        int line = Integer.parseInt(file.readLine());
        return (line != -1) ? new FourZipNode(line) : new FourZipNode(parse(file), parse(file), parse(file), parse(file));
    }

    private String preorder(FourZipNode node)
    {
        return (node.getValue() != -1) ? Integer.toString(node.getValue()) :
                "(" + preorder(node.getChild(Quadrant.UL)) + ", " + preorder(node.getChild(Quadrant.UR)) + ", " +
                        preorder(node.getChild(Quadrant.LL)) + ", " + preorder(node.getChild(Quadrant.LR)) + ")";
    }


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

        // if this node has children, keep going
        if(node.getValue() != -1)
        {
            for (int row = coord.getRow(); row < coord.getRow() + dim2; row++)
            {
                for(int col = coord.getCol(); col < coord.getCol() + dim2; col++)
                {
                    rawImage[row][col] = node.getValue();
                }
            }
            return;
        }
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
     * @param outputFile
     * @throws IOException
     */
    public void writeCompressed(String outputFile) throws IOException
    {
        FourZipNode node = new FourZipNode();
        FileWriter fw = new FileWriter(outputFile);
        BufferedWriter writer = new BufferedWriter(fw);

        writeCompressed(null, writer);
    }

    private void writeCompressed(FourZipNode node, BufferedWriter writer)
    {

    }

    public static QTree rawFromFile(String filename) throws IOException
    {
        // initially makes an empty QTree
        QTree tree = new QTree();

        BufferedReader file = new BufferedReader(new FileReader(filename));

        return tree;
    }

    public static QTree compressedFromFile(String filename) throws IOException
    {
        // initially makes an empty QTree
        QTree tree = new QTree();

        // constructs a new BufferedReader reading in the file
        BufferedReader file = new BufferedReader(new FileReader(filename));

        // the compressed size is the number of lines in the file
        tree.rawSize = Integer.parseInt(file.readLine());
        // the dimension is the square-root of the first line
        tree.dim = (int)Math.round(Math.sqrt(tree.rawSize));

        // set the root of the tree to be the return of the parse file on the remaining lines
        tree.root = parse(file);

        // return our newly generated tree
        return tree;
    }

    @Override
    public String toString()
    {
        // returns the preorder traversal of the root
        return preorder(this.root);
    }
}
