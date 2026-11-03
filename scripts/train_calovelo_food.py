from pathlib import Path
from ultralytics import YOLO

def train_and_export():
    """
    Train YOLOv8-nano food detector on custom food dataset
    and export FP16 TFLite model + class labels for CaloVelo Android app.
    """
    # 1. Load pre-trained YOLOv8-nano object detector
    model = YOLO("yolov8n.pt")

    # 2. Train on dataset (e.g. Roboflow/Kaggle Indian + Food-101 in YOLO format)
    model.train(
        data="food_dataset/data.yaml",
        epochs=60,
        imgsz=640,
        batch=16,
        device=0,          # NVIDIA GPU (set to 'cpu' if training on CPU)
        workers=4,
        patience=15,
        project="calovelo_vision",
        name="yolov8n_food"
    )

    # 3. Export best weights to FP16 TFLite (.tflite)
    best_pt = Path("calovelo_vision/yolov8n_food/weights/best.pt")
    trained_model = YOLO(str(best_pt))
    tflite_path = trained_model.export(format="tflite", imgsz=640, half=True)
    print(f"Exported TFLite model to: {tflite_path}")

    # 4. Export ordered class labels to food_labels.txt
    labels_out = Path("food_labels.txt")
    names = trained_model.names
    with labels_out.open("w", encoding="utf-8") as f:
        for idx in sorted(names.keys()):
            f.write(f"{names[idx]}\n")
    print(f"Saved {len(names)} class labels to {labels_out.resolve()}")

if __name__ == "__main__":
    train_and_export()
