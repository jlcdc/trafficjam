import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: neutronest
 * Date: 15-1-16
 * Time: 上午12:32
 * To change this template use File | Settings | File Templates.
 */
public class TrafficPenalty {
    //根据statusHistory、currentFlowResult计算T(i)时刻某路口滞留车辆数


    private static int computeStay(String key,int time) {


        //左转右转直行实际能够通过的车辆数
        int left_through,right_through,straight_through;
        left_through = right_through =  straight_through = 0;

        //判断是十字路口还是T字路口,-1:三个方向都可以走的路口,0:不能左转的路口,1:不能右转的路口,2:不能直行的路口
        int turn_flag = -1;
        //绿灯时通过率才有效
        int status = TrafficStore.statusHistory.get(key).get(time)[0];
        if (status==1) {
            left_through = TrafficConstant.ThroughRate[0];
        }else if(status==-1){
            turn_flag = 0;
        }
        status = TrafficStore.statusHistory.get(key).get(time)[0];
        if (TrafficStore.statusHistory.get(key).get(time)[1]==1) {
            right_through = TrafficConstant.ThroughRate[1];
        }else if(status==-1){
            turn_flag = 1;
        }
        status = TrafficStore.statusHistory.get(key).get(time)[0];
        if (TrafficStore.statusHistory.get(key).get(time)[2]==1) {
            straight_through = TrafficConstant.ThroughRate[2];
        }else if(status==-1){
            turn_flag = 2;
        }

        //获取当前时段该路口的流量
        int i_flow = TrafficStore.currentFlow.get(key);



        //左转右转直行的滞留车辆
        int left_stay,right_stay,straight_stay;
        left_stay = right_stay = straight_stay = 0;

        //如果是T字路口，则需要改变通行率
        double left_rate = TrafficConstant.TurnRate[0];
        double right_rate = TrafficConstant.TurnRate[1];
        double straight_rate = TrafficConstant.TurnRate[2];
        switch(turn_flag){
            case 0:
                right_rate += left_rate;
                left_rate = 0;
                break;
            case 1:
                left_rate += right_rate;
                right_rate = 0;
                break;
            case 2:
                left_rate += straight_rate*0.5;
                right_rate += straight_rate*0.5;
                straight_rate = 0;
                break;
            default:
                break;
        }

        //上取整可能会导致流量增大一点
        left_stay = Math.max(0, (int)Math.ceil(i_flow*left_rate) - left_through);
        right_stay = Math.max(0, (int)Math.ceil(i_flow*right_rate) - right_through);
        straight_stay = Math.max(0,(int)Math.ceil(i_flow*straight_rate)-straight_through);

        //返回T(i)时刻滞留车辆数
        return (left_stay + right_stay + straight_stay);
    }

    //根据currentStatus计算penalty,该方法只计算某一个十字路口（或则交通路口）
    public static double computePenalty(Map<String, Integer[]> current_traffic_status, int time){
        //代价
        double penalty = 0;
        // 每个路口T(i)时刻penalty: 左转滞留+右转滞留+直行滞留;违反交通规则扣分;违反公平性原则扣分
        for (String key : current_traffic_status.keySet()) {
            // 更新，车辆滞留部分
            penalty += computeStay(key, time);

            // 更新，加上红绿灯违反交通规则的惩罚 a:直行垂直直行惩罚 b:直行垂直左转惩罚
            double a, b;
            a = b = 0;

            String[] lights = key.split("-");
            String left_key = lights[0] + "-" + TrafficStore.trafficLightMap.get(key)[0];
            String right_key = lights[0] + "-" + TrafficStore.trafficLightMap.get(key)[1];

            // 垂直方向不能同时直行
            if (current_traffic_status.get(key)[2] == 1 &&
                    ((current_traffic_status.containsKey(left_key) && current_traffic_status.get(left_key)[2] == 1)
                            || (current_traffic_status.containsKey(right_key)&& current_traffic_status.get(right_key)[2] == 1))) {
                a += TrafficConstant.Zeta * TrafficStore.currentFlow.get(key);
                if (current_traffic_status.containsKey(left_key)) {
                    a += TrafficConstant.Zeta * TrafficStore.currentFlow.get(left_key);
                }
                if (current_traffic_status.containsKey(right_key)) {
                    a += TrafficConstant.Zeta * TrafficStore.currentFlow.get(right_key);
                }
            }
            // 直行时垂直方向右侧不能左转
            if (current_traffic_status.get(key)[2] == 1&& current_traffic_status.containsKey(right_key) && current_traffic_status.get(right_key)[0] == 1) {
                b += TrafficConstant.Zeta* (TrafficStore.currentFlow.get(right_key) + TrafficStore.currentFlow.get(key));
            }

            // 违规扣分
            penalty += (0.5*a + b);

            // 更新，加上违反公平原则扣分 v*sqrt(r-4)
            if (time > 3) {
                for (int j = 0; j < 3; j++) {
                    if (TrafficStore.statusHistory.get(key).get(time)[j] == 0) {
                        int waitTime = 1;
                        int waitStart = time;
                        while (waitStart>0 && TrafficStore.statusHistory.get(key).get(waitStart - 1)[j] == 0) {
                            waitTime += 1;
                            waitStart -= 1;
                        }
                        penalty += Math.ceil(TrafficStore.currentFlow.get(key)* Math.sqrt(Math.max(waitTime - 4, 0)));
                    }
                }
            }

        }

        return penalty;

    }
}
