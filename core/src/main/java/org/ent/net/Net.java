package org.ent.net;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang3.Validate;
import org.ent.Ent;
import org.ent.Profile;
import org.ent.listener.MultiNetEventListener;
import org.ent.listener.NetEventListener;
import org.ent.listener.NopNetEventListener;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.Hub;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.util.ReferentialGarbageCollection;
import org.ent.permission.Permissions;
import org.ent.permission.PermissionsViolatedException;
import org.ent.permission.WriteFacet;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.ent.permission.Permissions.DOUBLE_CHECK_PERMISSIONS;

public class Net {

    private static final boolean VALIDATE = true;
    private static final boolean VALIDATE_MORE = false;

    private int netIndex;
    private String name;
    private Ent ent;

    private final List<Node> nodes;
    /**
     * Node names used for serialization and debugging. Saved here for consistency in
     * different output contexts.
     * <p>
     * Usually not set in computations where performance is of concern.
     */
    private BiMap<Node, String> nodeNames;
    /**
     * Annotations/labels that can be used for output and debugging.
     * <p>
     * Usually not set in computations where performance is of concern.
     */
    private Map<Node, String> nodeAnnotations;
    /**
     * Set of nodes that should be printed or drawn on output,
     * even if they are disconnected from the root.
     */
    private Set<Node> secondaryRoots;
    /**
     * 'sparse' indicates that the list of nodes may contain null entries.
     * A dense (non-sparse) net allows for more optimized algorithms.
     * This flag is not maintained automatically, but needs to be
     * set after an operation that leaves the net sparse (e.g. garbage collection).
     * An algorithm can throw an error for sparse Nets, if it cannot
     * handle them (fail fast).
     */
    private boolean sparse;

    private int currentIndex;
    private Node root;

    private boolean markerNodePermitted;

    private MarkerNode markerNode;

    NetEventListener netEventListener = new NopNetEventListener();

    private Permissions permissions;

    private boolean executable;

    public Net(Ent ent) {
        this.ent = ent;
        this.nodes = new ArrayList<>();
    }

    public Net() {
        this.nodes = new ArrayList<>();
    }

    public Net(int noNodes) {
        this.nodes = new ArrayList<>(noNodes);
    }

    public Ent getEnt() {
        return ent;
    }

    public int getNetIndex() {
        return netIndex;
    }

