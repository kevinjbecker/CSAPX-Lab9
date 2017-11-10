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

    /**
     * Create an initially empty tree.
     */
    public QTree()
    {
        this.compressedSize = 0;
        this.dim = 0;
        this.rawImage = null;
        this.rawSize = 0;
        this.root = null;
    }

    /**
     * Get the size of the compressed rawImage.
     *
     * @return Compressed rawImage size.
     *
     * @throws FourZipException If an image has not been compressed, or no compressed image has been read in.
     */
    public int getCompressedSize() throws FourZipException
    {
        if(this.root == null)
            throw new FourZipException("No compressed image yet.");
        return compressedSize;
    }

    /**
     * Get the raw image.
     *
     * @return The raw image.
     *
     * @throws FourZipException If the raw image does not exist (yet).
     */
    public int[][] getRawImage() throws FourZipException
    {
        if(this.rawImage == null)
            throw new FourZipException("No raw image yet.");
        return rawImage;
    }

    /**
     * Get the size of the raw image.
     *
     * @return Raw image size.
     *
     * @throws FourZipException If the raw image does not exist (yet).
     */
    public int getRawSize() throws FourZipException
    {
        if(rawImage == null)
            throw new FourZipException("No raw image yet.");
        return rawSize;
    }

    /**
     * Get the image's square dimension.
     *
     * @return The square dimension.
     */
    public int getSideDim() { return dim; }

    /**
     * This is the core routine for uncompressing an image stored in a file into its raw image (a 2-D array of grayscale
     * values (0-255). The main steps are as follows.
     *
     * 1. Open the compressed image file.
     * 2. Read the file size.
     * 3. Build the FourZip tree from the remaining numerical values in the file.
     *
     * There is only one integer value on each line.
     *
     * @param filename The name of the file containing the compressed image.
     *
     * @return The QTree instance created from the file data.
     *
     * @throws IOException If something goes wrong with the file, including formatting errors.
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
     * Parse the file being read and find the next FourZipNode subtree. This method is called recursively to read and
     * create the node's children.
     *
     * Recursively speaking, the input file stream contains the root node's value followed when appropriate by the
     * string values of each of its sub-nodes, going in a L-to-R, top-to-bottom order (quadrants UL, UR, LL, LR).
     *
     * @param file A file that may have already been partially parsed.
     *
     * @return The root node of the subtree that has been created.
     *
     * @throws IOException If there is any problem with the file, or file format.
     */
    private static FourZipNode parse(BufferedReader file) throws IOException
    {
        // gets the number on the next line
        int line = Integer.parseInt(file.readLine());
        // if number isn't -1, we've reached a termination point, else parse again recursively
        return (line != -1) ? new FourZipNode(line) : new FourZipNode(parse(file), parse(file), parse(file), parse(file));
    }

    /**
     * Create the uncompressed image from the internal FourZip tree.
     *
     * @throws FourZipException If no compressed image has been read in.
     */
    public void uncompress() throws FourZipException
    {
        // sets the raw image array to a new one
        this.rawImage = new int [this.dim][this.dim];

        // runs the uncompress routine
        uncompress(Coordinate.ORIGIN, this.dim, this.root);
    }

    /**
     * Converts a subtree of the FourZip tree into a square section of the raw image matrix. The main idea is that we
     * are working with a tree whose root represents the entire 2^n x 2^n rawImage. We are given a specific node in the
     * tree representing a dim2 x dim2 image. There are two cases:
     *
     * 1. The node is not split. We can write out the corresponding "block" of values into the raw image array based on
     *    the starting point and the given size of the region.
     * 2. The node is split. We must recursively call ourselves with the the four sub-regions. Take note of the pattern
     *    for representing the starting coordinate of the four sub-regions of a 4x4 grid:
     *
     *      upper left: (0, 0)
     *      upper right: (0, 1)
     *      lower left: (1, 0)
     *      lower right: (1, 1)
     *
     * @param coord The coordinate of the upper left corner of the square to be filled.
     * @param dim2 Both the length and width of the square to be filled.
     * @param node The root of the FourZip subtree that will be converted.
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
     * Write the compressed rawImage to the output file. This routine is meant to be called from a client after it has
     * been compressed.
     *
     * @param outFile The name of the file to write the compressed rawImage to.
     *
     * @throws IOException Any errors involved with writing the file out.
     * @throws FourZipException If the file has not been compressed yet.
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
     * The private writer is a recursive helper routine that writes out the compressed rawImage. It goes through the
     * tree in preorder fashion writing out the values of each node as they are encountered.
     *
     * @param node The current node in the tree.
     * @param writer The writer to write the node data out to.
     *
     * @throws IOException If there are issues with the writer.
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
     * Check to see whether a region in the raw image contains the same value. This routine is used by the private
     * compress routine so that it can construct the nodes in the tree.
     *
     * @param start the starting coordinate in the region
     * @param size the size of the region
     *
     * @return Whether the region can be compressed or not.
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
     * Compress a raw image file already read in to this object.
     *
     * @throws FourZipException If there is no raw image (yet).
     */
    public void compress() throws FourZipException
    {
        // if our rawImage is null, throw an error
        if(rawImage == null)
            throw new FourZipException("No raw image to compress yet.");
        // compresses everything
        this.root = compress(Coordinate.ORIGIN, this.dim);
    }

    /**
     * This is the core compression routine. Its job is to work over a region of the rawImage and compress it.
     * It is a recursive routine with two cases:
     *
     * 1. The entire region represented by this rawImage has the same value, or we are down to one pixel.
     *    In either case, we can now create a node that represents this.
     *
     * 2. If we can't compress at this level, we need to divide into 4 equally sized sub-regions and call ourselves
     *    again. Just like with uncompressing, we can compute the starting point of the four sub-regions by using the
     *    starting point and size of the full region.
     *
     * @param start the start coordinate for this region
     * @param size the size this region represents
     *
     * @return a node containing the compression information for the region
     */
    private FourZipNode	compress(Coordinate start, int size)
    {
        // if our size is one or we can compress the block, make a new FourZipNode with no children
        // otherwise we recurse into sub-quadrants
        return (size == 1 || canCompressBlock(start, size)) ? new FourZipNode(this.rawImage[start.getRow()][start.getCol()]):
                new FourZipNode(
                        compress(new Coordinate(start.getRow(), start.getCol()), size/2),
                        compress(new Coordinate(start.getRow(), (start.getCol() + size/2)), size/2),
                        compress(new Coordinate((start.getRow() + size/2), start.getCol()), size/2),
                        compress(new Coordinate((start.getRow() + size/2), (start.getCol() + size/2)), size/2)
                );
    }

    /**
     * Load a raw image. The input file is ASCII text. It contains a series of grayscale values as decimal numbers
     * (0-255). The dimension is assumed square, and is computed from the length of file. There is one value per line.
     *
     * @param inputFile the name of the file representing the raw image
     *
     * @return The QTree instance created from the raw data.
     *
     * @throws IOException if there are issues working with the file
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
     * A preorder (parent, left, right) traversal of a node. It returns a string which is empty if the node is null.
     * Otherwise it returns a string that concatenates the current node's value with the values of the 4 sub-regions
     * (with spaces between).
     *
     * @param node the node being traversed on.
     *
     * @return The string of the node.
     */
    private String preorder(FourZipNode node)
    {
        if(node == null) { return ""; }
        return (node.getValue() != -1) ? Integer.toString(node.getValue()) :
                "(" + preorder(node.getChild(Quadrant.UL)) + " " + preorder(node.getChild(Quadrant.UR)) + " " +
                        preorder(node.getChild(Quadrant.LL)) + " " + preorder(node.getChild(Quadrant.LR)) + ")";
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
