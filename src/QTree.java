import java.io.*;

public class QTree
{
    public static int QUAD_SPLIT = -1;

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
        return true;
    }


    public void compress() throws FourZipException
    {

    }

    private FourZipNode	compress(Coordinate start, int size)
    {
        return null;
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
        // makes a new empty matrix
        this.rawImage = new int[this.dim][this.dim];
    }

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
