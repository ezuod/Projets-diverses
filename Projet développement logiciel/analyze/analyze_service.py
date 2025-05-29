from fastapi import FastAPI, File, UploadFile
from pydantic import BaseModel
from fastapi.responses import JSONResponse
from transformers import BlipProcessor, BlipForConditionalGeneration
from keybert import KeyBERT
from PIL import Image
from pathlib import Path
import torch, io, os


app = FastAPI()

# CONFIG
device = "cuda" if torch.cuda.is_available() else "cpu"

# Chargement du modèle
processor = BlipProcessor.from_pretrained("Salesforce/blip-image-captioning-base")
model = BlipForConditionalGeneration.from_pretrained(
    "Salesforce/blip-image-captioning-base",
    torch_dtype=torch.float16 if device == "cuda" else torch.float32
).to(device)
kw_model = KeyBERT()


class PathRequest(BaseModel):
    path: str

def analyze(image: Image.Image):
    inputs = processor(images=image, return_tensors="pt").to(device)
    output = model.generate(**inputs, max_new_tokens=150)
    caption = processor.batch_decode(output, skip_special_tokens=True)[0]
    keywords = [kw[0] for kw in kw_model.extract_keywords(caption, top_n=10)]
    return {"caption": caption, "keywords": keywords}

@app.post("/analyze-filepath")
def analyze_from_path(data: PathRequest):
    # Rendre le chemin RELATIF par sécurité
    relative_path = Path(data.path)

    # Rebaser depuis le répertoire courant
    base_dir = Path(__file__).resolve().parent
    image_path = (base_dir / ".." / "backend" / "src" / "main" / "resources" / "images" / relative_path.name).resolve()

    if not image_path.exists():
        return JSONResponse(status_code=404, content={"error": f"File not found: {image_path}"})

    image = Image.open(image_path).convert("RGB")
    return analyze(image)
