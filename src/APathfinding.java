import java.util.ArrayList;
import java.util.Comparator;

class APathfinding {
    private final ArrayList<Node> borders;
    private final ArrayList<Node> open;
    private final ArrayList<Node> closed;
    private final ArrayList<Node> path;
    private int size, diagonalMoveCost;
    private long runTime;
    private double kValue;
    private Frame frame;
    private Node startNode, endNode, par;
    private boolean diagonal, running, noPath, complete, trig;

    public APathfinding(int size) {
        this.size = size;

        diagonalMoveCost = (int) (Math.sqrt(2 * (Math.pow(size, 2))));
        kValue = Math.PI / 2;
        diagonal = true;
        trig = false;
        running = false;
        complete = false;

        borders = new ArrayList<>();
        open = new ArrayList<>();
        closed = new ArrayList<>();
        path = new ArrayList<>();
    }

    public APathfinding(Frame frame, int size) {
        this.frame = frame;
        this.size = size;

        diagonalMoveCost = (int) (Math.sqrt(2 * (Math.pow(size, 2))));
        kValue = Math.PI / 2;
        diagonal = true;
        trig = false;
        running = false;
        complete = false;

        borders = new ArrayList<>();
        open = new ArrayList<>();
        closed = new ArrayList<>();
        path = new ArrayList<>();
    }

    public APathfinding(Frame frame, int size, Node start, Node end) {
        this.frame = frame;
        this.size = size;
        startNode = start;
        endNode = end;

        diagonalMoveCost = (int) (Math.sqrt(2 * (Math.pow(size, 2))));
        diagonal = true;
        trig = false;
        running = false;
        complete = false;

        borders = new ArrayList<>();
        open = new ArrayList<>();
        closed = new ArrayList<>();
        path = new ArrayList<>();
    }

    public void start(Node s, Node e) {
        running = true;
        startNode = s;
        startNode.setG(0);
        endNode = e;

        // Adding the starting node to the closed list
        addClosed(startNode);

        long startTime = System.currentTimeMillis();

        findPath(startNode);

        complete = true;
        long endTime = System.currentTimeMillis();
        runTime = endTime - startTime;
        System.out.println("Completed: " + runTime + "ms");
    }

