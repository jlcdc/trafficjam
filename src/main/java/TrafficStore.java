import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: neutronest
 * Date: 15-1-16
 * Time: 上午12:29
 * To change this template use File | Settings | File Templates.
 */
public class TrafficStore {

    public static Map<String, String[]> trafficLightMap =
            new HashMap<String, String[]>();

    public static Map<String, String[]> trafficLightNodeMap =
            new HashMap<String, String[]>();

    public static Map<String, Integer[]> trafficStatus =
            new HashMap<String, Integer[]>();

    public static Map<String, ArrayList<Integer[]>> statusHistory =
            new HashMap<String, ArrayList<Integer[]>>();

    public static Map<String, Integer> currentFlow =
            new HashMap<String, Integer>();




    // ------------------- Begin Init Method ----------------------

    public static void TrafficLightInit() {

        // get each node of the traffic graph
        String[] graphString = TrafficConstant.TrafficGraph.split(";");

        for (String graphItem: graphString) {

            String[] itemString = graphItem.split(",");
            // key format: "tl1-tl2"(the node, origin node)
            String lightKey = itemString[0] + "-" + itemString[1]; // maybe use StringBuffer better here.
            // value format: "tl3, tl4, tl5" (left node, right node, straight node)
            String[] lightValue = {itemString[2], itemString[3], itemString[4]};
            trafficLightMap.put(lightKey, lightValue);

            String lightNodeKey = itemString[0];
            String[] lightNodeValue = {itemString[1], itemString[2], itemString[3], itemString[4]};
            trafficLightNodeMap.put(lightNodeKey, lightNodeValue);
        }
    }

    public static void CurrentStatusInit() {

        for(String key: trafficLightMap.keySet()) {

            Integer[] status = new Integer[]{-1, -1, -1};
            if(statusHistory.containsKey(key)) {

                statusHistory.get(key).add(status);
            } else {

                ArrayList<Integer[]> statusArray = new ArrayList<Integer[]>();
                statusArray.add(status);
                statusHistory.put(key, statusArray);
            }
        }
    }

    public static void ParseFlowFrom

    // ------------------- End Init Method

    // ------------------- Change Method

    public static void setStatus(Map<String, Integer[]> statusItems, int time) {

        for(String key: statusItems.keySet()) {

            statusHistory.get(key).get(time)[0] = statusItems.get(key)[0];
            statusHistory.get(key).get(time)[1] = statusItems.get(key)[1];
            statusHistory.get(key).get(time)[2] = statusItems.get(key)[2];
        }
    }

    // ------------------- End Change Method

    // ------------------- Output Method

    public static void printCurrentStatusResult() throws IOException {

        Iterator<Map.Entry<String,Integer[]>> it =
                trafficStatus.entrySet().iterator();

        StringBuilder sb = new StringBuilder();
        while(it.hasNext()) {

            Map.Entry<String, Integer[]> entry = it.next();
            String[] keyStrs = entry.getKey().split("-");
            Integer[] status = entry.getValue();

            // TrafficLightID, FromID
            sb.append(keyStrs[0] + "," + keyStrs[1]);
            for(int s: status) {
                sb.append("," + s);
            }
            sb.append(";");
        }
        String statusStr = sb.toString();
        System.out.println(statusStr);
    }
}


