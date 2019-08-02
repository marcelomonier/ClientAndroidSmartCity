package com.example.clientsmartcity;

import java.net.SocketAddress;
import java.util.List;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.sddl.message.Message;

public class MListener implements NodeConnectionListener {
    @Override
    public void connected(NodeConnection nodeConnection) {

    }

    @Override
    public void reconnected(NodeConnection nodeConnection, SocketAddress socketAddress, boolean b, boolean b1) {

    }

    @Override
    public void disconnected(NodeConnection nodeConnection) {

    }

    @Override
    public void newMessageReceived(NodeConnection nodeConnection, Message message) {
        System.out.println(message.getContentObject().toString());

    }

    @Override
    public void unsentMessages(NodeConnection nodeConnection, List<Message> list) {

    }

    @Override
    public void internalException(NodeConnection nodeConnection, Exception e) {

    }
}
