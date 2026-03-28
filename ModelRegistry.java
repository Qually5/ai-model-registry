package com.qually5.registry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.time.Instant;

// Represents a machine learning model version
class ModelVersion {
    private String id;
    private String modelName;
    private String version;
    private String artifactUri;
    private String status; // e.g., "Staging", "Production", "Archived"
    private Instant createdAt;

    public ModelVersion(String modelName, String version, String artifactUri) {
        this.id = UUID.randomUUID().toString();
        this.modelName = modelName;
        this.version = version;
        this.artifactUri = artifactUri;
        this.status = "None";
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getModelName() { return modelName; }
    public String getVersion() { return version; }
    public String getArtifactUri() { return artifactUri; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("ModelVersion{name='%s', version='%s', status='%s', uri='%s'}", 
                             modelName, version, status, artifactUri);
    }
}

// Core Registry Service
public class ModelRegistry {
    // In-memory storage for demonstration
    private Map<String, List<ModelVersion>> registryStorage = new ConcurrentHashMap<>();

    public void registerModel(String modelName, String version, String artifactUri) {
        ModelVersion newVersion = new ModelVersion(modelName, version, artifactUri);
        registryStorage.computeIfAbsent(modelName, k -> new ArrayList<>()).add(newVersion);
        System.out.println("Registered new model version: " + newVersion);
    }

    public void transitionModelVersionStage(String modelName, String version, String stage) {
        List<ModelVersion> versions = registryStorage.get(modelName);
        if (versions != null) {
            for (ModelVersion mv : versions) {
                if (mv.getVersion().equals(version)) {
                    mv.setStatus(stage);
                    System.out.println("Transitioned " + modelName + " v" + version + " to " + stage);
                    return;
                }
            }
        }
        System.err.println("Model version not found: " + modelName + " v" + version);
    }

    public ModelVersion getProductionModel(String modelName) {
        List<ModelVersion> versions = registryStorage.get(modelName);
        if (versions != null) {
            for (ModelVersion mv : versions) {
                if ("Production".equals(mv.getStatus())) {
                    return mv;
                }
            }
        }
        return null;
    }

    public void listModels() {
        System.out.println("--- Current Model Registry State ---");
        registryStorage.forEach((name, versions) -> {
            System.out.println("Model: " + name);
            versions.forEach(v -> System.out.println("  - " + v));
        });
        System.out.println("------------------------------------");
    }

    public static void main(String[] args) {
        System.out.println("Initializing AI Model Registry Service...");
        ModelRegistry registry = new ModelRegistry();

        // Simulate registering models
        registry.registerModel("fraud-detection-xgboost", "1.0.0", "s3://models/fraud/v1.0.0/model.pkl");
        registry.registerModel("fraud-detection-xgboost", "1.1.0", "s3://models/fraud/v1.1.0/model.pkl");
        registry.registerModel("customer-churn-dnn", "2.0.0", "s3://models/churn/v2.0.0/saved_model.pb");

        // Simulate stage transitions
        registry.transitionModelVersionStage("fraud-detection-xgboost", "1.0.0", "Production");
        registry.transitionModelVersionStage("fraud-detection-xgboost", "1.1.0", "Staging");
        registry.transitionModelVersionStage("customer-churn-dnn", "2.0.0", "Production");

        // List all models
        registry.listModels();

        // Fetch a production model
        ModelVersion prodFraudModel = registry.getProductionModel("fraud-detection-xgboost");
        if (prodFraudModel != null) {
            System.out.println("Serving traffic using artifact: " + prodFraudModel.getArtifactUri());
        }
    }
}
