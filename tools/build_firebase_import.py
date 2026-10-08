"""Genera tools/firebase_import.json con `inventory` y `searchIndex` a partir de stock.json.

Importar en Firebase Console > Realtime Database > (menú ⋮) > Importar JSON.
La tokenización DEBE coincidir con `tokenize()` en ProductSearchLogic.kt.
"""
import json
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "app/src/main/assets/stock.json"
OUT = Path(__file__).resolve().parent / "firebase_import.json"
MIN_TOKEN_LENGTH = 2


def tokenize(text: str) -> set[str]:
    nfd = unicodedata.normalize("NFD", text)
    stripped = "".join(c for c in nfd if unicodedata.category(c) != "Mn")
    return {t for t in re.split(r"[^a-z0-9]+", stripped.lower()) if len(t) >= MIN_TOKEN_LENGTH}


def main() -> None:
    stock = json.loads(SRC.read_text(encoding="utf-8"))
    index: dict[str, dict[str, bool]] = {}
    for key, value in stock.items():
        for token in tokenize(key) | tokenize(value["model"]):
            index.setdefault(token, {})[key] = True
    OUT.write_text(
        json.dumps({"inventory": stock, "searchIndex": index}, ensure_ascii=False, indent=1),
        encoding="utf-8",
    )
    print(f"{len(stock)} productos, {len(index)} palabras indexadas -> {OUT}")


if __name__ == "__main__":
    main()
