package net.sf.juffrou.mq.activemq.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Programmatic JMX example
 * 
 */
public class AMQJmxExampleTest extends TestCase {
    private static final Log log = LogFactory.getLog(AMQJmxExampleTest.class);

    public String amqJmxUrl = "service:jmx:rmi:///jndi/rmi://apaxsys004:1099/jmxrmi";

 
    public void testApp() {
        MBeanServerConnection connection;
        
        String clientServiceName = "org.apache.activemq:ContainerName=ServiceMix,Type=SystemService,Name=ClientFactory";
        
        String topicName = "Boulder.Colorado";
        
        try {
            // Acquire a connection to the MBean server
            connection = connect();
            
            // How many MBeans are running? 
            count(connection);
            
            // Query for a single MBean 
            query(connection, clientServiceName);
            
            // Query all MBeans 
            query(connection, "");
            
            // Check for the topic first 
            Set mbeans = queryForTopic(connection, topicName);
            
            log.info("Located [" + mbeans.size() + "] MBeans");
            
            if(mbeans.size() > 0) {
                // Create a new topic on the broker
                createTopic(connection, topicName);
            }
            else {
                // Remove the topic from the broker and then create it
                removeTopic(connection, topicName);
                createTopic(connection, topicName);
            }
            
            mbeans = queryForTopic(connection, topicName);
            
            log.info("Located [" + mbeans.size() + "] MBeans");
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public MBeanServerConnection connect() 
        throws IOException {
        JMXConnector connector = null;
        MBeanServerConnection connection = null;

        String username = "";

        String password = "";

        Map env = new HashMap();
        String[] credentials = new String[] { username, password };
        env.put(JMXConnector.CREDENTIALS, credentials);

        try {
            connector = JMXConnectorFactory.newJMXConnector(new JMXServiceURL(amqJmxUrl), env);
            connector.connect();
            connection = connector.getMBeanServerConnection();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return connection; 
    }
    
    public void count(MBeanServerConnection conn)
        throws IOException {
        int numberOfMBeans = conn.getMBeanCount().intValue();
        log.info("Number of MBeans currently running: " + numberOfMBeans);
    }
    
    public void query(MBeanServerConnection conn, String query)
        throws IOException {
        if (conn != null && query != null) {
            listMBeans(conn, query);
        } else if (conn != null && query.equals("")) {
            listAllMBeanNames(conn);
        } else {
            log.fatal("Unable to connect to ServiceMix");
        }
    }
    
    public void listMBeans(MBeanServerConnection conn, String query) 
        throws IOException {
        ObjectName name;
        Set names = null; 
        try {
            name = new ObjectName(query);
            names = conn.queryMBeans(name, name);
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        for(Iterator iter = names.iterator(); iter.hasNext(); ) {
            ObjectInstance obj = (ObjectInstance) iter.next();
            log.info("+ " + obj.getClassName());
        }
    }
    
    public void listAllMBeanNames(MBeanServerConnection conn)
        throws IOException {
        Set names = getAllMBeanNames(conn);

        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName objName = (ObjectName) iter.next();
            log.info("+ " + objName);
        }
    }
    
    public void listMBeanAttrs(MBeanServerConnection conn, String query) 
        throws IOException {
        ObjectName objName = null;
        try {
            objName = new ObjectName(query);
            log.info("+ " + objName.getCanonicalName()); 
            
            MBeanInfo info = getMBeanInfo(conn, objName); 
            MBeanAttributeInfo[] attrs = info.getAttributes(); 
            
            for(int i = 0; i < attrs.length; ++i) {
                Object obj = conn.getAttribute(objName, attrs[i].getName());
                log.info("  - " + attrs[i].getName() + obj);
            }
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void createTopic(MBeanServerConnection conn, String topicName)
        throws IOException {
        String brokerNameQuery = "org.apache.activemq:BrokerName=localhost,Type=Broker";
        String addTopicOperationName = "addTopic";
        Object[] params = { topicName };
        String[] sig = { "java.lang.String" };
        
        doTopicCrud(conn, topicName, brokerNameQuery, addTopicOperationName, params, sig, "creat");
    }
    
    private void removeTopic(MBeanServerConnection conn, String topicName) 
        throws IOException {
        String brokerNameQuery = "org.apache.activemq:BrokerName=localhost,Type=Broker";
        String removeTopicOperationName = "removeTopic";
        Object[] params = { topicName };
        String[] sig = { "java.lang.String" };
        
        doTopicCrud(conn, topicName, brokerNameQuery, removeTopicOperationName, params, sig, "remov");
    }

    private void doTopicCrud(MBeanServerConnection conn, String topicName, 
            String brokerNameQuery, String operationName, Object[] params, String[] sig, String verb) 
        throws IOException {
        ObjectName brokerObjName;
        
        try {
                log.info( verb + "ing new topic: [" + topicName + "]");
                brokerObjName = new ObjectName(brokerNameQuery); 
                conn.invoke(brokerObjName, operationName, params, sig);
                log.info("Topic [" + topicName + "] has been " + verb + "ed");
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    

    private void printMBeans(String topicName, Set mbeans) {
        if (!mbeans.isEmpty()) {
            for (Iterator iter = mbeans.iterator(); iter.hasNext();) {
                ObjectInstance mbean = (ObjectInstance) iter.next();
                log.info("+ " + mbean.getClassName());
            }
        } 
        else {
            log.info("Unable to locate MBean for " + topicName);
        }
    }

    public Set queryForTopic(MBeanServerConnection conn, String topicName) 
        throws IOException {
        // Was the topic created?  
        String topicsQuery = "org.apache.activemq:BrokerName=localhost,Type=Topic,*";
        // listMBeans(conn, topicsQuery);

        // Use JMX query expressions
        QueryExp queryExp = Query.eq(Query.attr("name"), Query.value(topicName)); 

        ObjectName objName;
        Set mbeans = null; 
        try {
            objName = new ObjectName(topicsQuery);
            log.info("Querying for " + topicName);
            mbeans = conn.queryMBeans(objName, queryExp);       
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return mbeans;
    }

    public void listAllMBeanAttrs(MBeanServerConnection conn, Set names) 
        throws IOException {

        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName objName = (ObjectName) iter.next();
            log.info("+ " + objName);

            MBeanInfo info = getMBeanInfo(conn, objName);
            
            MBeanAttributeInfo[] attrs = info.getAttributes();

            if (attrs == null)
                continue;

            for (int i = 0; i < attrs.length; ++i) {
                try {
                    Object obj = conn.getAttribute(objName, attrs[i].getName());
                    log.info(" - " + attrs[i].getName() + " = " + obj);
                } catch (NullPointerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (AttributeNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InstanceNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MBeanException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ReflectionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void query(MBeanServerConnection conn, QueryExp queryExp)
        throws IOException {
        log.info("Not yet implemented"); 
    }
    
    // Private ----------------------------------------------------------------

    private MBeanInfo getMBeanInfo(MBeanServerConnection conn, ObjectName objName) 
        throws IOException {
        MBeanInfo info = null;
        
        try {
            info = conn.getMBeanInfo((ObjectName) objName);
        } catch (InstanceNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IntrospectionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ReflectionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        return info;
    }
    
    private Set getAllMBeanNames(MBeanServerConnection conn)
        throws IOException {
        return conn.queryNames(null, null);
    }
}
