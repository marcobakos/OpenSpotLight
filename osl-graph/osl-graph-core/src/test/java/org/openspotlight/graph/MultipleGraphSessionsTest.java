package org.openspotlight.graph;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspotlight.common.exception.AbstractFactoryException;
import org.openspotlight.common.util.AbstractFactory;
import org.openspotlight.jcr.provider.DefaultJcrDescriptor;
import org.openspotlight.security.SecurityFactory;
import org.openspotlight.security.idm.AuthenticatedUser;
import org.openspotlight.security.idm.User;

public class MultipleGraphSessionsTest {

    private static SLGraph           graph   = null;
    private static SLGraphSession    session = null;
    private static AuthenticatedUser user;

    @AfterClass
    public static void finish() {
        session.close();
        graph.shutdown();
    }

    @BeforeClass
    public static void init() throws AbstractFactoryException, SLInvalidCredentialsException {
        final SLGraphFactory factory = AbstractFactory.getDefaultInstance(SLGraphFactory.class);
        graph = factory.createGraph(DefaultJcrDescriptor.TEMP_DESCRIPTOR);

        final SecurityFactory securityFactory = AbstractFactory.getDefaultInstance(SecurityFactory.class);
        final User simpleUser = securityFactory.createUser("testUser");
        user = securityFactory.createIdentityManager(DefaultJcrDescriptor.TEMP_DESCRIPTOR).authenticate(simpleUser, "password");
    }

    @Test
    public void testMultipleSessions() throws AbstractFactoryException, SLGraphException, SLInvalidCredentialsException {
        session = graph.openSession(user);
        final SLGraphSession session2 = graph.openSession(user);

        final SLNode abstractTestNode = session.createContext("abstractTest").getRootNode();
        final SLNode node1 = abstractTestNode.addNode("teste!");
        final SLNode testRootNode = session.createContext("test").getRootNode();
        final SLNode node2 = testRootNode.addNode("teste!");

        Assert.assertEquals(false, node1.getID().equals(node2.getID()));

        final String node1ID = node1.getID();
        final String node2ID = node2.getID();

        session.close();

        final SLNode abstractTestNode2 = session2.createContext("abstractTest").getRootNode();
        final SLNode node3 = abstractTestNode2.addNode("teste!");
        final SLNode testRootNode2 = session2.createContext("test").getRootNode();
        final SLNode node4 = testRootNode2.addNode("teste!");

        Assert.assertEquals(false, node3.getID().equals(node4.getID()));

        // I've commeted out the asserts having node1 and node2,
        // because they belong to a closed session. Nodes of a closed session cannot be used any more.
        // Thus, before closing the session I've got the IDs to bo asserted later on ;)
        Assert.assertEquals(false, node1ID.equals(node3.getID()));
        Assert.assertEquals(false, node2ID.equals(node4.getID()));
        //Assert.assertEquals(false, node1.getID().equals(node3.getID()));
        //Assert.assertEquals(false, node2.getID().equals(node4.getID()));

        session2.close();

    }

    @Test
    public void testOpenCloseSessions() throws AbstractFactoryException, SLGraphException, SLInvalidCredentialsException {
        session = graph.openSession(user);

        SLNode abstractTestNode = session.createContext("abstractTest").getRootNode();
        SLNode node1 = abstractTestNode.addNode("teste!");
        SLNode testRootNode = session.createContext("test").getRootNode();
        SLNode node2 = testRootNode.addNode("teste!");

        Assert.assertEquals(false, node1.getID().equals(node2.getID()));
        session.close();
        session = graph.openSession(user);

        abstractTestNode = session.createContext("abstractTest").getRootNode();
        node1 = abstractTestNode.addNode("teste!");
        testRootNode = session.createContext("test").getRootNode();
        node2 = testRootNode.addNode("teste!");

        Assert.assertEquals(false, node1.getID().equals(node2.getID()));
    }

}