    public Net setNetIndex(int netIndex) {
        this.netIndex = netIndex;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Permissions getPermissions() {
        if (permissions == null) {
            permissions = new NoPermissions();
        }
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public Net addEventListener(NetEventListener netEventListener) {
        if (this.netEventListener.getClass() == NopNetEventListener.class) {
            this.netEventListener = netEventListener;
        } else if (this.netEventListener instanceof MultiNetEventListener multiNetEventListener) {
            multiNetEventListener.addNetEventListener(netEventListener);
        } else {
            NetEventListener current = this.netEventListener;
            MultiNetEventListener multiNetEventListener = new MultiNetEventListener();
            multiNetEventListener.addNetEventListener(current);
            multiNetEventListener.addNetEventListener(netEventListener);
            this.netEventListener = multiNetEventListener;
        }
        return this;
    }

    public NetEventListener event() {
        return netEventListener;
    }

    public NetEventListener event(Permissions permissions) {
        if (permissions == Permissions.DIRECT) {
            return NopNetEventListener.INSTANCE;
        } else {
            return netEventListener;
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public boolean isSparse() {
        return sparse;
    }

    public void setSparse(boolean sparse) {
        this.sparse = sparse;
    }

    public void addNode(Node node) {
        if (node.getNet() != null) {
            throw new IllegalArgumentException("net in node must be unset");
        }
        node.setNet(this);
        addNodeInternal(node);
    }

    public void addNodes(Collection<Node> nodes) {
        nodes.forEach(this::addNode);
    }

    public void removeNode(Node node) {
        if (VALIDATE) {
            if (node.getNet() != this) {
                throw new IllegalStateException("node does not belong to this net");
            }
        }
        int index = node.getIndex();
        nodes.set(index, null);
        if (nodeNames != null) {
            nodeNames.remove(node);
        }
    }

    public void removeNodeIf(Predicate<? super Node> filter) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node != null && filter.test(node)) {
                nodes.set(i, null);
                if (nodeNames != null) {
                    nodeNames.remove(node);
                }
            }
        }
    }

    public List<Node> removeAllNodes() {
        List<Node> formerNodes = new ArrayList<>(this.nodes);
        formerNodes.forEach(n -> n.setNet(null));
        nodes.clear();
        if (nodeNames != null) {
            nodeNames.clear();
        }
        return formerNodes;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(@NotNull Node root) {
        if (root.getNet() != this) {
            throw new IllegalStateException("Root must be one of the net nodes");
        }
        event().setRoot(this.root, root);
        this.root = root;
    }

    @VisibleForTesting
    void validateBelongsToNet(@NotNull Node node) {
        Validate.notNull(node);
        if (VALIDATE) {
            if (node.getNet() != this) {
                throw new IllegalStateException("node belongs to another net");
            }
            if (VALIDATE_MORE) {
                if (node != markerNode && !nodes.contains(node)) {
                    throw new IllegalStateException("node does not belong to this net");
                }
            }
        }
    }

    public MarkerNode permitMarkerNode() {
        this.markerNodePermitted = true;
        this.markerNode = new MarkerNode(this);
        return this.markerNode;
    }

    public void forbidMarkerNode() {
        if (VALIDATE) {
            consistencyCheck(); // check marker is not referenced
        }
        this.markerNodePermitted = false;
        this.markerNode = null;
    }

    public boolean isMarkerNodePermitted() {
        return markerNodePermitted;
    }

    public MarkerNode getMarkerNode() {
        return markerNode;
    }

    public void consistencyCheck() {
        if (root == null) {
            throw new AssertionError("Root is null");
        }
        if (VALIDATE_MORE) {
            if (!this.nodes.contains(root)) {
                throw new AssertionError("Root must be one of the net nodes");
            }
        }
        for (Node node : nodes) {
            consistencyCheck(node);
        }
    }

    private void consistencyCheck(Node node) {
        if (node instanceof MarkerNode) {
            throw new AssertionError("Net node must not be a marker node");
        }
        if (node.getNet() != this) {
            throw new AssertionError("Node belongs to another net");
        }
        for (Arrow arrow : node.getArrows()) {
            consistencyCheck(arrow);
        }
    }

    private void consistencyCheck(Arrow arrow) {
        Node child = arrow.getTarget(Permissions.DIRECT);
        if (child instanceof MarkerNode) {
            if (!markerNodePermitted) {
                throw new AssertionError("Child of node is marker node, but they are not permitted");
            } else if (child != markerNode) {
                throw new AssertionError("Child of node is marker node, but not the designated one");
            }
        } else {
            if (VALIDATE_MORE) {
                if (!nodes.contains(child)) {
                    throw new AssertionError("Child of node must be part of the net");
                }
            }
        }
    }

    public void referentialGarbageCollection() {
        new ReferentialGarbageCollection(this).run();
    }

    public void runWithMarkerNode(Consumer<MarkerNode> consumer) {
        MarkerNode marker = permitMarkerNode();
        consumer.accept(marker);
        forbidMarkerNode();
    }

    public void ancestorExchange(Node node1, Node node2, Permissions permissions) {
        if (DOUBLE_CHECK_PERMISSIONS) {
            if (permissions.noWrite(node1.getNet(), WriteFacet.ARROW)) {
                throw new PermissionsViolatedException();
            }
        }
        Net net = node1.getNet();
        net.validateBelongsToNet(node2);
        Hub hub1 = node1.getHub();
        Hub hub2 = node2.getHub();
        node1.setHub(hub2);
        hub2.setNode(node1);
        node2.setHub(hub1);
        hub1.setNode(node2);
        event(permissions).ancestorExchange(node1, node2);
    }

    public Node newRoot(Permissions permissions) {
        Node newRoot = newNode(permissions);
        setRoot(newRoot);
        return newRoot;
    }

    @VisibleForTesting
    public Node newRoot() {
        Profile.verifyTestProfile();
        return newRoot(Permissions.DIRECT);
    }

    public Node newNode(int value, Node leftChild, Node rightChild) {
        Profile.verifyTestProfile();
        return newNode(value, leftChild, rightChild, Permissions.DIRECT);
    }

    public Node newNode(int value, Node leftChild, Node rightChild, Permissions permissions) {
        if (DOUBLE_CHECK_PERMISSIONS) {
            if (permissions.noWrite(netIndex, WriteFacet.NEW_NODE)) {
                throw new PermissionsViolatedException();
            }
        }
        Node node = new Node(this, value, leftChild, rightChild);
        addNodeInternal(node);
        event(permissions).calledNewNode(node);
        return node;
    }

    public Node newNode(int value, Optional<Node> leftChild, Optional<Node> rightChild, Permissions permissions) {
        if (DOUBLE_CHECK_PERMISSIONS) {
            if (permissions.noWrite(netIndex, WriteFacet.NEW_NODE)) {
                throw new PermissionsViolatedException();
            }
        }
        leftChild.ifPresent(this::validateBelongsToNet);
        rightChild.ifPresent(this::validateBelongsToNet);
        Node node = new Node(this, value, leftChild, rightChild);
        addNodeInternal(node);
        event(permissions).calledNewNode(node);
        return node;
    }

    @VisibleForTesting
    public Node newNode(int value, Optional<Node> leftChild, Optional<Node> rightChild) {
        Profile.verifyTestProfile();
        return newNode(value, leftChild, rightChild, Permissions.DIRECT);
    }

    public Node newNode(Permissions permissions) {
        if (DOUBLE_CHECK_PERMISSIONS) {
            if (permissions.noWrite(netIndex, WriteFacet.NEW_NODE)) {
                throw new PermissionsViolatedException();
            }
        }
        Node bNode = new Node(this);
        addNodeInternal(bNode);
        event(permissions).calledNewNode(bNode);
        return bNode;
    }

    @VisibleForTesting
    public Node newNode() {
        Profile.verifyTestProfile();
        return newNode(Permissions.DIRECT);
    }

    public Node newNode(Node leftChild, Node rightChild, Permissions permissions) {
        if (DOUBLE_CHECK_PERMISSIONS) {
            if (permissions.noWrite(netIndex, WriteFacet.NEW_NODE)) {
                throw new PermissionsViolatedException();
            }
        }
        validateBelongsToNet(leftChild);
        validateBelongsToNet(rightChild);
        Node bNode = new Node(this, leftChild, rightChild);
        addNodeInternal(bNode);
        event(permissions).calledNewNode(bNode);
        return bNode;
    }

    @VisibleForTesting
    public Node newNode(Node leftChild, Node rightChild) {
        Profile.verifyTestProfile();
        return newNode(leftChild, rightChild, Permissions.DIRECT);
    }

    public Node newNode(Command command, Permissions permissions) {
        return newNode(command.getValue(), permissions);
    }

    @VisibleForTesting
    public Node newNode(int value) {
        Profile.verifyTestProfile();
        return newNode(value, Permissions.DIRECT);
    }

    public Node newNode(int value, Permissions permissions) {
        if (DOUBLE_CHECK_PERMISSIONS) {
            if (permissions.noWrite(netIndex, WriteFacet.NEW_NODE)) {
                throw new PermissionsViolatedException();
            }
        }
        Node cNode = new Node(this, value);
        addNodeInternal(cNode);
        event(permissions).calledNewNode(cNode);
        return cNode;
    }

    public Node newUNode(Node child, Permissions permissions) {
        if (DOUBLE_CHECK_PERMISSIONS) {
            if (permissions.noWrite(netIndex, WriteFacet.NEW_NODE)) {
                throw new PermissionsViolatedException();
            }
        }
        Node uNode = new Node(this, child);
        addNodeInternal(uNode);
        event(permissions).calledNewNode(uNode);
        return uNode;
    }

    @VisibleForTesting
    public Node newUNode(Node child) {
        Profile.verifyTestProfile();
        return newUNode(child, Permissions.DIRECT);
    }

    public Node newCNode(Command command, Permissions permissions) {
        return newNode(command, permissions);
    }

    @VisibleForTesting
    public Node newCNode(Command command) {
        Profile.verifyTestProfile();
        return newNode(command, Permissions.DIRECT);
    }

    public Node newCNode(int value, Permissions permissions) {
        return newNode(value, permissions);
    }

    @VisibleForTesting
    public Node newCNode(int value) {
        Profile.verifyTestProfile();
        return newNode(value, Permissions.DIRECT);
    }

    private void addNodeInternal(Node node) {
        node.setIndex(currentIndex);
        nodes.add(node);
        if (nodes.size() - 1 != currentIndex) {
            throw new AssertionError();
        }
        currentIndex++;
    }

    public void setName(Node node, String name) {
        validateBelongsToNet(node);
        if (nodeNames == null) {
            nodeNames = HashBiMap.create();
        }
        nodeNames.put(node, name);
    }

    public String getName(Node node) {
        if (nodeNames == null) {
            return null;
        }
        return nodeNames.get(node);
    }

    public Node getByName(String name) {
        if (nodeNames == null) {
            return null;
        }
        return nodeNames.inverse().get(name);
    }

    public void setAnnotation(Node node, String annotation) {
        validateBelongsToNet(node);
        if (nodeAnnotations == null) {
            nodeAnnotations = new HashMap<>();
        }
        nodeAnnotations.put(node, annotation);
    }

    public void appendAnnotation(Node node, String annotation) {
        validateBelongsToNet(node);
        if (nodeAnnotations == null) {
            nodeAnnotations = new HashMap<>();
        }
        nodeAnnotations.merge(node, annotation, (current, ann) -> current + "|" + ann);
    }

    public String getAnnotation(Node node) {
        if (nodeAnnotations == null) {
            return null;
        }
        return nodeAnnotations.get(node);
    }

    public Map<Node, String> getAnnotations() {
        return nodeAnnotations;
    }

    public void addSecondaryRoot(Node node) {
        if (node.getNet() != this) {
            throw new AssertionError();
        }
        if (secondaryRoots == null) {
            secondaryRoots = new LinkedHashSet<>();
        }
        secondaryRoots.add(node);
    }

    public Set<Node> getSecondaryRoots() {
        return secondaryRoots;
    }

    public List<Node> getNodesAsList() {
        return nodes;
    }

    public Node getNode(int index) {
        return nodes.get(index);
    }

    public String format() {
        return new NetFormatter().format(this);
    }

    class NoPermissions implements Permissions {
        @Override
        public boolean noWrite(int indexTarget, WriteFacet facet) {
            return netIndex != indexTarget;
        }

        @Override
        public boolean noPointTo(int indexTarget) {
            return netIndex != indexTarget;
        }

        @Override
        public boolean noExecute(Net net) {
            return true;
        }
    }
}
