package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class TomcatPortScanner {
    public static void main(String[] args) {
        int startPort = 1; // Starting port
        int endPort = 9000; // 65535; // Ending port

        for (int port = startPort; port <= endPort; port++) {
            if (isPortOpen(port)) {
                String pid = getProcessId(port);
                if (pid != null) {
                    String processName = getProcessName(pid);
                  
                    //////////////////////
                    // System.out.println("Port " + port + " is open. PID: " + pid + ", Process Name: " + processName);

                    // Check if the process is Tomcat
                   String[] serviceNamesToCheck = {"apache","Apache","tomcat", "Tomcat", "javaw.exe"};
                   for (String word : serviceNamesToCheck) {
                	    if (processName.toLowerCase().contains(word)) {
                	    	System.out.println("Tomcat is possibly running on port: " + port + " with PID: " + pid + ", Process Name: " + processName);
                	    	
                	    	//
                	    	 String host = "http://localhost"; // Change this if needed
                	         // port: Possible port Tomcat is running on
                	         String contextPath = "/"; // "/your-web-app"; // Change to your web app context path
                	         int returnCode = isWebAppRunning(host, port, contextPath);
                	         if (returnCode != 0) {
                	              // System.out.println("The web application is " +"\u001B[32m"+ "RUNNING"+"\u001B[0m" + " on port: "+ "\u001B[32m"+ port+ "\u001B[0m" +". Code: " +"\u001B[32m"
                	              //		 + returnCode +"\u001B[0m"+ "\n-------------------------------------------");
                	        	  System.out.println("The web application is "  + "RUNNING"  + " on port: "+  port  + ". Code: " 
               	                 		 + returnCode  + "\n-------------------------------------------");
               	     
                	         } else {
                	             // System.out.println("The web application is "+"\u001B[31m"+ "NOT" + "\u001B[0m" + " running on port: " + "\u001B[31m" + port +"\u001B[0m" 
                	             //		 + "\n-------------------------------------------");
                	        	 System.out.println("The web application is "+ "NOT"   + " running on port: "  + port  
                        	            + "\n-------------------------------------------");
                	         } 
                	        
                	    }
                   }
                    
                }
            }
        }
        
        System.out.println("\nScan finished. Scanned ports "+ startPort+" to "+endPort);
    }
 

    private static int isWebAppRunning(String host, int port, String contextPath) {
        try {
            URL url = new URL(host + ":" + port + contextPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000); // 2 seconds timeout
            connection.setReadTimeout(2000); // 2 seconds read timeout

            int responseCode = connection.getResponseCode();
            return responseCode; // responseCode == HttpURLConnection.HTTP_OK; // Check for 200 OK
        } catch (IOException e) {
            return 0; // Handle exceptions (e.g., connection issues)
        }
    }
    
    private static boolean isPortOpen(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return true; // Port is open
        } catch (IOException e) {
            return false; // Port is closed
        }
    }

    private static String getProcessId(int port) {
        String pid = null;

        // Command to find PID using netstat
        String command = "netstat -ano | findstr :" + port;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // The last column is usually the PID
                    String[] parts = line.trim().split("\\s+");
                    pid = parts[parts.length - 1]; // Get the PID
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return pid;
    }

    private static String getProcessName(String pid) {
        String processName = "Unknown";

        // Command to find process name using tasklist
        String command = "tasklist /FI \"PID eq " + pid + "\"";

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip the first line (header)
                    if (line.startsWith("Image Name")) {
                        continue;
                    }
                    // Process name is the first column
                    String[] parts = line.trim().split("\\s+");
                    processName = parts[0]; // Get the process name
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return processName;
    }
}
