"""
Dataset Download Helper for CaloVelo Food Detector Training.

Options:
1. Roboflow Universe (Annotated Indian Food Dataset & Food-101 in YOLOv8 format)
2. Kaggle API (Indian Food Images Dataset)
"""

def download_roboflow_dataset(api_key: str):
    try:
        from roboflow import Roboflow
        rf = Roboflow(api_key=api_key)
        # Indian Food Dataset on Roboflow (YOLOv8 format)
        project = rf.workspace("indian-food-detection").project("indian-food-recognition")
        dataset = project.version(1).download("yolov8")
        print(f"Dataset downloaded to: {dataset.location}")
    except ImportError:
        print("Please install roboflow: pip install roboflow")

def download_kaggle_dataset():
    """
    Downloads Indian Food Dataset via Kaggle CLI.
    Prerequisites: pip install kaggle, set up ~/.kaggle/kaggle.json
    """
    import os
    os.system("kaggle datasets download -d kmader/food41 -p food_dataset --unzip")
    print("Downloaded Food-101 dataset to food_dataset/")

if __name__ == "__main__":
    print("Run download_roboflow_dataset('YOUR_ROBOFLOW_API_KEY') to fetch annotated food datasets.")
