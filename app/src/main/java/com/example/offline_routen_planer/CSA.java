package com.example.offline_routen_planer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class CSA {
    /*
     * Expected arrival times for each stop (=String). Consists of the time, an
     * index for the last connection/transfer used and a transfer flag all stored in
     * an Array within a hashmap.
     */
    HashMap<Integer, int[]> arrivalTimes = new HashMap<Integer, int[]>(); // stop_id-->[arrivalTime][connection_last||transfer][Transfer-Flag]

    // id/name-pairs of stops
    List<Stop> names = new ArrayList<Stop>();
    public List<String> autoCompleteNames = new ArrayList<String>();

    // HashMaps for calendars
    HashMap<Integer, Integer> inTrip = new HashMap<Integer, Integer>(); // trip_id-->service_id
    HashMap<Integer, int[]> service_schedule = new HashMap<Integer, int[]>();// service_id-->[M,D,W,T,F,S,S,start,end]
    HashMap<Integer, HashMap<Integer, Integer>> service_dates = new HashMap<Integer, HashMap<Integer, Integer>>();// service_id-->calendar_dates
    HashMap<Integer, Integer> calendar_dates; // exception_date-->exception_type

    // HashMaps for ID-Hashing
    HashMap<String, Integer> trip_id_map = new HashMap<String, Integer>();
    HashMap<String, Integer> stop_id_map = new HashMap<String, Integer>();
    HashMap<String, Integer> service_id_map = new HashMap<String, Integer>();
    int counter = 0;

    // array of all connections
    int[][] connectionList = null; // [dep_stop][arr_stop][dep_time][arr_time][trip_id]
    int[] dep_stops, arr_stops, dep_times, arr_times, trip_ids = null;

    // array of all transfers
    int[][] transfers = null; // [dep_stop][arr_stop][transfer_time]

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDD");
    //Calendar cal = Calendar.getInstance();

    IO io = new IO();
    int start_time_final = 0;
    int footTime = 0;

    protected void makeConnections() {
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString();
        path = path.replace("\\", "//") +"//VVS_Data//Fahrplandaten//";
        BufferedReader br;

        // ******************initialize arrival times*****************************
        // stop_id-->[arrivalTime][connection_last||transfer][Transfer-Flag]

         //br =
        // io.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//Test_Data//stops.txt");
        br = io.readFile("C:\\Users\\Jonas\\Desktop\\Bachelorarbeit\\git\\VVS_Data\\Fahrplandaten\\stops.txt");
        //br = io.readFile(path+"stops.txt");
        String sCurrentLine = null;

        try {
            while ((sCurrentLine = br.readLine()) != null) {
                String[] line = sCurrentLine.split("~");
                int[] value = { Integer.MAX_VALUE, -1, 0 };
                int stop_id = map(stop_id_map, line[1]);
                arrivalTimes.put(stop_id, value);
                String name = replaceUmlaut(line[3]);
                Stop stop = new Stop(stop_id, Integer.MAX_VALUE, name, -1); // just used for names
                names.add(stop);
                if(!autoCompleteNames.contains(name)) {
                    autoCompleteNames.add(name);
                }
            }
            counter = 0;
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // *********************initialize calendar*********************************
        // service_id-->[M,D,W,T,F,S,S,start,end]

         BufferedReader bk =
         io.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//Test_Data//calendar.txt");
		//BufferedReader bk = io
		//		.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//VVS_Data//Fahrplandaten//calendar.txt");
        //BufferedReader bk = io.readFile(path+"calendar.txt");
        try {
            while ((sCurrentLine = bk.readLine()) != null) {
                String[] line = sCurrentLine.split("~");
                int service_id = map(service_id_map, line[1]);
                int[] value = { service_id, Integer.parseInt(line[3]), Integer.parseInt(line[5]),
                        Integer.parseInt(line[7]), Integer.parseInt(line[9]), Integer.parseInt(line[11]),
                        Integer.parseInt(line[13]), Integer.parseInt(line[15]), Integer.parseInt(line[17]),
                        Integer.parseInt(line[19]) };
                service_schedule.put(service_id, value);
            }
            counter = 0;
            bk.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // *******************initialize calendar_dates******************************
        // exception_date-->exception_type

         BufferedReader bl =
        io.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//Test_Data//calendar_dates.txt");
		//BufferedReader bl = io
		//		.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//VVS_Data//Fahrplandaten//calendar_dates.txt");
        //BufferedReader bl = io.readFile(path+"calendar_dates.txt");
        try {

            int service_id = -1;
            while ((sCurrentLine = bl.readLine()) != null) {
                String[] line = sCurrentLine.split("~");
                int newID = service_id_map.get(line[1]);

                if (service_id != newID) {
                    service_dates.put(service_id, calendar_dates);
                    service_id = newID;
                    calendar_dates = new HashMap<Integer, Integer>();
                    calendar_dates.put(Integer.parseInt(line[3]), Integer.parseInt(line[5]));

                } else {
                    calendar_dates.put(Integer.parseInt(line[3]), Integer.parseInt(line[5]));
                }
            }
            bl.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // *********************initialize inTrip*********************************
        // trip_id-->service_id

         BufferedReader bf =
         io.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//Test_Data//trips.txt");
		//BufferedReader bf = io
		//		.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//VVS_Data//Fahrplandaten//trips.txt");
        //BufferedReader bf = io.readFile(path+"trips.txt");
        try {
            while ((sCurrentLine = bf.readLine()) != null) {
                String[] line = sCurrentLine.split("~");
                int trip_id = map(trip_id_map, line[5]);
                int service_id = service_id_map.get(line[3]);

                inTrip.put(trip_id, service_id);

            }
            counter = 0;
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ***********************initialize transfers**********************************
        // [dep_stop][arr_stop][transfer_time]

		BufferedReader bj1 = io
				.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//Test_Data//Transfers.txt");
        //BufferedReader bj1 = io.readFile(path+"transfers.txt");
        //BufferedReader bj2 = io.readFile(path+"transfers.txt");
		BufferedReader bj2 = io
				.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//Test_Data//Transfers.txt");
        // BufferedReader bj =
        // io.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//Test_Data//transfers.txt");

        int number = 0;
        try {
            while ((sCurrentLine = bj1.readLine()) != null) {
                counter++; //count transfers

            }
            bj1.close();
            transfers = new int[counter][3];
            counter = 0;

            while ((sCurrentLine = bj2.readLine()) != null) {
                String[] line = sCurrentLine.split("~");
                // if (line[5].equals("2")) { //for unchanged data base
                transfers[number][0] = stop_id_map.get(line[1]);
                transfers[number][1] = stop_id_map.get(line[3]);
                transfers[number][2] = Integer.parseInt(line[7]);
                number++;
            }
            bj2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // *********************makeConnections************************************
        // [dep_stop][arr_stop][dep_time][arr_time][trip_id]

        // BufferedReader bh =
        // io.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//Test_Data//stop_times.txt");
		BufferedReader bh1 = io
				.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//VVS_Data//Fahrplandaten//stop_times.txt");
        //BufferedReader bh1 = io.readFile(path+"stop_times.txt");
		BufferedReader bh2 = io
				.readFile("C://Users//Jonas//Desktop//Bachelorarbeit//git//VVS_Data//Fahrplandaten//stop_times.txt");
        //BufferedReader bh2 = io.readFile(path+"stop_times.txt");
        // connectionList = io.read();

        int dep = 0;
        int arr = 0;
        int trip_id = 0;
        int dep_time = 0;
        int arr_time = 0;
        int index = 0;

        try {
            while ((sCurrentLine = bh1.readLine()) != null) {
                counter++; //count transfers

            }
            bh1.close();
            connectionList = new int[counter][5];
            dep_stops = new int[counter];
            arr_stops = new int[counter];
            dep_times = new int[counter];
            arr_times = new int[counter];
            trip_ids = new int[counter];
            counter = 0;
            while ((sCurrentLine = bh2.readLine()) != null) {

                String[] line = sCurrentLine.split(",");

                for (int i = 0; i < line.length; i++) {
                    line[i] = line[i].substring(1, line[i].length() - 1); // remove ""
                }

                if (Integer.parseInt(line[4]) != 1) { // same trip
                    arr_time = convertTimeToInt((line[1]));
                    arr = stop_id_map.get(line[3]);
                    int[] value = { dep, arr, dep_time, arr_time, trip_id };
                    connectionList[index] = value;
                    index++;
                    dep_time = convertTimeToInt((line[2])); // if arr_stop is next dep_stop
                    dep = arr;
                } else { // new trip
                    dep_time = convertTimeToInt((line[2]));
                    dep = stop_id_map.get(line[3]);
                    trip_id = trip_id_map.get(line[0]);
                }
            }
            bh2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void sort() {
       // Arrays.sort(connectionList, (a, b) -> Double.compare(a[2], b[2]));
        //splitList();
        // io.save(connectionList);
    }

    /**
     * split multidimensional array into single ones for better efficiency
     */
    protected void splitList() {
        for (int i = 0; i < connectionList.length; i++) {


            dep_stops[i] = connectionList[i][0];
            arr_stops[i] = connectionList[i][1];
            dep_times[i] = connectionList[i][2];
            arr_times[i] = connectionList[i][3];
            trip_ids[i] = connectionList[i][4];
        }
    }

    @SuppressWarnings("unchecked")
    protected void csa(String start, String end, int start_time, int date) {

        start = replaceUmlaut(start);
        end = replaceUmlaut(end);
        List<Integer> startStops = new ArrayList<Integer>();
        List<Integer> endStops = new ArrayList<Integer>();
        HashMap<Integer, int[]> arrivalTimes = (HashMap<Integer, int[]>) this.arrivalTimes.clone();

        // add start_time to all start_stops
        for (Stop stop : names) {
            if (start.equals(stop.getStop_name())) {
                //stop.setArrivalTime(start_time);
                int[] value = { start_time, -1, 0 };
                arrivalTimes.replace(stop.getStop_id(), value);
                startStops.add(stop.getStop_id());
            } else if (end.equals(stop.getStop_name())) {
                endStops.add(stop.getStop_id());
            }
        }

        // add footpaths to startStops
        for (int i = 0; i < transfers.length; i++) {
            if (startStops.contains(transfers[i][0]) && !(startStops.contains(transfers[i][1]))) {
                int[] value = { start_time + convertTime(transfers[i][2]), i, 1 };
                arrivalTimes.replace(transfers[i][1], value);
            }

        }
        int randomEndStop = endStops.get(0);
        for (int i = binarySearch(start_time); i < connectionList.length; i++) {

            int dep_time = dep_times[i];
            if (dep_time > arrivalTimes.get(randomEndStop)[0]) { // stop criteria
                break;
            } else {
                if (drivesToday(trip_ids[i], date)) {
                    int dep_stop = dep_stops[i];
                    int arr_time = arr_times[i];

                    if ((arrivalTimes.get(dep_stop)[0] <= dep_time)) { // check if reachable

                        // inTrip.replace(trip_id, true); // stay in trip

                        int arr_stop = arr_stops[i];
                        if (arrivalTimes.get(arr_stop)[0] > arr_time) {
                            arrivalTimes.replace(arr_stop, new int[] { arr_time, i, 0 }); //get off

                            // add footpaths to current arr_stop
                            for (int j = 0; j < transfers.length; j++) {

                                if (transfers[j][0] == arr_stop) {
                                    int newOne = arr_time + convertTime(transfers[j][2]);
                                    if (newOne < arrivalTimes.get(transfers[j][1])[0]) {
                                        arrivalTimes.replace(transfers[j][1], new int[] { newOne, j, 1 } );
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        // get best destination of endStops
        int earliestArr = Integer.MAX_VALUE;
        int destination = 0;
        for (Integer stop_id : endStops) {
            if (earliestArr > arrivalTimes.get(stop_id)[0]) {
                earliestArr = arrivalTimes.get(stop_id)[0];
                destination = stop_id;
            } else if (earliestArr == arrivalTimes.get(stop_id)[0]) {
                if (arrivalTimes.get(stop_id)[2] == 0) {
                    earliestArr = arrivalTimes.get(stop_id)[0];
                    destination = stop_id;
                }
            }
        }
        printPath(destination, start_time, earliestArr, arrivalTimes);
        System.out.println(" ");
        System.out.println("Reisezeit: " + (convertTravelTime(start_time_final, earliestArr)+footTime) + " min");
    }

    /**
     * checks whether current connection schedules today
     *
     * @param trip_id
     * @param simpleDate
     * @return boolean
     */
    @SuppressWarnings("deprecation")
    private boolean drivesToday(int trip_id, int simpleDate) {
        int service_id = inTrip.get(trip_id);

        // find exceptions

        if (service_dates.containsKey(service_id)) {
            if (service_dates.get(service_id).containsKey(simpleDate)) {
                if (service_dates.get(service_id).get(simpleDate) == 2) {
                    return false;
                } else {
                    return true;
                }
            }}

//		for (int i = 0; i < exceptions.length; i++) {
//			if(service_id == exceptions[i][0] && simpleDate == exceptions[i][1]) {
//				if(exceptions[i][2] == 2) {
//					return false;
//				} else {
//					return true;
//				}
//			}
//		}


        int[] service = service_schedule.get(service_id);
        if (service[8] <= simpleDate && simpleDate <= service[9]) {

            try {
//				cal.setTime(sdf.parse("" + simpleDate)); //with Calendar
//				switch (cal.DAY_OF_MONTH) {
                switch(sdf.parse("" + simpleDate).getDay()) {
                    case 0:
                        if (service[7] == 1) {
                            return true;
                        } else {
                            return false;
                        }
                    case 1:
                        if (service[1] == 1) {
                            return true;
                        } else {
                            return false;
                        }
                    case 2:
                        if (service[2] == 1) {
                            return true;
                        } else {
                            return false;
                        }
                    case 3:
                        if (service[3] == 1) {
                            return true;
                        } else {
                            return false;
                        }
                    case 4:
                        if (service[4] == 1) {
                            return true;
                        } else {
                            return false;
                        }
                    case 5:
                        if (service[5] == 1) {
                            return true;
                        } else {
                            return false;
                        }
                    case 6:
                        if (service[6] == 1) {
                            return true;
                        } else {
                            return false;
                        }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            return false;
        }

        return false;
    }

    /**
     * finds fastest path from startStop to destination backwardly
     *
     * @param destination
     */
    public void printPath(int destination, int startTime, int endTime, HashMap<Integer, int[]> arrivalTimes) {
        Stack<int[]> stack = new Stack<int[]>();


        for (int i = binarySearch(endTime); i != -1; i--) {
            if(dep_times[i] < startTime) { //end criteria
                break;
            }
            if (arrivalTimes.get(destination)[2] == 0) {// connection was taken

                if (arrivalTimes.get(destination)[1] == i) {
                    int index = arrivalTimes.get(destination)[1];
                    int[] value = { index, 0 };
                    stack.push(value);
                    destination = dep_stops[index];
                }
            } else if (arrivalTimes.get(destination)[2] == 1) {// footpath was taken
                int index = arrivalTimes.get(destination)[1];
                int[] value = { index, 1 };
                stack.push(value);
                destination = transfers[index][0];
            }
        }
        int[] temp = stack.peek();
        if(temp[1] == 1) {
            footTime = transfers[temp[0]][2] / 60;
            printConnections(stack.pop());
        }
        start_time_final = dep_times[stack.peek()[0]]; //for calculating travel time
        while (!stack.isEmpty()) {
            printConnections(stack.pop());
        }
    }

    protected void printConnections(int[] value) {
        if (value[1] == 0) {// connection was taken
            System.out.println(getName(dep_stops[value[0]]) + " -> " + getName(arr_stops[value[0]]) + " "
                    + trip_ids[value[0]] + " " + convertDisplayTime(dep_times[value[0]]) + " "
                    + convertDisplayTime(arr_times[value[0]]));
        } else {// footpath was taken
            if(transfers[value[0]][2] == 0) {
                System.out.println("Umsteigen");
            } else {
                System.out.println(getName(transfers[value[0]][0]) + " -> " + getName(transfers[value[0]][1]) + " Fußweg: "
                        + transfers[value[0]][2] / 60 + " min");
            }
        }
    }

    /**
     *
     * @param hash
     * @return stop_name
     */
    protected String getName(int hash) {
        for (Stop stop : names) {
            if (hash == stop.getStop_id()) {
                return stop.getStop_name();
            }

        }
        return "not found";
    }

    private int map(HashMap<String, Integer> map, String id) {
        Integer i = map.get(id);
        if (i == null) {
            map.put(id, counter);
            i = counter;
            ++counter;
        } else {
            System.out.println("duplicate");
        }
        return i;
    }

    /**
     * skipping connections before start_time with binarySearch
     *
     * @param startTime
     * @return index
     */
    private int binarySearch(int startTime) {
        int left = 0;
        int right = connectionList.length - 1;
        int middle = 0;
        while (left <= right) {
            middle = (left + right) / 2;
            if (dep_times[middle] < startTime) {
                left = middle + 1;
            } else if (dep_times[middle] > startTime) {
                right = middle - 1;
            } else {
                return middle;
            }
        }
        return middle;
    }

    /**
     * converts hh::mm:ss into hhmmss
     *
     * @param time
     * @return
     */
    private int convertTimeToInt(String time) {
        String[] field = time.split(":");
        time = "" + field[0] + field[1] + field[2];

        return Integer.parseInt(time);
    }

    /**
     * converts time in seconds into "mmss" format
     *
     * @param time
     * @return
     */
    public int convertTime(int time) {
        int sekunden = time % 60;
        int minuten = (time - sekunden) / 60;
        return minuten * 100 + sekunden;
    }

    /**
     * converts "hhmmss" format into "hh:mm""
     *
     * @param time
     * @return
     */
    public String convertDisplayTime(int time) {
        time = time / 100;
        if (time % 100 < 10) {
            return time / 100 + ":0" + time % 100;
        }
        return time / 100 + ":" + time % 100;
    }

    /**
     * calculates the difference between two times
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public int convertTravelTime(int startTime, int endTime) {
        startTime /= 100;
        endTime /= 100;
        startTime = (startTime / 100) * 60 + startTime % 100;
        endTime = (endTime / 100) * 60 + endTime % 100;
        return endTime - startTime;
    }

    public static String replaceUmlaut(String input) {

        // replace all lower Umlauts
        String o_strResult = input.replaceAll("ü", "ue").replaceAll("ö", "oe").replaceAll("ä", "ae").replaceAll("ß",
                "ss");

        // first replace all capital umlaute in a non-capitalized context (e.g. Übung)
        o_strResult = o_strResult.replaceAll("Ü(?=[a-zäöüß ])", "Ue").replaceAll("Ö(?=[a-zäöüß ])", "Oe")
                .replaceAll("Ä(?=[a-zäöüß ])", "Ae");

        // now replace all the other capital umlaute
        o_strResult = o_strResult.replaceAll("Ü", "UE").replaceAll("Ö", "OE").replaceAll("Ä", "AE");

        return o_strResult;
    }public static void main(String[] args) {



    }

}

