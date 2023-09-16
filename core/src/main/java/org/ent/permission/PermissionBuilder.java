package org.ent.permission;

import org.ent.net.Net;

import java.util.*;
import java.util.function.Consumer;

public class PermissionBuilder {

    private final int numDomains;
    private final NetPermissionBuilder[] netPermissionBuilders;

    public PermissionBuilder(int numDomains) {
        this.numDomains = numDomains;
        this.netPermissionBuilders = new NetPermissionBuilder[numDomains];
    }

    public PermissionBuilder net(Consumer<NetPermissionBuilder> netPermissionBuilderConsumer) {
        netPermissionBuilders[0] = new NetPermissionBuilder();
        netPermissionBuilderConsumer.accept(netPermissionBuilders[0]);
        return this;
    }

    public Permissions buildPermissions(int netIndex) {
        boolean[] canWriteValue = new boolean[numDomains];
        boolean[] canWriteNewNode = new boolean[numDomains];
        boolean[] canWriteArrow = new boolean[numDomains];
        boolean[] canPointTo = new boolean[numDomains];
        boolean[] canExecute = new boolean[numDomains];
        canWriteValue[netIndex] = true;
        canWriteNewNode[netIndex] = true;
        canWriteArrow[netIndex] = true;
        canPointTo[netIndex] = true;
        canExecute[netIndex] = true;
        NetPermissionBuilder netPermissionBuilder = netPermissionBuilders[netIndex];
        if (netPermissionBuilder != null) {
            for (WriteFacet facet : WriteFacet.values()) {
                for (Integer idx : netPermissionBuilder.canWrite.get(facet)) {
                    switch (facet) {
                        case VALUE -> canWriteValue[idx] = true;
                        case NEW_NODE -> canWriteNewNode[idx] = true;
                        case ARROW -> canWriteArrow[idx] = true;
                    }
                }
            }
            for (Integer idx : netPermissionBuilder.canPointTo) {
                canPointTo[idx] = true;
            }
            for (Integer idx : netPermissionBuilder.canExecute) {
                canExecute[idx] = true;
            }
        }
        return new DataPermissions(canWriteValue, canWriteNewNode, canWriteArrow, canPointTo, canExecute);
    }

    public boolean shouldBeExecutable(int netIndex) {
        return Arrays.stream(netPermissionBuilders)
                .filter(Objects::nonNull)
                .flatMap(npm -> npm.canExecute.stream())
                .anyMatch(i -> i == netIndex);
    }

    public static class NetPermissionBuilder {

        private final EnumMap<WriteFacet, List<Integer>> canWrite = new EnumMap<>(WriteFacet.class);
        private final List<Integer> canExecute = new ArrayList<>();
        private final List<Integer> canPointTo = new ArrayList<>();

        public NetPermissionBuilder() {
            for (WriteFacet facet : WriteFacet.values()) {
                canWrite.put(facet, new ArrayList<>());
            }
        }

        public NetPermissionBuilder canExecute(Net net) {
            return canExecute(net.getNetIndex());
        }

        public NetPermissionBuilder canWrite(Net net, WriteFacet facet) {
            return canWrite(net.getNetIndex(), facet);
        }

        public NetPermissionBuilder canWrite(int netIndex, WriteFacet facet) {
            canWrite.get(facet).add(netIndex);
            return this;
        }

        public NetPermissionBuilder canExecute(int netIndex) {
            canExecute.add(netIndex);
            return this;
        }

        public NetPermissionBuilder canPointTo(Net net) {
            return canPointTo(net.getNetIndex());
        }

        private NetPermissionBuilder canPointTo(int netIndex) {
            canPointTo.add(netIndex);
            return this;
        }
    }
}
