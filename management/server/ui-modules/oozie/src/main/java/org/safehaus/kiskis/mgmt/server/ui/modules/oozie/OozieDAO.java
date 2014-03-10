package org.safehaus.kiskis.mgmt.server.ui.modules.oozie;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dilshat
 */
public class OozieDAO {

    private static final Logger LOG = Logger.getLogger(OozieDAO.class.getName());
    private static final DbManager dbManager;
    private static final AgentManager agentManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
        agentManager = ServiceLocator.getService(AgentManager.class);
    }

    public static boolean saveClusterInfo(OozieConfig cluster) {
        try {

//            byte[] data = Util.serialize(cluster);
//
//            String cql = "insert into oozie_info (uid, info) values (?,?)";
//            dbManager.executeUpdate(cql, cluster.getUuid(), ByteBuffer.wrap(data));
            dbManager.saveInfo(OozieModule.MODULE_NAME, cluster.getUuid().toString(), cluster);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveClusterInfo", ex);
            return false;
        }
        return true;
    }

    public static List<OozieConfig> getClusterInfo() {
        List<OozieConfig> list = new ArrayList<OozieConfig>();
        try {
//            String cql = "select * from oozie_info";
//            ResultSet results = dbManager.executeQuery(cql);
//            for (Row row : results) {
//
//                ByteBuffer data = row.getBytes("info");
//
//                byte[] result = new byte[data.remaining()];
//                data.get(result);
//                OozieConfig config = (OozieConfig) deserialize(result);
//                list.add(config);
//            }

            return dbManager.getInfo(OozieModule.MODULE_NAME, OozieConfig.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getClusterInfo", ex);
        }
        return list;
    }

    public static boolean deleteClusterInfo(UUID uuid) {
        try {
//            String cql = "delete from oozie_info where uid = ?";
//            dbManager.executeUpdate(cql, uuid);
            dbManager.deleteInfo(OozieModule.MODULE_NAME, uuid.toString());
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteClusterInfo", ex);
        }
        return false;
    }

//    public static Object deserialize(byte[] bytes) throws ClassNotFoundException, IOException {
//        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//        ObjectInputStream ois = new ObjectInputStream(bais);
//        Object o = ois.readObject();
//        ois.close();
//        return o;
//    }
    public static Set<Agent> getAgents(Set<UUID> uuids) {
        Set<Agent> list = new HashSet<Agent>();
        for (UUID uuid : uuids) {
            Agent agent = agentManager.getAgentByUUIDFromDB(uuid);
            list.add(agent);
        }
        return list;
    }

    public static HadoopClusterInfo getHadoopClusterInfo(String clusterName) {
        HadoopClusterInfo hadoopClusterInfo = null;
        try {
            return dbManager.getInfo(HadoopClusterInfo.SOURCE, clusterName, HadoopClusterInfo.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo(name)", ex);
        }
        return hadoopClusterInfo;
    }
}
