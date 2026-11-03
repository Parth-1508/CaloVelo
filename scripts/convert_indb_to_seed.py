import csv
import json

def convert_indb_to_seed(csv_path: str, json_out_path: str):
    """
    Converts raw INDB (Indian Nutrient Databank) or USDA CSV export
    into CaloVelo nutrition_seed.json asset format.
    """
    catalog = []
    with open(csv_path, mode="r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row["food_name"].strip()
            label_key = name.lower().replace(" ", "_").replace("-", "_")
            catalog.append({
                "labelKey": label_key,
                "displayName": name,
                "category": row.get("category", "General"),
                "defaultServingGrams": int(float(row.get("serving_g", 150))),
                "kcalPer100g": int(float(row["energy_kcal"])),
                "proteinPer100g": round(float(row["protein_g"]), 1),
                "carbsPer100g": round(float(row["carb_g"]), 1),
                "fatPer100g": round(float(row["fat_g"]), 1),
                "fiberPer100g": round(float(row.get("fibre_g", 0.0)), 1)
            })

    with open(json_out_path, mode="w", encoding="utf-8") as out:
        json.dump(catalog, out, indent=2)

    print(f"Successfully converted {len(catalog)} food items to {json_out_path}")

if __name__ == "__main__":
    convert_indb_to_seed("indb_raw.csv", "nutrition_seed.json")