    public void setup(Node s, Node e) {
        running = true;
        startNode = s;
        startNode.setG(0);
        par = startNode;
        endNode = e;

        // Adding the starting node to the closed list
        addClosed(startNode);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isComplete() {
        return complete;
    }

    public Node getStart() {
        return startNode;
    }

    public void setStart(Node s) {
        startNode = s;
        startNode.setG(0);
    }

    public Node getEnd() {
        return endNode;
    }

    public void setEnd(Node e) {
        endNode = e;
    }

    public Node getPar() {
        return par;
    }

    public boolean isNoPath() {
        return noPath;
    }

    public boolean isDiagonal() {
        return diagonal;
    }

    public void setDiagonal(boolean d) {
        diagonal = d;
    }

    public boolean isTrig() {
        return trig;
    }

    public void setTrig(boolean t) {
        trig = t;
    }

    public void setSize(int s) {
        size = s;
        diagonalMoveCost = (int) (Math.sqrt(2 * (Math.pow(size, 2))));
    }

    public void findPath(Node parent) {
        Node openNode = null;

        if (diagonal) {
            // Detects and adds one step of nodes to open list
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (i == 1 && j == 1) {
                        continue;
                    }
                    int possibleX = (parent.getX() - size) + (size * i);
                    int possibleY = (parent.getY() - size) + (size * j);

                    // Possible coordinates of borders
                    // Using (crossBorderX, parent.getY())
                    // and (parent.getX(), crossBorderY())
                    // To see if there are borders in the way
                    int crossBorderX = parent.getX() + (possibleX - parent.getX());
                    int crossBorderY = parent.getY() + (possibleY - parent.getY());

                    // Disables ability to cut corners around borders
                    if (searchBorder(crossBorderX, parent.getY()) != -1
                            | searchBorder(parent.getX(), crossBorderY) != -1 && ((j == 0 | j == 2) && i != 1)) {
                        continue;
                    }

                    calculateNodeValues(possibleX, possibleY, parent);
                }
            }
        } else if (!trig) {
            // Detects and adds one step of nodes to open list
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if ((i == 0 && j == 0) || (i == 0 && j == 2) ||
                            (i == 1 && j == 1) || (i == 2 && j == 0) ||
                            (i == 2 && j == 2)) {
                        continue;
                    }
                    int possibleX = (parent.getX() - size) + (size * i);
                    int possibleY = (parent.getY() - size) + (size * j);

                    calculateNodeValues(possibleX, possibleY, parent);
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                // Uses cosine and sine functions to get circle of points
                // around parent
                int possibleX = (int) Math.round(parent.getX() + (-size * Math.cos(kValue * i)));
                int possibleY = (int) Math.round(parent.getY() + (-size * Math.sin(kValue * i)));

                calculateNodeValues(possibleX, possibleY, parent);
            }
        }
        // frame.repaint();

        // Set the new parent node
        parent = lowestFCost();

        if (parent == null) {
            System.out.println("END> NO PATH");
            noPath = true;
            running = false;
            frame.repaint();
            return;
        }

        if (Node.isEqual(parent, endNode)) {
            endNode.setParent(parent.getParent());

            connectPath();
            running = false;
            complete = true;
            frame.repaint();
            return;
        }

        // Remove parent node from open list
        removeOpen(parent);
        // Add parent node to closed list
        addClosed(parent);

        // Allows correction for shortest path during runtime
        // When new parent Node is selected.. Checks all adjacent open
        // Nodes.. Then checks if the (G Score of parent + open Node
        // distance from parent) is less than the current G score
        // of the open node.. If true.. Sets parent of open Node
        // as new parent.. and re-calculates G, and F values
        if (diagonal) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (i == 1 && j == 1) {
                        continue;
                    }
                    int possibleX = (parent.getX() - size) + (size * i);
                    int possibleY = (parent.getY() - size) + (size * j);
                    Node openCheck = getOpenNode(possibleX, possibleY);

                    // If spot being looked at, is an open node
                    if (openCheck != null) {
                        int distanceX = parent.getX() - openCheck.getX();
                        int distanceY = parent.getY() - openCheck.getY();
                        int newG = parent.getG();

                        if (distanceX != 0 && distanceY != 0) {
                            newG += diagonalMoveCost;
                        } else {
                            newG += size;
                        }

                        if (newG < openCheck.getG()) {
                            int s = searchOpen(possibleX, possibleY);
                            open.get(s).setParent(parent);
                            open.get(s).setG(newG);
                            open.get(s).setF(open.get(s).getG() + open.get(s).getH());
                        }
                    }
                }
            }
        }
        if (!frame.showSteps()) {
            findPath(parent);
        } else {
            par = parent;
        }
    }

    private void calculateNodeValues(int possibleX, int possibleY, Node parent) {
        // If the coordinates are outside of the borders
        if (possibleX < 0 | possibleY < 0 | possibleX >= frame.getWidth() | possibleY >= frame.getHeight()) {
            return;
        }

        // If the node is already a border node or a closed node or an
        // already open node, then don't make open node
        if (searchBorder(possibleX, possibleY) != -1 | searchClosed(possibleX, possibleY) != -1
                | searchOpen(possibleX, possibleY) != -1) {
            return;
        }
        // Create an open node with the available x and y
        // coordinates
        Node openNode = new Node(possibleX, possibleY);

        // Set the parent of the open node
        openNode.setParent(parent);

        // Calculating G cost
        // Cost to move from parent node to one open node (x
        // and
        // y
        // separately)
        int GxMoveCost = openNode.getX() - parent.getX();
        int GyMoveCost = openNode.getY() - parent.getY();
        int gCost = parent.getG();

        if (GxMoveCost != 0 && GyMoveCost != 0) {
            gCost += diagonalMoveCost;
        } else {
            gCost += size;
        }
        openNode.setG(gCost);

        // Calculating H Cost
        int HxDiff = Math.abs(endNode.getX() - openNode.getX());
        int HyDiff = Math.abs(endNode.getY() - openNode.getY());
        int hCost = HxDiff + HyDiff;
        openNode.setH(hCost);

        // Calculating F Cost
        int fCost = gCost + hCost;
        openNode.setF(fCost);

        addOpen(openNode);
    }

    private void connectPath() {
        if (getPathList().size() == 0) {
            Node parentNode = endNode.getParent();

            while (!Node.isEqual(parentNode, startNode)) {
                addPath(parentNode);

                for (int i = 0; i < getClosedList().size(); i++) {
                    Node current = getClosedList().get(i);

                    if (Node.isEqual(current, parentNode)) {
                        parentNode = current.getParent();
                        break;
                    }
                }
            }
            reverse(getPathList());
        }

    }

    public void addBorder(Node node) {
        if (borders.size() == 0) {
            borders.add(node);
        } else if (!checkBorderDuplicate(node)) {
            borders.add(node);
        }
    }

    private void addOpen(Node node) {
        if (open.size() == 0) {
            open.add(node);
        } else if (!checkOpenDuplicate(node)) {
            open.add(node);
        }
    }

    private void addClosed(Node node) {
        if (closed.size() == 0) {
            closed.add(node);
        } else if (!checkClosedDuplicate(node)) {
            closed.add(node);
        }
    }

    private void addPath(Node node) {
        if (path.size() == 0) {
            path.add(node);
        } else {
            path.add(node);
        }
    }

    public void removePath(int location) {
        path.remove(location);
    }

    public void removeBorder(int location) {
        borders.remove(location);
    }

    public void removeOpen(int location) {
        open.remove(location);
    }

    private void removeOpen(Node node) {
        for (int i = 0; i < open.size(); i++) {
            if (node.getX() == open.get(i).getX() && node.getY() == open.get(i).getY()) {
                open.remove(i);
                return;
            }
        }
    }

    public void removeClosed(int location) {
        closed.remove(location);
    }

    private boolean checkBorderDuplicate(Node node) {
        for (Node border : borders) {
            if (node.getX() == border.getX() && node.getY() == border.getY()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOpenDuplicate(Node node) {
        for (Node node1 : open) {
            if (node.getX() == node1.getX() && node.getY() == node1.getY()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkClosedDuplicate(Node node) {
        for (Node node1 : closed) {
            if (node.getX() == node1.getX() && node.getY() == node1.getY()) {
                return true;
            }
        }
        return false;
    }

    public int searchBorder(int xSearch, int ySearch) {
        int Location = -1;

        for (int i = 0; i < borders.size(); i++) {
            if (borders.get(i).getX() == xSearch && borders.get(i).getY() == ySearch) {
                Location = i;
                break;
            }
        }
        return Location;
    }

    private int searchClosed(int xSearch, int ySearch) {
        int Location = -1;

        for (int i = 0; i < closed.size(); i++) {
            if (closed.get(i).getX() == xSearch && closed.get(i).getY() == ySearch) {
                Location = i;
                break;
            }
        }
        return Location;
    }

    private int searchOpen(int xSearch, int ySearch) {
        int Location = -1;

        for (int i = 0; i < open.size(); i++) {
            if (open.get(i).getX() == xSearch && open.get(i).getY() == ySearch) {
                Location = i;
                break;
            }
        }
        return Location;
    }

    private void reverse(ArrayList list) {
        int j = list.size() - 1;

        for (int i = 0; i < j; i++) {
            Object temp = list.get(i);
            list.remove(i);
            list.add(i, list.get(j - 1));
            list.remove(j);
            list.add(j, temp);
            j--;
        }
    }

    private Node lowestFCost() {
        if (open.size() > 0) {
            open.sort(Comparator.comparingInt(Node::getF));
            return open.get(0);
        }
        return null;
    }

    public ArrayList<Node> getBorderList() {
        return borders;
    }

    public ArrayList<Node> getOpenList() {
        return open;
    }

    public Node getOpen(int location) {
        return open.get(location);
    }

    public ArrayList<Node> getClosedList() {
        return closed;
    }

    public ArrayList<Node> getPathList() {
        return path;
    }

    public long getRunTime() {
        return runTime;
    }

    public void reset() {
        while (open.size() > 0) {
            open.remove(0);
        }

        while (closed.size() > 0) {
            closed.remove(0);
        }

        while (path.size() > 0) {
            path.remove(0);
        }
        noPath = false;
        running = false;
        complete = false;
    }

    private Node getOpenNode(int x, int y) {
        for (Node node : open) {
            if (node.getX() == x && node.getY() == y) {
                return node;
            }
        }
        return null;
    }

    public void printBorderList() {
        for (Node border : borders) {
            System.out.print(border.getX() + ", " + border.getY());
            System.out.println();
        }
        System.out.println("===============");
    }

    public void printOpenList() {
        for (Node node : open) {
            System.out.print(node.getX() + ", " + node.getY());
            System.out.println();
        }
        System.out.println("===============");
    }

    public void printPathList() {
        for (int i = 0; i < path.size(); i++) {
            System.out.print(i + ": " + path.get(i).getX() + ", " + path.get(i).getY() + ": " + path.get(i).getF());
            System.out.println();
        }
        System.out.println("===============");
    }
}
