import csv
import json
import re

def clean_text(text):
    if not text:
        return ""
    # Remove HTML tags or weird quotes
    text = re.sub(r'<[^>]+>', '', text)
    text = text.replace('""', '"')
    return text.strip()

def process_mega_gym(csv_path, json_out_path):
    """
    Parses MegaGymDataset.csv and outputs a clean JSON seed
    for CaloVelo Phase 4 (Exercise Catalog).
    """
    catalog = []

    with open(csv_path, mode="r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            title = row.get("Title", "").strip()
            if not title:
                continue

            # Create a unique ID/Key (e.g., "barbell_bench_press")
            exercise_id = title.lower().replace(" ", "_").replace("-", "_")
            exercise_id = re.sub(r'[^a-z0-9_]', '', exercise_id)

            # Map BodyPart to standardized ENUM-friendly strings
            body_part = row.get("BodyPart", "Other").upper().replace(" ", "_")

            # Map Equipment
            equipment = row.get("Equipment", "Bodyweight").upper().replace(" ", "_")
            if not equipment:
                equipment = "BODYWEIGHT"

            desc = clean_text(row.get("Desc", ""))

            catalog.append({
                "exerciseId": exercise_id,
                "name": title,
                "muscleGroup": body_part,
                "equipmentType": equipment,
                "instructions": desc
            })

    # Sort alphabetically by name
    catalog.sort(key=lambda x: x["name"])

    # Write to JSON
    with open(json_out_path, mode="w", encoding="utf-8") as out:
        json.dump(catalog, out, indent=2)

    print(f"✅ Successfully converted {len(catalog)} exercises to {json_out_path}")

if __name__ == "__main__":
    csv_input = "temp/megaGymDataset.csv"
    json_output = "app/src/main/assets/gym_seed.json"

    print("Building CaloVelo Gym Exercise Seed...")
    process_mega_gym(csv_input, json_output)
