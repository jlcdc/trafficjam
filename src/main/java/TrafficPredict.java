import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: neutronest
 * Date: 15-1-11
 * Time: 下午5:15
 * To change this template use File | Settings | File Templates.
 */


public class TrafficPredict {


    public static int randomStatus() {

        double rand = Math.random();
        if(rand >= 0.5) return 1;
        else return 0;
    }

    public static void generateStatus(int time) throws IOException {

        // the best status
        Map<String, Integer[]> bestStatus = new HashMap<String, Integer[]>();
        // the current status
        Map<String, Integer[]> currentStatus = new HashMap<String, Integer[]>();

        // traversal all the traffic node
        Iterator<?> it = TrafficStore.trafficLightNodeMap.entrySet().iterator();
        while(it.hasNext()) {
            // each init
            double bestPenalty = Integer.MAX_VALUE;
            bestStatus.clear();

            Map.Entry<String, String[]> trafficLightItem = (Map.Entry<String, String[]>)it.next();
            String itemLightId = (String)trafficLightItem.getKey();
            // all four origin direction!
            String[] fromIds = (String[])trafficLightItem.getValue();

            // random algorithm
            // the search_num is constant
            for( int search_i = 0; search_i<200; search_i++) {

                // for each center node, calculate all four from-id-direction
                currentStatus.clear();
                for(String fromId: fromIds) {

                    // not dead end
                    if(!"#".equals(fromId)) {

                        String key = itemLightId + "-" + fromId;
                        //
                        String[] destination = TrafficStore.trafficLightMap.get(key);

                        Integer[] tempStatus = new Integer[3];
                        for(int idx = 0; idx < 3; idx++) {

                            if ("#".equals(destination[idx])) {

                                tempStatus[idx] = -1;
                            } else {

                                if( idx == 1) {

                                    tempStatus[idx] = 1;
                                } else {
                                    // random
                                    tempStatus[idx] = randomStatus();
                                }
                            }
                        }

                        currentStatus.put(key, tempStatus);
                    }
                }

                TrafficStore.setStatus(currentStatus, time);
                double penalty = TrafficPenalty.computePenalty(currentStatus, time);
                if( bestPenalty > penalty) {
                    bestPenalty = penalty;
                    for(String key: currentStatus.keySet()) {

                        bestStatus.put(key, currentStatus.get(key));
                    }
                }
            }
            // update the status
            TrafficStore.setStatus(bestStatus, time);
            Iterator<?> it2 = bestStatus.entrySet().iterator();
            while(it2.hasNext())  {

                Map.Entry<String, Integer[]> itemStatus = (Map.Entry<String, Integer[]>) it2.next();
                TrafficStore.trafficStatus.put(itemStatus.getKey(), itemStatus.getValue());
            }
        }
        TrafficStore.printCurrentStatusResult();
    }

    public static String readAllDataFromFile(String pathname) throws IOException {

        File filename = new File(pathname);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
        BufferedReader br = new BufferedReader(reader);
        String line = "";
        line = br.readLine();
        while(line != null) {
            line += ";";
            line += br.readLine();
        }

        return line;
    }

    public static void main(String[] args) throws  NumberFormatException, IOException {

        TrafficStore.TrafficLightInit();

        // release
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String flows_str = br.readLine();

        // String pathname = "/Users/neutronest/projects/traffic/benchmark/flow0901.txt";
        // String flows_str = readAllDataFromFile(pathname);
        int time = 0;
        while(!"end".equalsIgnoreCase(flows_str)) {

            //TODO
            // all clear
            TrafficStore.trafficStatus.clear();
            TrafficStore.currentFlow.clear();

            // parse the flow
            String[] flows = flows_str.trim().split(";");
            for(String flow: flows) {

                String[] strs = flow.trim().split(",");
                String key = strs[0] + "-" + strs[1];
                TrafficStore.currentFlow.put(key, Integer.valueOf(strs[2]));
            }


            if( time % 120 == 0) {
                time = time % 120;
                TrafficStore.statusHistory.clear();
            }

            TrafficStore.CurrentStatusInit();
            generateStatus(time);

            flows_str = br.readLine();
            time++;
        }
    }





}
