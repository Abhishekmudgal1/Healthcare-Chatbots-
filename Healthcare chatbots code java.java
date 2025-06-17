import weka.classifiers.trees.J48;
import weka.core.*;
import weka.core.converters.CSVLoader;
import java.io.*;
import java.util.*;

public class HealthcareChatbot {

    private static Instances trainingData;
    private static J48 tree;
    private static ArrayList<String> symptomsList = new ArrayList<>();
    private static Map<String, String[]> doctorMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        // Load training dataset
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("Training.csv"));
        trainingData = loader.getDataSet();
        trainingData.setClassIndex(trainingData.numAttributes() - 1);

        // Train Decision Tree
        tree = new J48();
        tree.buildClassifier(trainingData);

        // Prepare symptoms list
        for (int i = 0; i < trainingData.numAttributes() - 1; i++) {
            symptomsList.add(trainingData.attribute(i).name());
        }

        // Load doctor recommendations
        loadDoctors("doctors_dataset.csv");

        // Run the chatbot
        executeBot();
    }

    private static void loadDoctors(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",", 3);  // Disease, Name, Link
            if (parts.length == 3) {
                doctorMap.put(parts[0].trim(), new String[]{parts[1].trim(), parts[2].trim()});
            }
        }
        reader.close();
    }

    private static void executeBot() throws Exception {
        Scanner scanner = new Scanner(System.in);
        double[] values = new double[trainingData.numAttributes()];
        Arrays.fill(values, 0);

        System.out.println("Please respond with yes/Yes or no/No:");

        for (int i = 0; i < symptomsList.size(); i++) {
            System.out.print("Do you have " + symptomsList.get(i) + "? ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("yes")) {
                values[i] = 1;
            }
        }

        Instance inputInstance = new DenseInstance(1.0, values);
        inputInstance.setDataset(trainingData);

        double prediction = tree.classifyInstance(inputInstance);
        String predictedDisease = trainingData.classAttribute().value((int) prediction);

        System.out.println("\nYou may have: " + predictedDisease);

        if (doctorMap.containsKey(predictedDisease)) {
            String[] doctorInfo = doctorMap.get(predictedDisease);
            System.out.println("Consult: " + doctorInfo[0]);
            System.out.println("Visit: " + doctorInfo[1]);
        } else {
            System.out.println("No doctor info available for this disease.");
        }
    }
}
/*react.js frontend outlines */
import React, { useState, useEffect } from 'react';
import axios from 'axios';

const ChatbotUI = () => {
  const [symptoms, setSymptoms] = useState([]);
  const [selected, setSelected] = useState([]);
  const [result, setResult] = useState(null);

  useEffect(() => {
    axios.get("/api/symptoms").then(res => setSymptoms(res.data));
  }, []);

  const handleCheck = (symptom) => {
    if (selected.includes(symptom)) {
      setSelected(selected.filter(s => s !== symptom));
    } else {
      setSelected([...selected, symptom]);
    }
  };

  const handleSubmit = () => {
    axios.post("/api/predict", { symptoms: selected })
      .then(res => setResult(res.data));
  };

  return (
    <div>
      <h2>Check your symptoms</h2>
      {symptoms.map(symptom => (
        <label key={symptom}>
          <input type="checkbox" onChange={() => handleCheck(symptom)} />
          {symptom}
        </label>
      ))}
      <button onClick={handleSubmit}>Predict Disease</button>
      {result && (
        <div>
          <h3>You may have: {result.disease}</h3>
          <p>Consult: {result.doctor}</p>
          <a href={result.link} target="_blank" rel="noreferrer">More Info</a>
        </div>
      )}
    </div>
  );
};

export default ChatbotUI;
# java +react integration framework
ReactJS UI  <----->  Spring Boot REST API  <----->  Weka Model & Prediction Logic
      ↑                                             ↑
   Collect symptoms                           Reads model, predicts,
      ↓                                           returns response
REST POST /api/predict                  Java HealthcareChatbot.predictDisease()

#  To Convert Java to REST API: (Use Spring Boot)

public class ChatbotController {

    @PostMapping("/api/predict")
    public ResponseEntity<?> predict(@RequestBody List<String> symptoms) {
        // Convert symptom list into Weka instance, classify, return JSON
    }

    @GetMapping("/api/symptoms")
    public List<String> getSymptoms() {
        return HealthcareChatbot.getSymptomsList();
    }
}
