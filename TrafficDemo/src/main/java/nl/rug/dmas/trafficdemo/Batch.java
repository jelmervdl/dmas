/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import nl.rug.dmas.trafficdemo.actors.Driver;
import nl.rug.dmas.trafficdemo.streetgraph.GraphReader;
import nl.rug.dmas.trafficdemo.streetgraph.StreetGraph;

/**
 *
 * @author jelmer
 */
public class Batch {
    static public Batch read(File graphFile, File input) throws Exception {
        StreetGraph graph = GraphReader.read(graphFile);
        
        Scanner scanner = new Scanner(input);
        
        Pattern sectionHeader = Pattern.compile("\\[(.+?)\\]");
        Pattern sectionEntry = Pattern.compile("(.+?)\\s*=\\s*(.+?)");
        
        Map<String, Map<String,Parameter>> sections = new HashMap<>();
        
        Map<String,Parameter> section = new HashMap<>();
        sections.put("global", section);
        
        while (scanner.hasNext()) {
            if (scanner.hasNext(sectionHeader)) {
                String next = scanner.next(sectionHeader);
                String sectionName = scanner.match().group(1);

                if (sections.containsKey(sectionName)) {
                    section = sections.get(sectionName);
                } else {
                    section = new HashMap<>();
                    sections.put(sectionName, section);
                }
            }
            else {
                String name = scanner.next();
                
                if (!scanner.hasNext("=")) {
                    throw new Exception("Expected equals symbol after " + name);
                }
                
                scanner.next("="); // consume the equals sign
                String value = scanner.nextLine().trim();
                
                try {
                    section.put(name, Parameter.fromString(value));
                } catch (IllegalArgumentException e) {
                    System.err.println(String.format("Could not parse \"%s\"", value));
                }
            }
        }
        
        // Print config for debugging purpose
        printConfig(sections);
        
        int iterations = (int) sections.get("global").get("iterations").getValue(null);
        
        float time = sections.get("global").get("time").getValue(null);
        
        List<Scenario> scenarios = new ArrayList<>();
        
        for (Map.Entry<String, Parameter> entry : sections.get("variable").entrySet()) {
            for (float value : entry.getValue()) {
                for (int i = 0; i < iterations; ++i) {
                    Scenario scenario = createScenario(graph, sections.get("fixed"));

                    Field field = Scenario.class.getField(entry.getKey());
                    field.set(scenario, new Parameter.Fixed(value));

                    scenarios.add(scenario);
                }
            }
        }
        
        return new Batch(scenarios, time);
    }
    
    private static void printConfig(final Map<String, Map<String,Parameter>> sections) {
        for (Map.Entry<String, Map<String,Parameter>> section : sections.entrySet()) {
            System.err.println("[" + section.getKey() + "]");
            
            for (Map.Entry<String,Parameter> entry : section.getValue().entrySet()) {
                System.err.println(String.format("%s = %s", entry.getKey(), entry.getValue()));
            }
        }
    }

    private static Scenario createScenario(StreetGraph graph, Map<String, Parameter> parameters) throws IllegalAccessException, NoSuchFieldException {
        Scenario scenario = new Scenario(graph, System.currentTimeMillis());
        
        for (Map.Entry<String,Parameter> entry : parameters.entrySet()) {
            Field field = Scenario.class.getField(entry.getKey());
            field.set(scenario, entry.getValue());
        }
        
        return scenario;
    }
    
    static class Measurer extends ScenarioAdapter {
        static class Entry {
            public Class driver;
            public String route;
            public float drivingTime;
            
            public Entry(Class driver, String route, float drivingTime) {
                this.driver = driver;
                this.route = route;
                this.drivingTime = drivingTime;
            }
        }
        
        List<Entry> entries = new ArrayList<>();
        
        @Override
        public void carRemoved(Car car) {
            if (car.getDriver().reachedDestination()) {
                Driver driver = car.getDriver();
                entries.add(new Entry(
                        driver.getClass(),
                        driver.getPath().toString(),
                        driver.getDrivingTime()));
            }
        }
    }
    
    private final List<Scenario> scenarios;
    
    private final Map<Scenario, Measurer> statistics = new HashMap<>();
    
    private final float time;
    
    public Batch(List<Scenario> scenarios, float time) {
        this.scenarios = scenarios;
        this.time = time;
    }
    
    public void run(int concurrent) {
        ExecutorService threadPool = Executors.newFixedThreadPool(concurrent);
        List<Future<?>> futures = new ArrayList<>(scenarios.size());
        
        for (Scenario scenario : scenarios) {
            Measurer measurer = new Measurer();
            scenario.addListener(measurer);
            
            statistics.put(scenario, measurer);
            
            futures.add(threadPool.submit(scenario.getJumpLoop(time)));
        }
        
        // Wait for all things to finish
        threadPool.shutdown();
        
        while (!threadPool.isTerminated()) {
            int completedFutures = 0;
            
            for (Future<?> future : futures) {
                if (future.isDone()) {
                    completedFutures += 1;
                }
            }
            
            System.err.println(String.format("%d/%d completed", completedFutures, futures.size()));
            
            try {
                threadPool.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // still waiting
            }
        }
        
        List<String> headers = new ArrayList<>();
        headers.add("Scenario");
        
        List<Field> fields = new ArrayList<>();
        
        for (Field field : Scenario.class.getFields()) {
                if (Parameter.class.isAssignableFrom(field.getType())) {
                    fields.add(field);
                    headers.add(field.getName());
                }
        }
        
        headers.add("Driver");
        headers.add("Route");
        headers.add("DrivingTime");
        
        // Print the headers
        printRow(headers, System.out);
        
        for (Map.Entry<Scenario, Measurer> entry : statistics.entrySet()) {
            Scenario scenario = entry.getKey();
            Measurer measurer = entry.getValue();
            
            List<String> variables = new ArrayList<>();
            variables.add(scenario.toString());
            
            // First collect the common rows which describe the scenario
            for (Field field : fields) {
                try {
                    variables.add(((Parameter) field.get(scenario)).toString());
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    variables.add("");
                }
            }
            
            // then print a row for each of the entries
            for (Measurer.Entry measurement : measurer.entries) {
                List<String> row = new ArrayList<>(variables);
                row.add(measurement.driver.getSimpleName());
                row.add(measurement.route);
                row.add(Float.toString(measurement.drivingTime));
                printRow(row, System.out);
            }
        }
    }
    
    static private void printRow(List<String> row, PrintStream out) {
        Iterator<String> it = row.iterator();
        
        while (true) {
            out.print(it.next());
            if (it.hasNext()) {
                out.print(",");
            } else {
                out.println();
                break;
            }
        }
    }
    
    static public void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: graph-file parameter-file");
            System.exit(-1);
        }
        
        File graph = new File(args[0]);
        File params = new File(args[1]);
//        File graph = new File("input/graaf2.txt");
//        File params = new File("input/experiment.txt");
        
        Batch batch = Batch.read(graph, params);
        batch.run(Runtime.getRuntime().availableProcessors() - 1);
    }
}
