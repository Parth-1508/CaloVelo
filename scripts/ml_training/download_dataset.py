"""
CaloVelo Dataset Downloader
Helper script to download open-source food datasets for YOLOv8 training.
"""

import os
import sys
import subprocess

def install_roboflow():
    try:
        import roboflow
    except ImportError:
        print("Installing roboflow...")
        subprocess.check_call([sys.executable, "-m", "pip", "install", "roboflow"])

def download_roboflow_dataset():
    install_roboflow()
    from roboflow import Roboflow

    print("This script downloads an Indian Food Detection dataset from Roboflow Universe.")
    api_key = input("Enter your Roboflow API Key (get one at universe.roboflow.com): ").strip()

    if not api_key:
        print("Error: API Key is required.")
        return

    try:
        rf = Roboflow(api_key=api_key)
        # Using a public Indian Food recognition dataset
        project = rf.workspace("indian-food-detection").project("indian-food-recognition")
        # Download in YOLOv8 format
        dataset = project.version(1).download("yolov8")

        print(f"\n✅ Dataset successfully downloaded to: {dataset.location}")
        print("Update train_yolov8_food.py 'dataset_yaml' path to point to this directory's data.yaml")

    except Exception as e:
        print(f"Error downloading dataset: {e}")

if __name__ == "__main__":
    download_roboflow_dataset()
