"""
CaloVelo YOLOv8 Food Detection Training Script
Trains a custom YOLOv8-nano model on a food dataset (like Food-101 or Nutrition5k)
and exports it to TensorFlow Lite (.tflite) for Android deployment.
"""

import os
from ultralytics import YOLO

def train_and_export():
    # 1. Initialize YOLOv8-nano (smallest, fastest model for mobile)
    print("Initializing YOLOv8n...")
    model = YOLO("yolov8n.pt")

    # Define dataset path (replace with your dataset.yaml path)
    dataset_yaml = "dataset/data.yaml"

    if not os.path.exists(dataset_yaml):
        print(f"Warning: Dataset YAML not found at {dataset_yaml}. Please configure your dataset first.")
        print("To download a sample dataset, use the download_dataset.py script.")
        return

    # 2. Train the model
    # Batch size 16 and image size 640x640 is optimal for TFLite mobile inference
    print("Starting training...")
    model.train(
        data=dataset_yaml,
        epochs=50,
        imgsz=640,
        batch=16,
        device="auto", # Automatically uses GPU if available
        project="calovelo_vision",
        name="food_detector",
        patience=10
    )

    # 3. Export to TensorFlow Lite (FP16 precision for mobile speed)
    print("Exporting model to TFLite format...")
    best_weights = "calovelo_vision/food_detector/weights/best.pt"

    if os.path.exists(best_weights):
        trained_model = YOLO(best_weights)
        tflite_path = trained_model.export(
            format="tflite",
            imgsz=640,
            half=True, # FP16 quantization
            optimize=True
        )
        print(f"✅ Successfully exported TFLite model to: {tflite_path}")

        # 4. Generate class labels file for Android assets
        names = trained_model.names
        labels_path = "food_labels.txt"
        with open(labels_path, "w", encoding="utf-8") as f:
            # YOLOv8 class indices are guaranteed to be sequential
            for idx in sorted(names.keys()):
                f.write(f"{names[idx]}\n")
        print(f"✅ Generated {len(names)} class labels in: {labels_path}")
        print("\nNEXT STEPS:")
        print(f"Copy the generated .tflite file and {labels_path} to:")
        print("CaloVelo/app/src/main/assets/")
    else:
        print("Error: Training failed to produce best.pt weights.")

if __name__ == "__main__":
    train_and_export()
